package com.moloch.molochenchantments;

import java.util.List;

import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

}