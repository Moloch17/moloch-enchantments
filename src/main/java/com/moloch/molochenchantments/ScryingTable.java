package com.moloch.molochenchantments;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.StructureSearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ScryingTable implements Listener {

    // Brighter, more readable purple for locator compass names.
    private static final TextColor LOCATOR_PURPLE = TextColor.color(0xC77DFF);

    // Search ranges. Biome radius is in blocks; structure radius is in chunks.
    private static final int BIOME_RADIUS = 8000;
    private static final int STRUCTURE_RADIUS = 150;

    /** A search strategy that returns the nearest matching location, or null if none is found. */
    @FunctionalInterface
    private interface Locator {
        Location locate(World world, Location origin);
    }

    /**
     * A single scrying trade: what it finds, what it costs (besides the compass),
     * and whether its search may run off the main thread. Biome lookups are async-safe;
     * structure lookups must run on the main thread.
     */
    private record ScryTarget(String display, Material cost, int costAmount, Locator locator, boolean async) {}

    private static final List<ScryTarget> TARGETS = List.of(
        biome("Pale Garden", Biome.PALE_GARDEN),
        biome("Mangrove Swamp", Biome.MANGROVE_SWAMP),
        biome("Eroded Badlands", Biome.ERODED_BADLANDS),
        biome("Lush Cave", Biome.LUSH_CAVES),
        customBiome("Sulfur Cave", NamespacedKey.minecraft("sulfur_caves")),
        biome("Deep Dark", Biome.DEEP_DARK),
        structure("Ocean Monument", Structure.MONUMENT),
        structure("Woodland Mansion", Structure.MANSION),
        chorusStructure("Ancient City", Structure.ANCIENT_CITY)
    );

    private static ScryTarget biome(String display, Biome biome) {
        return new ScryTarget(display, Material.LAPIS_BLOCK, 1, biomeLocator(biome), true);
    }

    private static ScryTarget customBiome(String display, NamespacedKey key) {
        return new ScryTarget(display, Material.LAPIS_BLOCK, 1, (world, origin) -> {
            Biome biome = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
            return biome == null ? null : locateBiome(world, origin, biome);
        }, true);
    }

    private static ScryTarget structure(String display, Structure structure) {
        return new ScryTarget(display, Material.LAPIS_BLOCK, 1, structureLocator(structure), false);
    }

    private static ScryTarget chorusStructure(String display, Structure structure) {
        return new ScryTarget(display, Material.POPPED_CHORUS_FRUIT, 64, structureLocator(structure), false);
    }

    private static Locator biomeLocator(Biome biome) {
        return (world, origin) -> locateBiome(world, origin, biome);
    }

    private static Locator structureLocator(Structure structure) {
        return (world, origin) -> {
            StructureSearchResult result =
                world.locateNearestStructure(origin, structure, STRUCTURE_RADIUS, false);
            return result != null ? result.getLocation() : null;
        };
    }

    private static Location locateBiome(World world, Location origin, Biome biome) {
        BiomeSearchResult result = world.locateNearestBiome(origin, BIOME_RADIUS, biome);
        return result != null ? result.getLocation() : null;
    }

    private final Plugin plugin;
    private final Set<UUID> activePlayers = new HashSet<>();

    public ScryingTable(Plugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Merchant merchant = Bukkit.createMerchant();
        List<MerchantRecipe> recipes = new ArrayList<>();

        for (ScryTarget target : TARGETS) {
            ItemStack result = new ItemStack(Material.COMPASS);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(locatorName(target.display()));
            result.setItemMeta(resultMeta);

            // Costs a compass + the target's payment (a lapis block, or popped chorus fruit).
            MerchantRecipe recipe = new MerchantRecipe(result, 0, Integer.MAX_VALUE, false);
            recipe.addIngredient(new ItemStack(Material.COMPASS));
            recipe.addIngredient(new ItemStack(target.cost(), target.costAmount()));
            recipes.add(recipe);
        }

        merchant.setRecipes(recipes);
        activePlayers.add(player.getUniqueId());
        var view = MenuType.MERCHANT.builder()
            .title(Component.text("Scrying Table"))
            .merchant(merchant)
            .checkReachable(false)
            .build(player);
        player.openInventory(view);
    }

    /** Purple, non-italic name shared by the trade preview and the finished compass. */
    private Component locatorName(String displayName) {
        return Component.text(displayName + " Locator Compass")
            .color(LOCATOR_PURPLE)
            .decoration(TextDecoration.ITALIC, false);
    }

    @EventHandler
    public void onMerchantClick(InventoryClickEvent event) {
        if (!activePlayers.contains(event.getWhoClicked().getUniqueId())) return;
        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        int index = merchantInv.getSelectedRecipeIndex();
        if (index < 0 || index >= TARGETS.size()) return;
        ScryTarget target = TARGETS.get(index);

        // Locate the two ingredient slots regardless of which the player filled first.
        int compassSlot = findIngredient(merchantInv, Material.COMPASS);
        int costSlot = findIngredient(merchantInv, target.cost());
        if (compassSlot < 0 || costSlot < 0) return;

        ItemStack costItem = merchantInv.getItem(costSlot);
        if (costItem == null || costItem.getAmount() < target.costAmount()) return;

        consume(merchantInv, compassSlot, 1);
        consume(merchantInv, costSlot, target.costAmount());

        plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
        activateCompass(player, target);
    }

    private int findIngredient(MerchantInventory inv, Material type) {
        for (int slot = 0; slot < 2; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() == type) return slot;
        }
        return -1;
    }

    private void consume(MerchantInventory inv, int slot, int amount) {
        ItemStack item = inv.getItem(slot);
        if (item == null) return;
        if (item.getAmount() > amount) {
            item.setAmount(item.getAmount() - amount);
            inv.setItem(slot, item);
        } else {
            inv.setItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        activePlayers.remove(event.getPlayer().getUniqueId());
    }

    private void activateCompass(Player player, ScryTarget target) {
        String displayName = target.display();
        player.sendMessage(Component.text("Searching for nearest " + displayName + "...")
            .decoration(TextDecoration.ITALIC, false));

        final Location origin = player.getLocation();
        final World world = player.getWorld();
        if (target.async()) {
            // Biome searches are async-safe; run them off the main thread, then deliver back on it.
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Location found = target.locator().locate(world, origin);
                plugin.getServer().getScheduler().runTask(plugin, () -> deliver(player, target, found));
            });
        } else {
            // Structure searches must run on the main thread.
            plugin.getServer().getScheduler().runTask(plugin, () ->
                deliver(player, target, target.locator().locate(world, origin)));
        }
    }

    private void deliver(Player player, ScryTarget target, Location found) {
        String displayName = target.display();
        if (found == null) {
            player.sendMessage(Component.text("Could not find " + displayName + " within range.")
                .decoration(TextDecoration.ITALIC, false));
            return;
        }

        ItemStack locator = buildLocatorCompass(found, displayName);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(locator);
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        player.sendMessage(Component.text("Your compass now points to the nearest " + displayName + ".")
            .decoration(TextDecoration.ITALIC, false));
    }

    private ItemStack buildLocatorCompass(Location target, String displayName) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setLodestone(target);
        meta.setLodestoneTracked(false);
        meta.displayName(locatorName(displayName));
        compass.setItemMeta(meta);
        return compass;
    }
}
