package com.moloch.molochenchantments;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MolochEnchantments extends JavaPlugin {

    @Override
    public void onEnable() {
        removeEnchantingTableRecipe();
        Bukkit.getPluginManager().registerEvents(new EnchantingTableListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(this), this);
        getLogger().info("MolochEnchantments has been enabled!");

        EnchantBookRecipes recipes = new EnchantBookRecipes(this);
        recipes.registerAll();
        Bukkit.getPluginManager().registerEvents(recipes, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("MolochEnchantments has been disabled!");
    }

    private void removeEnchantingTableRecipe() {
        NamespacedKey key = NamespacedKey.minecraft("enchanting_table");
        boolean removed = Bukkit.removeRecipe(key);
        if (removed) {
            getLogger().info("Removed enchanting table recipe.");
        } else {
            getLogger().warning("Enchanting table recipe was not found / already removed.");
        }
    }
}