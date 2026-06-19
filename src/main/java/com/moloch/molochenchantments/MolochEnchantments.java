package com.moloch.molochenchantments;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MolochEnchantments extends JavaPlugin {

    private EnchantTableDecorator decorator;
    private ResourcePackService resourcePack;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ScryingTable scryingTable = new ScryingTable(this);
        Bukkit.getPluginManager().registerEvents(scryingTable, this);
        Bukkit.getPluginManager().registerEvents(new EnchantingTableListener(scryingTable), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(this), this);

        decorator = new EnchantTableDecorator(this);
        Bukkit.getPluginManager().registerEvents(decorator, this);
        decorator.start();

        EnchantingTableRecipe tableRecipe = new EnchantingTableRecipe(this);
        tableRecipe.register();
        Bukkit.getPluginManager().registerEvents(tableRecipe, this);

        resourcePack = new ResourcePackService(this);
        if (resourcePack.start()) {
            Bukkit.getPluginManager().registerEvents(resourcePack, this);
        }

        getLogger().info("MolochEnchantments has been enabled!");

        EnchantBookRecipes recipes = new EnchantBookRecipes(this);
        recipes.registerAll();
        Bukkit.getPluginManager().registerEvents(recipes, this);
    }

    @Override
    public void onDisable() {
        if (decorator != null) {
            decorator.stop();
        }
        if (resourcePack != null) {
            resourcePack.stop();
        }
        getLogger().info("MolochEnchantments has been disabled!");
    }
}