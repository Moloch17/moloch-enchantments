package com.moloch.molochenchantments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

/**
 * Replaces the vanilla enchanting table recipe (which uses a book) with one that
 * uses an eye of ender instead, and discovers it for players on join so it shows
 * in the recipe book and works under the doLimitedCrafting gamerule.
 */
public final class EnchantingTableRecipe implements Listener {

    private final Plugin plugin;
    private final NamespacedKey recipeKey;

    public EnchantingTableRecipe(Plugin plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "enchanting_table");
    }

    public void register() {
        // Drop the vanilla book recipe so only the eye-of-ender variant remains.
        Bukkit.removeRecipe(NamespacedKey.minecraft("enchanting_table"));

        if (Bukkit.getRecipe(recipeKey) == null) {
            ShapedRecipe recipe = new ShapedRecipe(recipeKey, new ItemStack(Material.ENCHANTING_TABLE));
            recipe.shape(" E ", "DOD", "OOO");
            recipe.setIngredient('E', Material.ENDER_EYE);
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('O', Material.OBSIDIAN);
            Bukkit.addRecipe(recipe, true);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipe(recipeKey);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(recipeKey);
    }
}
