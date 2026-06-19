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
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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

    // Scry timing (in ticks; 20 ticks = 1s). The portal ambience loops at 0.75 volume for at
    // least MIN_SCRY_TICKS and until the search returns, then stops, holds silence, and pops.
    private static final int MIN_SCRY_TICKS = 100;      // minimum 5s of looping
    private static final int LOOP_PERIOD_TICKS = 60;    // re-trigger the ~3s ambience so it loops
    private static final float LOOP_VOLUME = 0.5f;
    private static final int HOLD_TICKS = 20;           // hold silence 1s before popping
    private static final double EJECT_UP = 0.3;         // upward launch speed (arc height)
    private static final double EJECT_OUT = 0.075;      // outward launch speed (~1 block away)

    private final Plugin plugin;
    private final EnchantTableDecorator decorator;
    private final Set<UUID> activePlayers = new HashSet<>();
    // The table block a player is currently scrying at, so the result can pop out of its eye.
    private final Map<UUID, Location> tableLocations = new HashMap<>();

    public ScryingTable(Plugin plugin, EnchantTableDecorator decorator) {
        this.plugin = plugin;
        this.decorator = decorator;
    }

    public void openMenu(Player player, Location table) {
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
        tableLocations.put(player.getUniqueId(), table);
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

        ItemStack compassItem = merchantInv.getItem(compassSlot);
        ItemStack costItem = merchantInv.getItem(costSlot);
        if (compassItem == null || costItem == null || costItem.getAmount() < target.costAmount()) return;

        Location table = tableLocations.get(player.getUniqueId());
        if (table == null) return;

        // Snapshot exactly what is being spent so a failed scry can refund the real items
        // (e.g. an existing locator compass the player inserted), not generic replacements.
        ItemStack refundCompass = compassItem.clone();
        refundCompass.setAmount(1);
        ItemStack refundCost = costItem.clone();
        refundCost.setAmount(target.costAmount());

        consume(merchantInv, compassSlot, 1);
        consume(merchantInv, costSlot, target.costAmount());

        plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
        startScry(player, target, table, refundCompass, refundCost);
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
        UUID id = event.getPlayer().getUniqueId();
        activePlayers.remove(id);
        tableLocations.remove(id);
    }

    /**
     * Runs the search, then ~2s later (or as soon as the search returns, whichever is later)
     * has the eye floating above the table pop out the result: the located compass on success,
     * or the refunded ingredients if nothing was found.
     */
    private void startScry(Player player, ScryTarget target, Location table,
                           ItemStack refundCompass, ItemStack refundCost) {
        final Location origin = player.getLocation();
        final World searchWorld = player.getWorld();
        final Location castAt = decorator.eyeLocation(table);
        final World world = castAt.getWorld();
        if (world == null) return;

        // Launch the ejection toward where the player stood at click time (they may move or
        // disconnect before the search finishes). Outward speed matches the upward speed.
        Vector horizontal = origin.toVector().subtract(castAt.toVector()).setY(0);
        if (horizontal.lengthSquared() > 1.0e-6) {
            horizontal.normalize().multiply(EJECT_OUT);
        } else {
            horizontal.zero();
        }
        final Vector launch = new Vector(horizontal.getX(), EJECT_UP, horizontal.getZ());

        // Kick off the search; the result is recorded for the timer below to pick up.
        final Location[] result = new Location[1];
        final boolean[] searchDone = new boolean[1];
        runSearch(target, searchWorld, origin, found -> {
            result[0] = found;
            searchDone[0] = true;
        });

        new BukkitRunnable() {
            int ticks = 0;
            int phase = 0;       // 0 = looping, 1 = fading out, 2 = holding silence
            int phaseStart = 0;

            @Override
            public void run() {
                switch (phase) {
                    case 0 -> {
                        // Loop the portal ambience from the table's eye, starting immediately.
                        if (ticks % LOOP_PERIOD_TICKS == 0) {
                            playLoop(world, castAt, LOOP_VOLUME);
                        }
                        // Loop for at least MIN_SCRY_TICKS, and keep looping until the search returns.
                        if (ticks >= MIN_SCRY_TICKS && searchDone[0]) {
                            stopLoop(world);
                            phase = 1;
                            phaseStart = ticks;
                        }
                    }
                    default -> {
                        // Hold silence briefly, then pop the result out of the eye.
                        if (ticks - phaseStart >= HOLD_TICKS) {
                            completeScry(target, table, result[0], refundCompass, refundCost, launch);
                            cancel();
                            return;
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playLoop(World world, Location at, float volume) {
        world.playSound(at, Sound.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, volume, 1.0f);
    }

    private void stopLoop(World world) {
        for (Player p : world.getPlayers()) {
            p.stopSound(Sound.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS);
        }
    }

    /** Dispatches the locate call on the correct thread and hands the result back on the main thread. */
    private void runSearch(ScryTarget target, World world, Location origin, Consumer<Location> callback) {
        if (target.async()) {
            // Biome searches are async-safe; run them off the main thread, then resume on it.
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Location found = target.locator().locate(world, origin);
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(found));
            });
        } else {
            // Structure searches must run on the main thread.
            plugin.getServer().getScheduler().runTask(plugin,
                () -> callback.accept(target.locator().locate(world, origin)));
        }
    }

    private void completeScry(ScryTarget target, Location table, Location found,
                              ItemStack refundCompass, ItemStack refundCost, Vector launch) {
        // Pop from the eye's computed hover point, so it works even if the eye is faded out.
        Location pop = decorator.eyeLocation(table);
        World world = pop.getWorld();
        if (world == null) return;

        if (found != null) {
            popOut(world, pop, buildLocatorCompass(found, target.display()), launch);
            world.playSound(pop, Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f);
        } else {
            // Nothing found: refund the exact items the player spent.
            popOut(world, pop, refundCompass, launch);
            popOut(world, pop, refundCost, launch);
            world.playSound(pop, Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1.0f, 0.7f);
        }
    }

    /** Drops a stack at the eye so it pops up and outward (toward the crafter) out of the table. */
    private void popOut(World world, Location pop, ItemStack stack, Vector launch) {
        world.dropItem(pop, stack, item -> {
            item.setVelocity(launch.clone());
            item.setPickupDelay(10);
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
