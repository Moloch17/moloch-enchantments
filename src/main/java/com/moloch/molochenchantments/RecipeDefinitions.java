package com.moloch.molochenchantments;

import java.util.List;

public final class RecipeDefinitions {

    public record Ingredient(String type, String value, String potionType, boolean nonConsumed) {
        static Ingredient mat(String value) { return new Ingredient("MATERIAL", value, null, false); }
        static Ingredient matNC(String value) { return new Ingredient("MATERIAL", value, null, true); }
        static Ingredient potion(String potionType) { return new Ingredient("POTION", null, potionType, false); }
        static Ingredient tippedArrow(String potionType) { return new Ingredient("TIPPED_ARROW", null, potionType, false); }
        static Ingredient splashPotion(String potionType) { return new Ingredient("SPLASH_POTION", null, potionType, false); }
        static Ingredient lingeringPotion(String potionType) { return new Ingredient("LINGERING_POTION", null, potionType, false); }
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
            )),

            // Breach
            new RecipeData("BREACH", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("LODESTONE"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("BREACH", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WIND_CHARGE"),
                Ingredient.mat("IRON_SPEAR")
            )),
            new RecipeData("BREACH", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.splashPotion("WEAKNESS"),
                Ingredient.mat("LAPIS_LAZULI")
            )),
            new RecipeData("BREACH", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("HEAVY_CORE"),
                Ingredient.mat("EXPERIENCE_BOTTLE"),
                Ingredient.mat("ECHO_SHARD")
            )),

            // Channeling
            new RecipeData("CHANNELING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PRISMARINE_CRYSTALS"),
                Ingredient.mat("LIGHTNING_ROD"),
                Ingredient.matNC("CONDUIT")
            )),

            // Density
            new RecipeData("DENSITY", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("IRON_BLOCK"),
                Ingredient.mat("BREEZE_ROD")
            )),
            new RecipeData("DENSITY", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("LODESTONE"),
                Ingredient.mat("BLAZE_ROD")
            )),
            new RecipeData("DENSITY", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.potion("STRONG_STRENGTH"),
                Ingredient.mat("REDSTONE")
            )),
            new RecipeData("DENSITY", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("NETHERITE_INGOT"),
                Ingredient.mat("NETHERITE_UPGRADE_SMITHING_TEMPLATE")
            )),
            new RecipeData("DENSITY", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("HEAVY_CORE"),
                Ingredient.mat("EXPERIENCE_BOTTLE"),
                Ingredient.mat("ECHO_SHARD"),
                Ingredient.mat("IRON_BLOCK")
            )),

            // Depth Strider
            new RecipeData("DEPTH_STRIDER", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("NAUTILUS_SHELL"),
                Ingredient.mat("SUGAR")
            )),
            new RecipeData("DEPTH_STRIDER", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("NAUTILUS_SHELL"),
                Ingredient.potion("SWIFTNESS")
            )),
            new RecipeData("DEPTH_STRIDER", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("NAUTILUS_SHELL"),
                Ingredient.mat("AMETHYST_SHARD"),
                Ingredient.mat("PRISMARINE_CRYSTALS")
            )),

            // Feather Falling
            new RecipeData("FEATHER_FALLING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("FEATHER")
            )),
            new RecipeData("FEATHER_FALLING", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WHITE_WOOL"),
                Ingredient.mat("COBWEB")
            )),
            new RecipeData("FEATHER_FALLING", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PHANTOM_MEMBRANE"),
                Ingredient.potion("SLOW_FALLING")
            )),
            new RecipeData("FEATHER_FALLING", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PHANTOM_MEMBRANE"),
                Ingredient.potion("SLOW_FALLING"),
                Ingredient.mat("WIND_CHARGE")
            )),

            // Fire Aspect
            new RecipeData("FIRE_ASPECT", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("FIRE_CHARGE"),
                Ingredient.mat("FLINT")
            )),
            new RecipeData("FIRE_ASPECT", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BLAZE_ROD"),
                Ingredient.potion("STRONG_STRENGTH")
            )),

            // Fire Protection
            new RecipeData("FIRE_PROTECTION", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SNOWBALL"),
                Ingredient.potion("WATER")
            )),
            new RecipeData("FIRE_PROTECTION", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("ICE"),
                Ingredient.potion("WATER")
            )),
            new RecipeData("FIRE_PROTECTION", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("ARMADILLO_SCUTE"),
                Ingredient.potion("THICK")
            )),
            new RecipeData("FIRE_PROTECTION", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SHROOMLIGHT"),
                Ingredient.potion("LONG_FIRE_RESISTANCE")
            )),

            // Flame
            new RecipeData("FLAME", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BLAZE_POWDER"),
                Ingredient.mat("SPECTRAL_ARROW"),
                Ingredient.mat("GUNPOWDER"),
                Ingredient.mat("ORANGE_DYE")
            )),

            // Fortune
            new RecipeData("FORTUNE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("RABBIT_FOOT"),
                Ingredient.mat("GOLDEN_DANDELION")
            )),
            new RecipeData("FORTUNE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("CHAINMAIL_CHESTPLATE"),
                Ingredient.mat("GOLDEN_APPLE")
            )),
            new RecipeData("FORTUNE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.potion("LEAPING"),
                Ingredient.mat("DIAMOND_ORE")
            )),

            // Frost Walker
            new RecipeData("FROST_WALKER", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BLUE_ICE"),
                Ingredient.mat("AMETHYST_SHARD")
            )),
            new RecipeData("FROST_WALKER", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SCULK_VEIN"),
                Ingredient.potion("THICK")
            )),

            // Impaling
            new RecipeData("IMPALING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("POINTED_DRIPSTONE"),
                Ingredient.mat("STRING")
            )),
            new RecipeData("IMPALING", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("AMETHYST_SHARD"),
                Ingredient.mat("BONE_MEAL")
            )),
            new RecipeData("IMPALING", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PRISMARINE_SHARD"),
                Ingredient.mat("LEAD")
            )),
            new RecipeData("IMPALING", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("ARMADILLO_SCUTE"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("IMPALING", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BREEZE_ROD"),
                Ingredient.mat("TWISTING_VINES")
            )),

            // Infinity
            new RecipeData("INFINITY", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SPECTRAL_ARROW"),
                Ingredient.mat("ECHO_SHARD"),
                Ingredient.mat("END_CRYSTAL"),
                Ingredient.mat("EGG")
            )),

            // Knockback
            new RecipeData("KNOCKBACK", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("FEATHER"),
                Ingredient.mat("GUNPOWDER")
            )),
            new RecipeData("KNOCKBACK", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WIND_CHARGE"),
                Ingredient.mat("SLIME_BALL")
            )),

            // Looting
            new RecipeData("LOOTING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("RABBIT_FOOT"),
                Ingredient.mat("PRISMARINE_CRYSTALS")
            )),
            new RecipeData("LOOTING", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("OMINOUS_BOTTLE"),
                Ingredient.tippedArrow("SLOWNESS")
            )),
            new RecipeData("LOOTING", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.potion("LEAPING"),
                Ingredient.mat("WITHER_SKELETON_SKULL")
            )),

            // Loyalty
            new RecipeData("LOYALTY", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("LEAD"),
                Ingredient.mat("SLIME_BALL")
            )),
            new RecipeData("LOYALTY", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GLOW_INK_SAC"),
                Ingredient.mat("AMETHYST_SHARD")
            )),
            new RecipeData("LOYALTY", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PRISMARINE_CRYSTALS"),
                Ingredient.mat("AXOLOTL_BUCKET")
            )),

            // Luck of the Sea
            new RecipeData("LUCK_OF_THE_SEA", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("TROPICAL_FISH"),
                Ingredient.mat("FIREFLY_BUSH")
            )),
            new RecipeData("LUCK_OF_THE_SEA", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("TRIPWIRE_HOOK"),
                Ingredient.mat("COPPER_CHESTPLATE")
            )),
            new RecipeData("LUCK_OF_THE_SEA", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PLENTY_POTTERY_SHERD"),
                Ingredient.mat("SPONGE")
            )),

            // Lunge
            new RecipeData("LUNGE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("POINTED_DRIPSTONE"),
                Ingredient.mat("WHITE_CARPET")
            )),
            new RecipeData("LUNGE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WIND_CHARGE"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("LUNGE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("ECHO_SHARD"),
                Ingredient.mat("BREEZE_ROD")
            )),

            // Lure
            new RecipeData("LURE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GOLDEN_DANDELION"),
                Ingredient.mat("STRING")
            )),
            new RecipeData("LURE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GLOW_BERRIES"),
                Ingredient.mat("LEVER")
            )),
            new RecipeData("LURE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("CRIMSON_FUNGUS"),
                Ingredient.mat("TRIPWIRE_HOOK")
            )),

            // Mending
            new RecipeData("MENDING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.lingeringPotion("LONG_REGENERATION"),
                Ingredient.mat("ECHO_SHARD"),
                Ingredient.matNC("DRAGON_EGG"),
                Ingredient.mat("POPPED_CHORUS_FRUIT"),
                Ingredient.mat("EXPERIENCE_BOTTLE"),
                Ingredient.mat("GLISTERING_MELON_SLICE"),
                Ingredient.mat("POISONOUS_POTATO")
            )),

            // Multishot
            new RecipeData("MULTISHOT", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SPECTRAL_ARROW"),
                Ingredient.mat("SPECTRAL_ARROW"),
                Ingredient.mat("ENDER_EYE"),
                Ingredient.mat("TORCHFLOWER_SEEDS")
            )),

            // Piercing
            new RecipeData("PIERCING", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("IRON_NUGGET"),
                Ingredient.mat("BONE")
            )),
            new RecipeData("PIERCING", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("AMETHYST_SHARD"),
                Ingredient.mat("IRON_BARS")
            )),
            new RecipeData("PIERCING", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PRISMARINE_SHARD"),
                Ingredient.mat("BREEZE_ROD")
            )),
            new RecipeData("PIERCING", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("QUARTZ"),
                Ingredient.mat("BLAZE_ROD")
            )),

            // Projectile Protection
            new RecipeData("PROJECTILE_PROTECTION", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("PHANTOM_MEMBRANE")
            )),
            new RecipeData("PROJECTILE_PROTECTION", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SHIELD"),
                Ingredient.mat("ENDER_PEARL")
            )),
            new RecipeData("PROJECTILE_PROTECTION", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SHIELD"),
                Ingredient.matNC("CONDUIT")
            )),
            new RecipeData("PROJECTILE_PROTECTION", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SHIELD"),
                Ingredient.matNC("CONDUIT"),
                Ingredient.mat("WIND_CHARGE")
            )),

            // Punch
            new RecipeData("PUNCH", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GUNPOWDER"),
                Ingredient.mat("SLIME_BALL")
            )),
            new RecipeData("PUNCH", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("TORCHFLOWER_SEEDS"),
                Ingredient.mat("CLAY_BALL")
            )),

            // Respiration
            new RecipeData("RESPIRATION", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GLOW_LICHEN"),
                Ingredient.mat("PUFFERFISH")
            )),
            new RecipeData("RESPIRATION", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("CONDUIT"),
                Ingredient.mat("WIND_CHARGE")
            )),
            new RecipeData("RESPIRATION", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("CONDUIT"),
                Ingredient.mat("DRIED_GHAST"),
                Ingredient.potion("WATER_BREATHING")
            )),

            // Riptide
            new RecipeData("RIPTIDE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PRISMARINE_CRYSTALS"),
                Ingredient.splashPotion("MUNDANE")
            )),
            new RecipeData("RIPTIDE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SOUL_SAND"),
                Ingredient.mat("BUBBLE_CORAL")
            )),
            new RecipeData("RIPTIDE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("DAYLIGHT_DETECTOR"),
                Ingredient.potion("STRONG_LEAPING")
            )),

            // Silk Touch
            new RecipeData("SILK_TOUCH", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("FEATHER"),
                Ingredient.mat("WHITE_DYE"),
                Ingredient.mat("COBWEB"),
                Ingredient.mat("GLOW_LICHEN")
            )),

            // Smite
            new RecipeData("SMITE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("ROTTEN_FLESH"),
                Ingredient.mat("GUNPOWDER")
            )),
            new RecipeData("SMITE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BONE"),
                Ingredient.mat("RESIN_CLUMP")
            )),
            new RecipeData("SMITE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("DEAD_BRAIN_CORAL"),
                Ingredient.mat("NETHER_WART")
            )),
            new RecipeData("SMITE", 4, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("PITCHER_POD"),
                Ingredient.mat("GHAST_TEAR")
            )),
            new RecipeData("SMITE", 5, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WITHER_SKELETON_SKULL"),
                Ingredient.mat("GOLDEN_APPLE")
            )),

            // Soul Speed
            new RecipeData("SOUL_SPEED", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SOUL_SAND"),
                Ingredient.mat("SUGAR")
            )),
            new RecipeData("SOUL_SPEED", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SOUL_CAMPFIRE"),
                Ingredient.mat("GOLDEN_CARROT")
            )),
            new RecipeData("SOUL_SPEED", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("SOUL_LANTERN"),
                Ingredient.potion("STRONG_SWIFTNESS")
            )),

            // Sweeping Edge
            new RecipeData("SWEEPING_EDGE", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("GUNPOWDER"),
                Ingredient.mat("BAMBOO")
            )),
            new RecipeData("SWEEPING_EDGE", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("AMETHYST_SHARD"),
                Ingredient.mat("NETHER_WART")
            )),
            new RecipeData("SWEEPING_EDGE", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("FLOW_BANNER_PATTERN"),
                Ingredient.mat("QUARTZ"),
                Ingredient.potion("STRENGTH")
            )),

            // Swift Sneak
            new RecipeData("SWIFT_SNEAK", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("SUGAR"),
                Ingredient.mat("WHITE_WOOL")
            )),
            new RecipeData("SWIFT_SNEAK", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("CAKE"),
                Ingredient.mat("TURTLE_SCUTE")
            )),
            new RecipeData("SWIFT_SNEAK", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.potion("STRONG_SWIFTNESS"),
                Ingredient.mat("SCULK_VEIN")
            )),

            // Thorns
            new RecipeData("THORNS", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("IRON_NUGGET"),
                Ingredient.mat("GREEN_DYE")
            )),
            new RecipeData("THORNS", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("POINTED_DRIPSTONE"),
                Ingredient.mat("RESIN_CLUMP")
            )),
            new RecipeData("THORNS", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("BLAZE_ROD"),
                Ingredient.mat("HORN_CORAL")
            )),

            // Wind Burst
            new RecipeData("WIND_BURST", 1, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.mat("WIND_CHARGE"),
                Ingredient.mat("PHANTOM_MEMBRANE"),
                Ingredient.mat("FEATHER")
            )),
            new RecipeData("WIND_BURST", 2, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.splashPotion("WIND_CHARGED"),
                Ingredient.mat("PRISMARINE_SHARD")
            )),
            new RecipeData("WIND_BURST", 3, List.of(
                Ingredient.mat("BOOK"),
                Ingredient.mat("LAPIS_LAZULI"),
                Ingredient.mat("GOLD_INGOT"),
                Ingredient.matNC("GUSTER_BANNER_PATTERN"),
                Ingredient.potion("STRONG_LEAPING"),
                Ingredient.mat("EXPERIENCE_BOTTLE")
            ))
        );
    }

    private RecipeDefinitions() {}
}
