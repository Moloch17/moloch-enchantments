package com.moloch.molochenchantments;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantingTableListener implements Listener {

    private final ScryingTable scryingTable;

    public EnchantingTableListener(ScryingTable scryingTable) {
        this.scryingTable = scryingTable;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);
            scryingTable.openMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (event.getLootTable() == null) return;

        String key = event.getLootTable().getKey().getKey();
        if (!key.startsWith("fishing")) return;

        List<ItemStack> loot = event.getLoot();
        for (int i = 0; i < loot.size(); i++) {
            ItemStack item = loot.get(i);
            ItemMeta meta = item.getItemMeta();
            boolean hasEnchants = meta != null && !meta.getEnchants().isEmpty();
            if (!hasEnchants) continue;

            if (item.getType() == Material.ENCHANTED_BOOK) {
                loot.set(i, new ItemStack(Material.BOOK, item.getAmount()));
            } else {
                loot.set(i, new ItemStack(item.getType(), item.getAmount()));
            }
        }
    }
}
