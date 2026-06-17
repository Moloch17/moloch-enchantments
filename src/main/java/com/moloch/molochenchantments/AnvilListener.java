package com.moloch.molochenchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;

public final class AnvilListener implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public AnvilListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilDamage(AnvilDamagedEvent event) {
        if (random.nextInt(4) != 0) {
            event.setCancelled(true);
        }
    }

    // When any item lands in an input slot, reset its RepairCost on the next tick.
    // Writing it back via setItem() triggers a fresh PrepareAnvilEvent where vanilla
    // can compute the result without hitting the "Too Expensive!" ceiling.
    @EventHandler
    public void onAnvilInput(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory anvil)) return;
        // Output slot is handled by onAnvilTake
        if (event.getClickedInventory() instanceof AnvilInventory && event.getSlot() == 2) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> resetInputCosts(anvil));
    }

    // Cap repair cost and provide an instant result for common cases while the
    // scheduled reset hasn't fired yet (e.g. book application, same-type combining).
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilView view = event.getView();
        ItemStack first = view.getItem(0);
        ItemStack result = event.getResult();

        if (result == null && first != null) {
            ItemStack second = view.getItem(1);
            result = buildResult(first, second, view.getRenameText());
            if (result != null) {
                event.setResult(result);
            }
        }

        if (view.getRepairCost() > 10) {
            view.setRepairCost(10);
        }
    }

    // Prevent combining items from stacking enchantments above the highest input level
    @EventHandler(priority = EventPriority.HIGH)
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

    // Reset the prior-work penalty on the output before it reaches the player's inventory
    @EventHandler
    public void onAnvilTake(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;

        ItemStack result = event.getCurrentItem();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta instanceof Repairable repairable && repairable.getRepairCost() > 0) {
            repairable.setRepairCost(0);
            result.setItemMeta(meta);
        }
    }

    private void resetInputCosts(AnvilInventory anvil) {
        for (int slot = 0; slot <= 1; slot++) {
            ItemStack item = anvil.getItem(slot);
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Repairable r) || r.getRepairCost() == 0) continue;
            r.setRepairCost(0);
            item.setItemMeta(meta);
            anvil.setItem(slot, item); // writing back triggers a fresh PrepareAnvilEvent
        }
    }

    // Reconstruct the anvil result for cases where vanilla refuses due to high cost.
    // Material repair (e.g. iron ingot on iron sword) is handled by resetInputCosts instead.
    private ItemStack buildResult(ItemStack base, ItemStack addition, String rename) {
        boolean hasRename = rename != null && !rename.isEmpty();
        if (addition == null && !hasRename) {
            return null;
        }

        ItemStack result = base.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;

        if (hasRename) {
            meta.displayName(Component.text(rename));
        }

        if (addition != null) {
            if (addition.getType() == Material.ENCHANTED_BOOK) {
                ItemMeta addMeta = addition.getItemMeta();
                if (addMeta instanceof EnchantmentStorageMeta bookMeta) {
                    applyEnchants(bookMeta.getStoredEnchants(), meta, result);
                }
            } else if (addition.getType() == base.getType()) {
                repairDurability(meta, addition);
                ItemMeta addMeta = addition.getItemMeta();
                if (addMeta != null) {
                    applyEnchants(addMeta.getEnchants(), meta, result);
                }
            } else {
                return null;
            }
        }

        if (meta instanceof Repairable repairable) {
            repairable.setRepairCost(0);
        }

        result.setItemMeta(meta);
        return result;
    }

    private void repairDurability(ItemMeta baseMeta, ItemStack addition) {
        if (!(baseMeta instanceof org.bukkit.inventory.meta.Damageable dBase)) return;
        ItemMeta addMeta = addition.getItemMeta();
        if (!(addMeta instanceof org.bukkit.inventory.meta.Damageable dAdd)) return;

        int maxDur = addition.getType().getMaxDurability();
        if (maxDur <= 0) return;

        int combined = (maxDur - dBase.getDamage()) + (maxDur - dAdd.getDamage()) + maxDur / 20;
        dBase.setDamage(Math.max(0, maxDur - Math.min(maxDur, combined)));
    }

    private void applyEnchants(Map<Enchantment, Integer> enchants, ItemMeta meta, ItemStack target) {
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int incoming = entry.getValue();
            int current = (meta instanceof EnchantmentStorageMeta s)
                    ? s.getStoredEnchantLevel(ench)
                    : meta.getEnchantLevel(ench);
            int level = (incoming == current) ? incoming + 1 : Math.max(incoming, current);

            if (meta instanceof EnchantmentStorageMeta storage) {
                storage.addStoredEnchant(ench, level, true);
            } else {
                meta.addEnchant(ench, level, true);
            }
        }
    }

    private int getEnchantLevel(ItemStack item, Enchantment ench) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        if (meta instanceof EnchantmentStorageMeta storage) {
            return storage.getStoredEnchantLevel(ench);
        }
        return meta.getEnchantLevel(ench);
    }
}
