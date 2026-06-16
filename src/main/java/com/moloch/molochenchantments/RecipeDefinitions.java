package com.moloch.molochenchantments;

import java.util.List;

public final class RecipeDefinitions {

    public record Ingredient(String type, String value, String potionType, boolean nonConsumed) {
        static Ingredient mat(String value) { return new Ingredient("MATERIAL", value, null, false); }
        static Ingredient matNC(String value) { return new Ingredient("MATERIAL", value, null, true); }
        static Ingredient potion(String potionType) { return new Ingredient("POTION", null, potionType, false); }
        static Ingredient tippedArrow(String potionType) { return new Ingredient("TIPPED_ARROW", null, potionType, false); }
    }

    public record RecipeData(String enchantment, int level, List<Ingredient> ingredients) {}

    static List<RecipeData> getAllRecipes() {
        return List.of(
            // Aqua Affinity
            new RecipeData("AQUA_AFFINITY", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.matNC("HEART_OF_THE_SEA"),
                Ingredient.mat("NAUTILUS_SHELL")
            )),

            // Bane of Arthropods
            new RecipeData("BANE_OF_ARTHROPODS", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SPIDER_EYE"),
                Ingredient.mat("STRING")
            )),
            new RecipeData("BANE_OF_ARTHROPODS", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SPIDER_EYE"),
                Ingredient.mat("COBWEB")
            )),
            new RecipeData("BANE_OF_ARTHROPODS", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SPIDER_EYE"),
                Ingredient.mat("ENDER_PEARL")
            )),
            new RecipeData("BANE_OF_ARTHROPODS", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("FERMENTED_SPIDER_EYE"),
                Ingredient.mat("ENDER_EYE")
            )),
            new RecipeData("BANE_OF_ARTHROPODS", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("FERMENTED_SPIDER_EYE"),
                Ingredient.potion("WEAVING")
            )),

            // Blast Protection
            new RecipeData("BLAST_PROTECTION", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("COPPER_BLOCK")
            )),
            new RecipeData("BLAST_PROTECTION", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("IRON_BLOCK")
            )),
            new RecipeData("BLAST_PROTECTION", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("ANVIL")
            )),
            new RecipeData("BLAST_PROTECTION", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("DIAMOND_BLOCK")
            )),

            // Efficiency
            new RecipeData("EFFICIENCY", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("AMETHYST_SHARD")
            )),
            new RecipeData("EFFICIENCY", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("QUARTZ")
            )),
            new RecipeData("EFFICIENCY", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BLAZE_POWDER")
            )),
            new RecipeData("EFFICIENCY", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.potion("STRONG_SWIFTNESS")
            )),
            new RecipeData("EFFICIENCY", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("DIAMOND"),
                Ingredient.mat("ECHO_SHARD")
            )),

            // Power
            new RecipeData("POWER", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("IRON_INGOT"),
                Ingredient.mat("STRING")
            )),
            new RecipeData("POWER", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("REDSTONE"),
                Ingredient.mat("LEATHER")
            )),
            new RecipeData("POWER", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SPYGLASS"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("POWER", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("BAMBOO"),
                Ingredient.mat("TWISTING_VINES")
            )),
            new RecipeData("POWER", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SCULK_VEIN"),
                Ingredient.tippedArrow("SWIFTNESS")
            )),

            // Protection
            new RecipeData("PROTECTION", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("LEATHER"),
                Ingredient.mat("POPPY")
            )),
            new RecipeData("PROTECTION", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("PHANTOM_MEMBRANE"),
                Ingredient.mat("NAUTILUS_SHELL")
            )),
            new RecipeData("PROTECTION", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.potion("SLOWNESS"),
                Ingredient.mat("EXPERIENCE_BOTTLE")
            )),
            new RecipeData("PROTECTION", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("NETHERITE_UPGRADE_SMITHING_TEMPLATE"),
                Ingredient.mat("ANCIENT_DEBRIS")
            )),

            // Quick Charge
            new RecipeData("QUICK_CHARGE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("SUGAR"),
                Ingredient.mat("RESIN_CLUMP")
            )),
            new RecipeData("QUICK_CHARGE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("EXPERIENCE_BOTTLE"),
                Ingredient.matNC("FLOW_BANNER_PATTERN")
            )),
            new RecipeData("QUICK_CHARGE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.potion("SWIFTNESS"),
                Ingredient.mat("ENDER_EYE")
            )),

            // Sharpness
            new RecipeData("SHARPNESS", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("FLINT"),
                Ingredient.mat("BONE_MEAL")
            )),
            new RecipeData("SHARPNESS", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("AMETHYST_SHARD"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("SHARPNESS", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("DIAMOND"),
                Ingredient.mat("WEEPING_VINES")
            )),
            new RecipeData("SHARPNESS", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.matNC("BLADE_POTTERY_SHERD"),
                Ingredient.mat("BLAZE_POWDER"),
                Ingredient.mat("TORCHFLOWER_SEEDS")
            )),
            new RecipeData("SHARPNESS", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("END_ROD"),
                Ingredient.potion("STRONG_STRENGTH")
            )),

            // Unbreaking
            new RecipeData("UNBREAKING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("OBSIDIAN"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("UNBREAKING", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("MANGROVE_ROOTS"),
                Ingredient.mat("MAGMA_CREAM")
            )),
            new RecipeData("UNBREAKING", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.matNC("TOTEM_OF_UNDYING"),
                Ingredient.mat("END_CRYSTAL"),
                Ingredient.mat("TURTLE_SCUTE")
            ))
        );
    }

    private RecipeDefinitions() {}
}
