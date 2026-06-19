package com.moloch.molochenchantments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BiomeSearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ScryingTable implements Listener {

    // Brighter, more readable purple for locator compass names.
    private static final TextColor LOCATOR_PURPLE = TextColor.color(0xC77DFF);

    private static final Map<String, Biome> BIOMES = new LinkedHashMap<>();
    private static final Map<String, String> BIOME_DISPLAY = new LinkedHashMap<>();

    static {
        register("FOREST", Biome.FOREST, "Forest");
        register("DESERT", Biome.DESERT, "Desert");
        register("SNOWY_PLAINS", Biome.SNOWY_PLAINS, "Snowy Plains");
        register("MANGROVE_SWAMP", Biome.MANGROVE_SWAMP, "Mangrove Swamp");
    }

    private static void register(String key, Biome biome, String display) {
        BIOMES.put(key, biome);
        BIOME_DISPLAY.put(key, display);
    }

    private final Plugin plugin;
    private final Set<UUID> activePlayers = new HashSet<>();

    public ScryingTable(Plugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Merchant merchant = Bukkit.createMerchant();
        List<MerchantRecipe> recipes = new ArrayList<>();

        for (Map.Entry<String, String> entry : BIOME_DISPLAY.entrySet()) {
            ItemStack result = new ItemStack(Material.COMPASS);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(locatorName(entry.getValue()));
            result.setItemMeta(resultMeta);

            // Costs a compass + a block of lapis lazuli.
            MerchantRecipe recipe = new MerchantRecipe(result, 0, Integer.MAX_VALUE, false);
            recipe.addIngredient(new ItemStack(Material.COMPASS));
            recipe.addIngredient(new ItemStack(Material.LAPIS_BLOCK));
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

        // Locate the two ingredient slots regardless of which the player filled first.
        int compassSlot = findIngredient(merchantInv, Material.COMPASS);
        int lapisSlot = findIngredient(merchantInv, Material.LAPIS_BLOCK);
        if (compassSlot < 0 || lapisSlot < 0) return;

        int index = merchantInv.getSelectedRecipeIndex();
        List<String> keys = new ArrayList<>(BIOMES.keySet());
        if (index < 0 || index >= keys.size()) return;

        String biomeKey = keys.get(index);
        Biome biome = BIOMES.get(biomeKey);
        String displayName = BIOME_DISPLAY.get(biomeKey);

        consumeOne(merchantInv, compassSlot);
        consumeOne(merchantInv, lapisSlot);

        plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
        activateCompass(player, biome, displayName);
    }

    private int findIngredient(MerchantInventory inv, Material type) {
        for (int slot = 0; slot < 2; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() == type) return slot;
        }
        return -1;
    }

    private void consumeOne(MerchantInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item == null) return;
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            inv.setItem(slot, item);
        } else {
            inv.setItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        activePlayers.remove(event.getPlayer().getUniqueId());
    }

    private void activateCompass(Player player, Biome biome, String displayName) {
        player.sendMessage(Component.text("Searching for nearest " + displayName + "...")
            .decoration(TextDecoration.ITALIC, false));

        final Location origin = player.getLocation();
        final org.bukkit.World world = player.getWorld();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            BiomeSearchResult result = world.locateNearestBiome(origin, 6400, biome);
            Location found = result != null ? result.getLocation() : null;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
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
            });
        });
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
