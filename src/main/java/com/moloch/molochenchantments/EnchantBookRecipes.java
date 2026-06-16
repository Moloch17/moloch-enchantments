package com.moloch.molochenchantments;

import org.bukkit.Bukkit;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EnchantBookRecipes implements Listener {

    private final org.bukkit.plugin.Plugin plugin;

    private final Map<NamespacedKey, ItemStack> notConsumedMap = new HashMap<>();
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    private static final Set<Enchantment> NO_NUMERAL = Set.of(
            Enchantment.AQUA_AFFINITY,
            Enchantment.CHANNELING,
            Enchantment.FLAME,
            Enchantment.INFINITY,
            Enchantment.MENDING,
            Enchantment.MULTISHOT,
            Enchantment.SILK_TOUCH);

    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V" };

    public EnchantBookRecipes(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    /** Loads all recipes from recipes.json and discovers them for online players. */
    public void registerAll() {
        plugin.getLogger().info("Registering enchanted book recipes from JSON...");
        try {
            loadRecipesFromJson();
            plugin.getLogger().info("Registered " + recipeKeys.size() + " enchanted book recipes.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load recipes from JSON!");
            e.printStackTrace();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            discoverRecipesForPlayer(player);
        }
    }

    private void loadRecipesFromJson() throws IOException {
        InputStream is = plugin.getResource("recipes.json");
        if (is == null) throw new IOException("recipes.json not found in plugin jar");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            JsonArray recipes = root.getAsJsonArray("recipes");

            int loaded = 0;
            List<String> failed = new ArrayList<>();
            for (int i = 0; i < recipes.size(); i++) {
                JsonObject recipeObj = recipes.get(i).getAsJsonObject();
                String name = recipeObj.has("name") ? recipeObj.get("name").getAsString() : "index " + i;
                try {
                    loadRecipeFromJson(recipeObj);
                    loaded++;
                } catch (Exception e) {
                    failed.add(name + " (" + e.getMessage() + ")");
                }
            }

            plugin.getLogger().info("Loaded " + loaded + "/" + recipes.size() + " recipes from JSON.");
            if (!failed.isEmpty()) {
                plugin.getLogger().warning("Failed to register " + failed.size() + " recipe(s):");
                for (String f : failed) {
                    plugin.getLogger().warning("  - " + f);
                }
            }
        }
    }

    /** Parses a single recipe object and registers it. Supports MATERIAL, POTION, and TIPPED_ARROW ingredients. */
    private void loadRecipeFromJson(JsonObject recipeObj) {
        String enchantStr = recipeObj.get("enchantment").getAsString();
        int level = recipeObj.get("level").getAsInt();

        Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(NamespacedKey.minecraft(enchantStr.toLowerCase()));
        if (enchantment == null) {
            throw new IllegalArgumentException("Unknown enchantment: " + enchantStr);
        }

        JsonArray ingredientArray = recipeObj.getAsJsonArray("ingredients");
        List<RecipeChoice> ingredients = new ArrayList<>();
        Material ncReturnMaterial = null;

        for (int i = 0; i < ingredientArray.size(); i++) {
            JsonObject ingredientObj = ingredientArray.get(i).getAsJsonObject();
            String type = ingredientObj.get("type").getAsString();

            RecipeChoice choice = switch (type) {
                case "MATERIAL" -> {
                    Material material = Material.valueOf(ingredientObj.get("value").getAsString());
                    if (ingredientObj.has("nonConsumed") && ingredientObj.get("nonConsumed").getAsBoolean()) {
                        ncReturnMaterial = material;
                    }
                    yield new RecipeChoice.ExactChoice(new ItemStack(material));
                }
                case "POTION" -> {
                    PotionType potionType = PotionType.valueOf(ingredientObj.get("potionType").getAsString());
                    yield createPotionChoice(potionType);
                }
                case "TIPPED_ARROW" -> {
                    PotionType potionType = PotionType.valueOf(ingredientObj.get("potionType").getAsString());
                    yield createTippedArrowChoice(potionType);
                }
                default -> throw new IllegalArgumentException("Unknown ingredient type: " + type);
            };

            ingredients.add(choice);
        }

        register(enchantment.getKey().getKey() + "_" + level, enchantment, level, ingredients, ncReturnMaterial);
    }

    private RecipeChoice createPotionChoice(PotionType potionType) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(potionType);
        potion.setItemMeta(meta);
        return new RecipeChoice.ExactChoice(potion);
    }

    private RecipeChoice createTippedArrowChoice(PotionType potionType) {
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) arrow.getItemMeta();
        meta.setBasePotionType(potionType);
        arrow.setItemMeta(meta);
        return new RecipeChoice.ExactChoice(arrow);
    }

    private String nameFor(Enchantment enchant, int level) {
        String[] words = enchant.getKey().getKey().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        String base = sb.toString().trim();
        return NO_NUMERAL.contains(enchant)
                ? "Book of " + base
                : "Book of " + base + " " + ROMAN[level];
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1).toLowerCase())
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }

    private ItemStack enchantedBook(Enchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchant, level, true);
        meta.displayName(Component.text(nameFor(enchant, level)));
        book.setItemMeta(meta);
        return book;
    }

    private void register(String keyName, Enchantment enchant, int level,
            List<RecipeChoice> ingredients, Material ncReturnMaterial) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);

        ItemStack result = enchantedBook(enchant, level);
        if (ncReturnMaterial != null) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
            meta.lore(List.of(
                Component.text(formatMaterialName(ncReturnMaterial)).color(NamedTextColor.GOLD)
                    .append(Component.text(" will not be consumed when crafting this item").color(NamedTextColor.YELLOW))
            ));
            result.setItemMeta(meta);
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (RecipeChoice choice : ingredients) {
            recipe.addIngredient(choice);
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.add(key);

        if (ncReturnMaterial != null) {
            notConsumedMap.put(key, new ItemStack(ncReturnMaterial));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        discoverRecipesForPlayer(event.getPlayer());
    }

    /** Makes all registered enchant book recipes visible in the player's recipe book. */
    public void discoverRecipesForPlayer(Player player) {
        player.discoverRecipes(recipeKeys);
    }

    /** Returns any non-consumed ingredient to the player's inventory after a matching craft. */
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getRecipe() instanceof org.bukkit.Keyed keyed)) return;

        ItemStack ncItem = notConsumedMap.get(keyed.getKey());
        if (ncItem == null) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(ncItem.clone());
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        });
    }
}
