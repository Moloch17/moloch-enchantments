package com.moloch.molochenchantments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

public final class EnchantingTableListener implements Listener {

    private static final Material BANNED = Material.ENCHANTING_TABLE;

    // ---- Placement / interaction with the block ----

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == BANNED) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Enchanting tables are disabled on this server.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() == BANNED) {
            event.setCancelled(true);
        }
    }

    // Prevent tables from entering inventories in any way

    @EventHandler
    public void onCreativeClick(InventoryCreativeEvent event) {
        if (isBanned(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isBanned(event.getCurrentItem()) || isBanned(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isBanned(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (isBanned(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (isBanned(event.getItem().getItemStack())) {
            event.getItem().remove();
            event.setCancelled(true);
        }
    }

    // Pevent commands from giving enchanting tables

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        if ((msg.startsWith("/give") || msg.startsWith("/minecraft:give"))
                && msg.contains("enchanting_table")) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            player.sendMessage("Enchanting tables are disabled on this server.");
        }
    }

    private boolean isBanned(ItemStack item) {
        return item != null && item.getType() == BANNED;
    }

    // Prevent anything enchanted from being fished up

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (event.getLootTable() == null) {
            return;
        }

        String key = event.getLootTable().getKey().getKey();
        if (!key.startsWith("fishing")) {
            return;
        }

        List<ItemStack> loot = event.getLoot();

        for (int i = 0; i < loot.size(); i++) {
            ItemStack item = loot.get(i);
            ItemMeta meta = item.getItemMeta();

            boolean hasEnchants = meta != null && !meta.getEnchants().isEmpty();
            if (!hasEnchants) {
                continue;
            }

            if (item.getType() == Material.ENCHANTED_BOOK) {
                loot.set(i, new ItemStack(Material.BOOK, item.getAmount()));
            } else {
                loot.set(i, new ItemStack(item.getType(), item.getAmount()));
            }
        }
    }

    // Cap enchantment cost of repairs at ten levels

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilView view = event.getView();
        if (view.getRepairCost() > 10) {
            view.setRepairCost(10);
        }
    }

    // Prevent combining enchanted items from creating higher enchantment levels

    @EventHandler
    public void onPrepareAnvilEnchantCap(PrepareAnvilEvent event) {
        AnvilView view = event.getView();
        ItemStack first = view.getItem(0);
        ItemStack second = view.getItem(1);
        ItemStack result = event.getResult();

        if (first == null || second == null || result == null) {
            return;
        }

        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) {
            return;
        }

        boolean changed = false;

        if (resultMeta instanceof EnchantmentStorageMeta resultStorage) {
            for (Map.Entry<Enchantment, Integer> entry : new HashMap<>(resultStorage.getStoredEnchants()).entrySet()) {
                Enchantment ench = entry.getKey();
                int maxInputLevel = Math.max(getEnchantLevel(first, ench), getEnchantLevel(second, ench));
                if (entry.getValue() > maxInputLevel) {
                    resultStorage.removeStoredEnchant(ench);
                    resultStorage.addStoredEnchant(ench, maxInputLevel, true);
                    changed = true;
                }
            }
        } else {
            for (Map.Entry<Enchantment, Integer> entry : new HashMap<>(resultMeta.getEnchants()).entrySet()) {
                Enchantment ench = entry.getKey();
                int maxInputLevel = Math.max(getEnchantLevel(first, ench), getEnchantLevel(second, ench));
                if (entry.getValue() > maxInputLevel) {
                    resultMeta.removeEnchant(ench);
                    resultMeta.addEnchant(ench, maxInputLevel, true);
                    changed = true;
                }
            }
        }

        if (changed) {
            result.setItemMeta(resultMeta);
            event.setResult(result);
        }
    }

    private int getEnchantLevel(ItemStack item, Enchantment ench) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }
        if (meta instanceof EnchantmentStorageMeta storage) {
            return storage.getStoredEnchantLevel(ench);
        }
        return meta.getEnchantLevel(ench);
    }
}