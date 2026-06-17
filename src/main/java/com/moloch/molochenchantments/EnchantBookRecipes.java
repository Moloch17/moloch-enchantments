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
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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

    public void registerAll() {
        plugin.getLogger().info("Registering enchanted book recipes...");
        int loaded = 0;
        List<String> failed = new ArrayList<>();

        for (RecipeDefinitions.RecipeData recipe : RecipeDefinitions.getAllRecipes()) {
            String label = recipe.enchantment() + "_" + recipe.level();
            try {
                loadRecipe(recipe);
                loaded++;
            } catch (Exception e) {
                failed.add(label + " (" + e.getMessage() + ")");
            }
        }

        plugin.getLogger().info("Registered " + loaded + " enchanted book recipes.");
        if (!failed.isEmpty()) {
            plugin.getLogger().warning("Failed to register " + failed.size() + " recipe(s):");
            for (String f : failed) {
                plugin.getLogger().warning("  - " + f);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            discoverRecipesForPlayer(player);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanAllCraftingGrids, 1L, 1L);
    }

    private void loadRecipe(RecipeDefinitions.RecipeData recipeData) {
        Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(NamespacedKey.minecraft(recipeData.enchantment().toLowerCase()));
        if (enchantment == null) {
            throw new IllegalArgumentException("Unknown enchantment: " + recipeData.enchantment());
        }

        List<RecipeChoice> ingredients = new ArrayList<>();
        Material ncReturnMaterial = null;

        for (RecipeDefinitions.Ingredient ing : recipeData.ingredients()) {
            RecipeChoice choice = switch (ing.type()) {
                case "MATERIAL" -> {
                    Material material = Material.valueOf(ing.value());
                    if (ing.nonConsumed()) ncReturnMaterial = material;
                    yield new RecipeChoice.ExactChoice(new ItemStack(material));
                }
                case "POTION" -> createPotionChoice(PotionType.valueOf(ing.potionType()));
                case "SPLASH_POTION" -> createSplashPotionChoice(PotionType.valueOf(ing.potionType()));
                case "LINGERING_POTION" -> createLingeringPotionChoice(PotionType.valueOf(ing.potionType()));
                case "TIPPED_ARROW" -> createTippedArrowChoice(PotionType.valueOf(ing.potionType()));
                default -> throw new IllegalArgumentException("Unknown ingredient type: " + ing.type());
            };
            ingredients.add(choice);
        }

        register(enchantment.getKey().getKey() + "_" + recipeData.level(), enchantment, recipeData.level(), ingredients, ncReturnMaterial);
    }

    private RecipeChoice createPotionChoice(PotionType potionType) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(potionType);
        potion.setItemMeta(meta);
        return new RecipeChoice.ExactChoice(potion);
    }

    private RecipeChoice createSplashPotionChoice(PotionType potionType) {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(potionType);
        potion.setItemMeta(meta);
        return new RecipeChoice.ExactChoice(potion);
    }

    private RecipeChoice createLingeringPotionChoice(PotionType potionType) {
        ItemStack potion = new ItemStack(Material.LINGERING_POTION);
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

    private void cleanAllCraftingGrids() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory inv)) continue;
            applyCleanPotions(inv);
        }
    }

    private void applyCleanPotions(CraftingInventory inv) {
        if (inv.getViewers().isEmpty()) return;
        ItemStack[] matrix = inv.getMatrix();
        boolean changed = false;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) continue;
            ItemStack clean = cleanPotion(matrix[i]);
            if (clean != matrix[i]) {
                matrix[i] = clean;
                changed = true;
            }
        }
        if (changed) {
            inv.setMatrix(matrix);
        }
    }

    private ItemStack cleanPotion(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return item;
        PotionType type = meta.getBasePotionType();
        if (type == null) return item;
        if (!meta.hasLore() && !meta.hasDisplayName() && meta.getCustomEffects().isEmpty()) return item;

        ItemStack clean = new ItemStack(item.getType(), item.getAmount());
        PotionMeta cleanMeta = (PotionMeta) clean.getItemMeta();
        cleanMeta.setBasePotionType(type);
        clean.setItemMeta(cleanMeta);
        return clean;
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

    public void discoverRecipesForPlayer(Player player) {
        player.discoverRecipes(recipeKeys);
    }

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
