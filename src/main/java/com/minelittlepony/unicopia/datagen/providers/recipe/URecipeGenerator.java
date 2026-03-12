package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellDuplicatingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.datagen.providers.recipe.BedSheetPatternRecipeBuilder.PatternTemplate;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.GlowingRecipe;
import com.minelittlepony.unicopia.recipe.JarExtractRecipe;
import com.minelittlepony.unicopia.recipe.JarInsertRecipe;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.family.BlockFamily.Variant;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class URecipeGenerator extends RecipeGenerator implements CraftingMaterialHelper {
    private static final List<Item> WOOLS = List.of(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL, Items.GRAY_WOOL, Items.GREEN_WOOL, Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL, Items.PURPLE_WOOL, Items.RED_WOOL, Items.YELLOW_WOOL, Items.WHITE_WOOL);

    private final RegistryEntryLookup<Item> items;

    private final RecipeExporter farmersDelightExporter;

    URecipeGenerator(WrapperLookup registries, RecipeExporter exporter, RecipeExporter farmersDelightExporter) {
        super(registries, exporter);
        items = registries.getOrThrow(RegistryKeys.ITEM);
        this.farmersDelightExporter = farmersDelightExporter;
    }

    @Override
    public void generate() {
        offerJarRecipes();
        offerWoodBlocksRecipes();
        offerChitinBlocksRecipes();
        offerCloudRecipes();
        offerFoodRecipes();
        offerGemstoneAndMagicRecipes();
        offerMagicSpellRecipes();
        offerSeaponyRecipes();
        offerEarthPonyRecipes();

        // beds
        createCustomBedRecipe(UItems.CLOUD_BED, Either.left(UBlocks.DENSE_CLOUD), Either.left(UBlocks.CLOUD_PLANKS)).offerTo(exporter);
        createCustomBedRecipe(UItems.CLOTH_BED, Either.right(ItemTags.WOOL), Either.right(ItemTags.LOGS)).offerTo(exporter);
        offerBedSheetRecipes();

        // sunglasses
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.SUNGLASSES)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass_block", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("##")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.SUNGLASSES)
            .input(ConventionalItemTags.GLASS_BLOCKS)
            .input(UItems.SUNGLASSES).criterion("has_broken_sunglasses", conditionsFromItem(UItems.BROKEN_SUNGLASSES))
            .offerTo(exporter, convertBetween(UItems.SUNGLASSES, UItems.BROKEN_SUNGLASSES));

        // farmers delight
        offerFarmersDelightCuttingRecipes();
    }

    private void offerJarRecipes() {
        ComplexRecipeJsonBuilder.create(JarExtractRecipe::new).offerTo(exporter, "empty_jar_from_filled_jar");
        ComplexRecipeJsonBuilder.create(JarInsertRecipe::new).offerTo(exporter, "filled_jar");
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.EMPTY_JAR, 7)
            .input('#', ItemTags.PLANKS)
            .input('*', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("*#*")
            .pattern("* *")
            .pattern("***")
            .offerTo(exporter);
    }

    private void offerCloudRecipes() {
        offerShapelessRecipe(UItems.CLOUD_LUMP, UTags.Items.CLOUD_JARS, "cloud", 4);
        generateFamily(UBlockFamilies.CLOUD, FeatureSet.of(FeatureFlags.VANILLA));
        offer2x3Recipe(UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, "pillar");
        offer2x2CompactingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD, UItems.CLOUD_LUMP);
        offerPolishedStoneRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        generateFamily(UBlockFamilies.CLOUD_PLANKS, FeatureSet.of(FeatureFlags.VANILLA));
        offerChestRecipe(UBlocks.CLOUD_CHEST, UBlocks.CLOUD_PLANKS);

        offer2x2CompactingRecipe(RecipeCategory.DECORATIONS, UBlocks.SHAPING_BENCH, UBlocks.DENSE_CLOUD);
        generateFamily(UBlockFamilies.CLOUD_BRICKS, FeatureSet.of(FeatureFlags.VANILLA));

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CARVED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PILLAR, UBlocks.CLOUD);

        // TODO: Cut Cloud, Smooth Cloud, Polished Cloud, Raked Cloud

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_SLAB, UBlocks.CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_STAIRS, UBlocks.CLOUD);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_SLAB, UBlocks.CLOUD_BRICKS, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_STAIRS, UBlocks.CLOUD_BRICKS);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_PLANKS, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_PLANKS);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_SLAB, UBlocks.DENSE_CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_STAIRS, UBlocks.DENSE_CLOUD);

        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_SLAB, UBlocks.ETCHED_CLOUD, 2);
        offerCloudShapingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_STAIRS, UBlocks.ETCHED_CLOUD);

        offerCompactingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD, UBlocks.CLOUD, 4);
        generateFamily(UBlockFamilies.DENSE_CLOUD, FeatureSet.of(FeatureFlags.VANILLA));
        offer2x3Recipe(UBlocks.CLOUD_DOOR, UBlocks.DENSE_CLOUD, "door");

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, UBlocks.UNSTABLE_CLOUD, 8)
            .input(UBlocks.CLOUD, 8)
            .input(Ingredient.ofItems(UItems.LIGHTNING_JAR, UItems.ZAP_APPLE_JAM_JAR))
            .criterion("has_lightning_jar", conditionsFromItem(UItems.LIGHTNING_JAR))
            .criterion("has_zap_jar", conditionsFromItem(UItems.ZAP_APPLE_JAM_JAR))
            .offerTo(exporter);
    }

    private void offerWoodBlocksRecipes() {
        // palm wood
        generateFamily(UBlockFamilies.PALM, FeatureSet.of(FeatureFlags.VANILLA));
        offerPlanksRecipe(UBlocks.PALM_PLANKS, UTags.Items.PALM_LOGS, 4);
        offerBarkBlockRecipe(UBlocks.PALM_WOOD, UBlocks.PALM_LOG);
        offerBarkBlockRecipe(UBlocks.STRIPPED_PALM_WOOD, UBlocks.STRIPPED_PALM_LOG);
        offerBoatRecipe(UItems.PALM_BOAT, UBlocks.PALM_PLANKS);
        offerChestBoatRecipe(UItems.PALM_CHEST_BOAT, UItems.PALM_BOAT);
        offerHangingSignRecipe(UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_PLANKS);

        // zap wood
        generateFamily(UBlockFamilies.ZAP, FeatureSet.of(FeatureFlags.VANILLA));
        offerPlanksRecipe(UBlocks.ZAP_PLANKS, UTags.Items.ZAP_LOGS, 4);
        offerBarkBlockRecipe(UBlocks.ZAP_WOOD, UBlocks.ZAP_LOG);
        offerBarkBlockRecipe(UBlocks.STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG);

        // waxed zap wood
        offerPlanksRecipe(UBlocks.WAXED_ZAP_PLANKS, UTags.Items.WAXED_ZAP_LOGS, 4);
        offerBarkBlockRecipe(UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_ZAP_LOG);
        generateFamily(UBlockFamilies.WAXED_ZAP, FeatureSet.of(FeatureFlags.VANILLA));
        offerBarkBlockRecipe(UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.WAXED_STRIPPED_ZAP_LOG);

        // golden oak wood
        generateFamily(UBlockFamilies.GOLDEN_OAK, FeatureSet.of(FeatureFlags.VANILLA));
        offerPlanksRecipe(UBlocks.GOLDEN_OAK_PLANKS, UTags.Items.GOLDEN_OAK_LOGS, 4);
        offerBarkBlockRecipe(UBlocks.GOLDEN_OAK_WOOD, UBlocks.GOLDEN_OAK_LOG);
        offerBarkBlockRecipe(UBlocks.STRIPPED_GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_LOG);

        offerSmelting(List.of(UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_WOOD, UItems.GOLDEN_STICK), RecipeCategory.FOOD, Items.GOLD_NUGGET, 20, 200, "gold_nugget");

        offerWaxingRecipes();

        // other doors
        offer2x3Recipe(UBlocks.CRYSTAL_DOOR, UItems.CRYSTAL_SHARD, "door");
        offerStableDoorRecipe(UBlocks.STABLE_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK_CANDY);
        offerStableDoorRecipe(UBlocks.DARK_OAK_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK);
    }

    private void offerChitinBlocksRecipes() {
        offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, UItems.CARAPACE, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHITIN);
        offerPolishedStoneRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);

        generateFamily(UBlockFamilies.CHISELED_CHITIN, FeatureSet.of(FeatureFlags.VANILLA));
        generateFamily(UBlockFamilies.POLISHED_CHITIN, FeatureSet.of(FeatureFlags.VANILLA));
        offerHiveRecipe(UBlocks.HIVE, UBlocks.CHITIN, UBlocks.MYSTERIOUS_EGG);
        offerHullRecipe(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);
        offerHullRecipe(UBlocks.POLISHED_CHITIN_HULL, UBlocks.POLISHED_CHITIN, UBlocks.CHITIN);
        offerSpikesRecipe(UBlocks.CHITIN_SPIKES, UBlocks.CHITIN);

        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN, UBlocks.CHISELLED_CHITIN);
        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN);
        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CHISELLED_CHITIN, 2);
        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_STAIRS, UBlocks.CHISELLED_CHITIN);
        offerStonecuttingRecipe(RecipeCategory.DECORATIONS, UBlocks.CHISELLED_CHITIN_WALL, UBlocks.CHISELLED_CHITIN);

        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_HULL, UBlocks.POLISHED_CHITIN);
        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_SLAB, UBlocks.POLISHED_CHITIN, 2);
        offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, UBlocks.POLISHED_CHITIN_STAIRS, UBlocks.POLISHED_CHITIN);
        offerStonecuttingRecipe(RecipeCategory.DECORATIONS, UBlocks.POLISHED_CHITIN_WALL, UBlocks.POLISHED_CHITIN);
    }

    private void offerGemstoneAndMagicRecipes() {
        offerShapelessRecipe(UItems.CRYSTAL_SHARD, Items.DIAMOND, "crystal_shard", 6);
        offerShapelessRecipe(UItems.CRYSTAL_SHARD, Items.AMETHYST_SHARD, "crystal_shard", 3);
        offer2x2CompactingRecipe(RecipeCategory.MISC, UItems.GEMSTONE, UItems.CRYSTAL_SHARD);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.SPELLBOOK)
            .input(Items.BOOK).criterion("has_book", conditionsFromItem(Items.BOOK))
            .input(UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .offerTo(exporter);

        // magic staff
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, UItems.MAGIC_STAFF)
            .input('*', UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .input('#', ConventionalItemTags.WOODEN_RODS).criterion("has_stick", conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("  *")
            .pattern(" # ")
            .pattern("#  ")
            .offerTo(exporter);

        // crystal heart
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, UItems.CRYSTAL_HEART)
            .input('#', UItems.CRYSTAL_SHARD).criterion("has_crystal_shard", conditionsFromItem(UItems.CRYSTAL_SHARD))
            .pattern("# #")
            .pattern("###")
            .pattern(" # ")
            .offerTo(exporter);

        // pegasus amulet
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.GOLDEN_FEATHER)
            .input('*', Items.GOLD_NUGGET).criterion("has_nugget", conditionsFromItem(Items.GOLD_NUGGET))
            .input('#', UTags.Items.MAGIC_FEATHERS).criterion("has_feather", conditionsFromTag(UTags.Items.MAGIC_FEATHERS))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .offerTo(exporter);
        offerCompactingRecipe(RecipeCategory.COMBAT, UItems.GOLDEN_WING, UItems.GOLDEN_FEATHER);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, UItems.PEGASUS_AMULET)
            .input('*', UItems.GOLDEN_WING).criterion("has_wing", conditionsFromItem(UItems.GOLDEN_WING))
            .input('#', UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .pattern("*#*")
            .offerTo(exporter);

        // friendship bracelet
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, UItems.FRIENDSHIP_BRACELET)
            .input('*', Items.STRING)
            .input('#', Items.LEATHER).criterion(hasItem(Items.LEATHER), conditionsFromItem(Items.LEATHER))
            .pattern("*#*")
            .pattern("# #")
            .pattern("*#*")
            .offerTo(exporter);
        ComplexRecipeJsonBuilder.create(GlowingRecipe::new).offerTo(exporter, "friendship_bracelet_glowing");

        // meadowbrook's staff
        SpellShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, UItems.MAGIC_STAFF)
            .input('*', UItems.GEMSTONE).criterion(hasItem(UItems.GEMSTONE), conditionsFromItem(UItems.GEMSTONE))
            .input('/', ConventionalItemTags.WOODEN_RODS).criterion(hasItem(Items.STICK), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("  *")
            .pattern(" / ")
            .pattern("/  ")
            .offerTo(exporter);
        offerShapelessRecipe(Items.STICK, UItems.MEADOWBROOKS_STAFF, "stick", 2);
    }

    private void offerMagicSpellRecipes() {
        offerSpell(exporter, UItems.GEMSTONE, SpellType.DISPLACEMENT, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 10).with(Trait.CHAOS, 10));
        offerSpell(exporter, UItems.GEMSTONE, SpellType.FROST, new SpellTraits.Builder().with(Trait.ICE, 10));
        offerSpell(exporter, UItems.GEMSTONE, SpellType.SCORCH, new SpellTraits.Builder().with(Trait.FIRE, 10));
        offerSpell(exporter, UItems.GEMSTONE, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.STRENGTH, 10).with(Trait.FOCUS, 6).with(Trait.POWER, 10));
        offerSpell(exporter, UItems.GEMSTONE, SpellType.TRANSFORMATION, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 10).with(Trait.LIFE, 10).with(Trait.CHAOS, 4));

        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.ARCANE_PROTECTION, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 18).with(Trait.DARKNESS, 1));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.BUBBLE, SpellType.CATAPULT, new SpellTraits.Builder().with(Trait.WATER, 9).with(Trait.AIR, 9));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.CATAPULT, SpellType.FLAME, new SpellTraits.Builder().with(Trait.FOCUS, 9).with(Trait.AIR, 9));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.CHILLING_BREATH, SpellType.FROST, new SpellTraits.Builder().with(Trait.ICE, 5).with(Trait.KNOWLEDGE, 10));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.DARK_VORTEX, SpellType.VORTEX, new SpellTraits.Builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.DARKNESS, 9).with(Trait.CHAOS, 8));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.FEATHER_FALL, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 20).with(Trait.LIFE, 10).with(Trait.CHAOS, 4).with(Trait.GENEROSITY, 10));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.FIRE_BOLT, SpellType.FLAME, new SpellTraits.Builder().with(Trait.FOCUS, 9).with(Trait.FIRE, 30));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.FLAME, SpellType.SCORCH, new SpellTraits.Builder().with(Trait.FIRE, 15));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.INFERNAL, SpellType.FLAME, new SpellTraits.Builder().with(Trait.FIRE, 50).with(Trait.DARKNESS, 10));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.LIGHT, SpellType.FIRE_BOLT, new SpellTraits.Builder().with(Trait.ICE, 30).with(Trait.LIFE, 30).with(Trait.FOCUS, 10));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.MIMIC, SpellType.TRANSFORMATION, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 19).with(Trait.LIFE, 10).with(Trait.CHAOS, 4));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.MIND_SWAP, SpellType.MIMIC, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 19).with(Trait.LIFE, 10).with(Trait.CHAOS, 40));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.NECROMANCY, SpellType.SIPHONING, new SpellTraits.Builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.DARKNESS, 19).with(Trait.CHAOS, 8).with(Trait.BLOOD, 10).with(Trait.POISON, 9));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.REVEALING, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 18).with(Trait.LIFE, 1).with(Trait.ORDER, 4));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.SIPHONING, SpellType.INFERNAL, new SpellTraits.Builder().with(Trait.BLOOD, 8).with(Trait.POISON, 10));
        offerSpellFromSpell(exporter, UItems.GEMSTONE, SpellType.VORTEX, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.STRENGTH, 10).with(Trait.KNOWLEDGE, 8).with(Trait.AIR, 9));

        offerSpellFromTwoSpells(exporter, UItems.GEMSTONE, SpellType.DISPEL_EVIL, SpellType.ARCANE_PROTECTION, SpellType.DISPLACEMENT, new SpellTraits.Builder().with(Trait.KINDNESS, 1).with(Trait.POWER, 1));
        offerSpellFromTwoSpells(exporter, UItems.GEMSTONE, SpellType.HYDROPHOBIC, SpellType.FROST, SpellType.SHIELD, new SpellTraits.Builder().with(Trait.FOCUS, 6));
        offerSpellFromTwoSpells(exporter, UItems.GEMSTONE, SpellType.PORTAL, SpellType.DISPLACEMENT, SpellType.DARK_VORTEX, new SpellTraits.Builder().with(Trait.KNOWLEDGE, 18).with(Trait.CHAOS, 20));

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.ALICORN_AMULET, SpellType.EMPTY_KEY)
            .base(UItems.GEMSTONE, SpellType.DARK_VORTEX)
            .traits(new SpellTraits.Builder().with(Trait.DARKNESS, 30).with(Trait.POWER, 30).with(Trait.BLOOD, 30))
            .offerTo(exporter, "alicorn_amulet");

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.UNICORN_AMULET, SpellType.EMPTY_KEY)
            .base(UItems.BROKEN_ALICORN_AMULET, SpellType.EMPTY_KEY)
            .input(UItems.PEGASUS_AMULET, SpellType.EMPTY_KEY)
            .input(UItems.CRYSTAL_HEART, SpellType.EMPTY_KEY)
            .input(UItems.GROGARS_BELL, SpellType.EMPTY_KEY)
            .input(Items.TOTEM_OF_UNDYING, SpellType.EMPTY_KEY)
            .traits(new SpellTraits.Builder())
            .criterion(hasItem(UItems.BROKEN_ALICORN_AMULET), conditionsFromItem(UItems.BROKEN_ALICORN_AMULET))
            .offerTo(exporter, "unicorn_amulet");

        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.DRAGON_BREATH_SCROLL, SpellType.EMPTY_KEY)
            .base(Items.PAPER, SpellType.EMPTY_KEY)
            .input(Items.PAPER, SpellType.EMPTY_KEY)
            .traits(new SpellTraits.Builder().with(Trait.FIRE, 1))
            .offerTo(exporter, "dragon_breath_scroll");

        ComplexSpellcraftingRecipeJsonBuilder.create(SpellDuplicatingRecipe::new, UItems.BOTCHED_GEM).offerTo(exporter, "spell_duplicating");
        ComplexSpellcraftingRecipeJsonBuilder.create(SpellEnhancingRecipe::new, UItems.BOTCHED_GEM).offerTo(exporter, "trait_combining_botched_gem");
        ComplexSpellcraftingRecipeJsonBuilder.create(SpellEnhancingRecipe::new, UItems.GEMSTONE).offerTo(exporter, "trait_combining_gemstone");

        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.SPECTRAL_CLOCK)
            .input(Items.CLOCK).criterion("has_clock", conditionsFromItem(Items.CLOCK))
            .offerTo(exporter);
        AltarRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.TOTEM_OF_DYING)
            .input(Items.TOTEM_OF_UNDYING).criterion("has_totem", conditionsFromItem(Items.TOTEM_OF_UNDYING))
            .offerTo(exporter);
    }

    private void offerFoodRecipes() {
        offerShapelessRecipe(UItems.PINEAPPLE_CROWN, UItems.PINEAPPLE, "seeds", 1);
        offerShapelessRecipe(UItems.SWEET_APPLE_SEEDS, UItems.SWEET_APPLE, "seeds", 3);
        offerShapelessRecipe(UItems.SOUR_APPLE_SEEDS, UItems.SOUR_APPLE, "seeds", 3);
        offerShapelessRecipe(UItems.GREEN_APPLE_SEEDS, UItems.GREEN_APPLE, "seeds", 3);
        offerShapelessRecipe(UItems.GOLDEN_OAK_SEEDS, Items.GOLDEN_APPLE, "seeds", 1);
        offerPieRecipe(UItems.APPLE_PIE, UItems.APPLE_PIE_SLICE, Items.WHEAT, UTags.Items.FRESH_APPLES);

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.ROCK_STEW)
            .input(UItems.ROCK, 3).criterion(hasItem(UItems.ROCK), conditionsFromItem(UItems.ROCK))
            .input(Items.BOWL)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.BOWL_OF_NUTS)
            .input(UItems.ACORN, 3).criterion(hasItem(UItems.ACORN), conditionsFromItem(UItems.ACORN))
            .input(Items.BOWL)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.OATMEAL_COOKIE)
            .input(UItems.OATS, 2).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.CHOCOLATE_OATMEAL_COOKIE)
            .input(UItems.OATS, 2)
            .input(Items.COCOA_BEANS).criterion(hasItem(Items.COCOA_BEANS), conditionsFromItem(Items.COCOA_BEANS))
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.PINECONE_COOKIE)
            .input(UItems.PINECONE).criterion(hasItem(UItems.PINECONE), conditionsFromItem(UItems.PINECONE))
            .input(Items.WHEAT, 2)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.SCONE)
            .input(UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .input(Items.WHEAT, 2)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.ROCK_CANDY, 3)
            .input(Items.SUGAR, 6).criterion(hasItem(Items.SUGAR), conditionsFromItem(Items.SUGAR))
            .input(UItems.PEBBLES, 3)
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, Items.BREAD)
            .input('#', UItems.OATS).criterion("has_oats", conditionsFromItem(UItems.OATS))
            .pattern("###")
            .offerTo(exporter, convertBetween(Items.BREAD, UItems.OATS));
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.JUICE)
            .input(Ingredient.fromTag(items.getOrThrow(UTags.Items.FRESH_APPLES)), 6).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .input(Items.GLASS_BOTTLE)
            .group("juice")
            .offerTo(exporter);
        appendIngredients(ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.MUFFIN), Items.SUGAR, Items.EGG, Items.POTATO, UItems.JUICE, UItems.WHEAT_WORMS).offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.MUG)
            .input('*', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .input('#', ConventionalItemTags.WOODEN_RODS).criterion(hasItem(Items.STICK), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
            .pattern("# #")
            .pattern("* *")
            .pattern(" # ")
            .offerTo(exporter);
        appendIngredients(ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.CIDER), UItems.BURNED_JUICE, UItems.MUG)
            .input(Ingredient.fromTag(items.getOrThrow(UTags.Items.FRESH_APPLES))).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.HAY_FRIES)
            .input('#', UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .pattern("#")
            .pattern("#")
            .pattern("#")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.HAY_BURGER)
            .input('~', Items.BREAD).criterion(hasItem(Items.BREAD), conditionsFromItem(Items.BREAD))
            .input('#', UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.DAFFODIL_DAISY_SANDWICH)
            .input('#', Items.BREAD).criterion(hasItem(Items.BREAD), conditionsFromItem(Items.BREAD))
            .input('~', ItemTags.SMALL_FLOWERS).criterion("has_flower", conditionsFromTag(ItemTags.SMALL_FLOWERS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.HORSE_SHOE_FRIES, 15)
            .input('#', Items.BAKED_POTATO).criterion(hasItem(Items.BAKED_POTATO), conditionsFromItem(Items.BAKED_POTATO))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.OATMEAL)
            .input(UItems.OATS, 3).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .input(ConventionalItemTags.MILK_BUCKETS)
            .input(Items.BOWL)
            .offerTo(exporter);

        offerSmelting(List.of(UItems.JUICE), RecipeCategory.FOOD, UItems.BURNED_JUICE, 0, 100, "juice");
        offerSmelting(List.of(Items.BREAD), RecipeCategory.FOOD, UItems.TOAST, 0.2F, 430, "bread");
        offerSmelting(List.of(UItems.TOAST), RecipeCategory.FOOD, UItems.BURNED_TOAST, 0.2F, 30, "bread");
        offerSmelting(List.of(UItems.BURNED_JUICE, UItems.BURNED_TOAST), RecipeCategory.FOOD, Items.CHARCOAL, 1, 20, "coal");
        offerSmelting(List.of(UItems.HAY_FRIES), RecipeCategory.FOOD, UItems.CRISPY_HAY_FRIES, 1F, 25, "hay_fries");
        offerSmelting(List.of(UItems.ZAP_APPLE), RecipeCategory.FOOD, UItems.COOKED_ZAP_APPLE, 1.2F, 430, "zap_apple");
        offerSmelting(List.of(Items.TROPICAL_FISH), RecipeCategory.FOOD, UItems.COOKED_TROPICAL_FISH, 1.2F, 230, "fish");
        offerSmelting(List.of(Items.PUFFERFISH), RecipeCategory.FOOD, UItems.COOKED_PUFFERFISH, 1.2F, 230, "fish");
        offerSmelting(List.of(Items.AXOLOTL_BUCKET), RecipeCategory.FOOD, UItems.FRIED_AXOLOTL, 2.2F, 230, "fried_axolotl");
        offerSmelting(List.of(UItems.FROG_LEGS), RecipeCategory.FOOD, UItems.COOKED_FROG_LEGS, 2.2F, 10, "frog_legs");
        offerSmelting(List.of(UBlocks.MYSTERIOUS_EGG.asItem()), RecipeCategory.FOOD, UItems.GREEN_FRIED_EGG, 3.8F, 630, "fried_egg");
        generateCookingRecipes("smoking", RecipeSerializer.SMOKING, SmokingRecipe::new, 100);
        generateCookingRecipes("campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING, CampfireCookingRecipe::new, 600);

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.ZAP_APPLE_JAM_JAR)
            .input(UItems.COOKED_ZAP_APPLE, 6).criterion(hasItem(UItems.COOKED_ZAP_APPLE), conditionsFromItem(UItems.COOKED_ZAP_APPLE))
            .input(UItems.EMPTY_JAR)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.JAM_TOAST, 8)
            .input(UItems.ZAP_APPLE_JAM_JAR).criterion(hasItem(UItems.ZAP_APPLE_JAM_JAR), conditionsFromItem(UItems.ZAP_APPLE_JAM_JAR))
            .input(UItems.TOAST, 8)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, UItems.CANDIED_APPLE)
            .input(ConventionalItemTags.WOODEN_RODS)
            .input(UTags.Items.FRESH_APPLES).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .input(Items.SUGAR, 4)
            .offerTo(exporter);

        // trick apples
        offerTrickRecipe(UItems.GREEN_APPLE, Items.GREEN_DYE);
        offerTrickRecipe(Items.APPLE, Items.RED_DYE);
        offerTrickRecipe(UItems.SOUR_APPLE, Items.YELLOW_DYE);
        offerTrickRecipe(UItems.SWEET_APPLE, Items.ORANGE_DYE);
        offerTrickRecipe(UItems.ROTTEN_APPLE, Items.ROTTEN_FLESH);
        offerTrickRecipe(UItems.COOKED_ZAP_APPLE, Items.SPIDER_EYE);
        offerTrickRecipe(Items.GOLDEN_APPLE, Items.GOLD_NUGGET);
        offerTrickRecipe(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLD_INGOT);
        offerTrickRecipe(UItems.MANGO, Items.LIME_DYE);
        offerTrickRecipe(UItems.PINEAPPLE, UItems.PINEAPPLE_CROWN);
        offerTrickRecipe(UItems.HORSE_SHOE_FRIES, UItems.IRON_HORSE_SHOE);
        offerTrickRecipe(UItems.MUFFIN, UItems.ROCK);
    }

    @Override
    public <T extends AbstractCookingRecipe> void generateCookingRecipes(String cooker, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime) {
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime / 3, UItems.JUICE, UItems.BURNED_JUICE, 0);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, Items.BREAD, UItems.TOAST, 0.2F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, UItems.TOAST, UItems.BURNED_TOAST, 0.2F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime / 2, UItems.BURNED_TOAST, Items.CHARCOAL, 1);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, UItems.HAY_FRIES, UItems.CRISPY_HAY_FRIES, 1F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, UItems.ZAP_APPLE, UItems.COOKED_ZAP_APPLE, 0.6F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, Items.TROPICAL_FISH, UItems.COOKED_TROPICAL_FISH, 0.35F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, Items.PUFFERFISH, UItems.COOKED_PUFFERFISH, 1.2F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime + 30, Items.AXOLOTL_BUCKET, UItems.FRIED_AXOLOTL, 2.2F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime, UItems.FROG_LEGS, UItems.COOKED_FROG_LEGS, 2.2F);
        offerFoodCookingRecipe(cooker, serializer, recipeFactory, cookingTime + 50, UBlocks.MYSTERIOUS_EGG, UItems.GREEN_FRIED_EGG, 3.8F);
    }

    public void offerTrickRecipe(ItemConvertible output, ItemConvertible input) {
        TrickCraftingRecipeJsonBuilder.create(RecipeCategory.FOOD, output)
            .input(UItems.ZAP_APPLE).criterion(hasItem(UItems.ZAP_APPLE), conditionsFromItem(UItems.ZAP_APPLE))
            .input(input)
            .offerTo(exporter, convertBetween(output, input) + "_trick");
    }

    private void offerSeaponyRecipes() {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.SHELLY)
            .input('C', UItems.CLAM_SHELL).criterion("has_clam_shell", conditionsFromItem(UItems.CLAM_SHELL))
            .input('o', UItems.ROCK_CANDY)
            .pattern("o o")
            .pattern(" C ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.COMBAT, UItems.PEARL_NECKLACE)
            .input('#', UTags.Items.SHELLS).criterion("has_shell", conditionsFromTag(UTags.Items.SHELLS))
            .input('~', Items.STRING)
            .pattern("# #")
            .pattern("# #")
            .pattern("~#~")
            .offerTo(exporter);
    }

    private void offerEarthPonyRecipes() {
        Arrays.stream(ItemFamilies.BASKETS).forEach(basket -> offerBasketRecipe(basket, getMaterial(basket, "_basket", "_planks")));
        Arrays.stream(ItemFamilies.HORSE_SHOES).forEach(horseshoe -> offerHorseshoeRecipe(horseshoe, getMaterial(horseshoe, "_horse_shoe", "_ingot")));
        Arrays.stream(ItemFamilies.POLEARMS).forEach(polearm -> {
            if (polearm == UItems.NETHERITE_POLEARM) {
                offerNetheriteUpgradeRecipe(UItems.DIAMOND_POLEARM, RecipeCategory.TOOLS, UItems.NETHERITE_POLEARM);
            } else {
                offerPolearmRecipe(polearm, getMaterial(polearm, "_polearm", "_ingot"));
            }
        });
        // weather vane
        offerWeatherVaneRecipe(UBlocks.WEATHER_VANE, Items.IRON_NUGGET);

        // Giant balloons
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.GIANT_BALLOON)
            .input('-', ItemTags.WOOL_CARPETS).criterion("has_carpet", conditionsFromTag(ItemTags.WOOL_CARPETS))
            .input('#', ItemTags.WOOL).criterion("has_wool", conditionsFromTag(ItemTags.WOOL))
            .pattern("---")
            .pattern("# #")
            .pattern("---")
            .offerTo(exporter);

        // worms
        offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, UItems.WHEAT_WORMS, RecipeCategory.BUILDING_BLOCKS, UBlocks.WORM_BLOCK);
        // fishing
        ItemConversionShapedRecipeBuilder.create(items, RecipeCategory.MISC, Items.FISHING_ROD, UItems.BAITED_FISHING_ROD)
            .input('#', Items.FISHING_ROD).criterion(hasItem(Items.FISHING_ROD), conditionsFromItem(Items.FISHING_ROD))
            .input('*', UItems.WHEAT_WORMS).criterion("has_wheat_worms", conditionsFromItem(UItems.WHEAT_WORMS))
            .pattern("# ")
            .pattern(" *")
            .group("fishing_rod")
            .offerTo(exporter);

        // utility
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, Items.DIRT)
            .input('*', UItems.WHEAT_WORMS).criterion("has_wheat_worms", conditionsFromItem(UItems.WHEAT_WORMS))
            .input('#', ItemTags.SAND).criterion("has_sand", conditionsFromTag(ItemTags.SAND))
            .pattern("*#")
            .pattern("#*")
            .offerTo(exporter, convertBetween(Items.DIRT, UItems.WHEAT_WORMS));

        offerShapelessRecipe(Items.BONE_MEAL, UTags.Items.SHELLS, "bonemeal", 3);

        // pegasus feathers for non pegasi
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.PEGASUS_FEATHER)
            .input('*', Items.GHAST_TEAR).criterion("has_ghast_tear", conditionsFromItem(Items.GHAST_TEAR))
            .input('#', UItems.GRYPHON_FEATHER).criterion("has_feather", conditionsFromItem(UItems.GRYPHON_FEATHER))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .offerTo(exporter);

        offer2x2CompactingRecipe(RecipeCategory.BUILDING_BLOCKS, Items.COBBLESTONE, UItems.ROCK);
        offerReversibleCompactingRecipesWithReverseRecipeGroup(RecipeCategory.MISC, UItems.PEBBLES, RecipeCategory.BUILDING_BLOCKS, Blocks.GRAVEL, convertBetween(UItems.PEBBLES, Blocks.GRAVEL), "pebbles");
        offerShapelessRecipe(UItems.PEBBLES, Blocks.SUSPICIOUS_GRAVEL, "pebbles", 9);
        offerSmelting(List.of(UItems.GOLDEN_OAK_SEEDS, UItems.GOLDEN_FEATHER), RecipeCategory.MISC, Items.GOLD_NUGGET, 3F, 10, "gold_nugget");

        offerGrowing(UBlocks.CURING_JOKE, Blocks.LAPIS_BLOCK, Blocks.CORNFLOWER);
        offerGrowing(UBlocks.GOLD_ROOT, Blocks.RAW_GOLD_BLOCK, Blocks.CARROTS);
        offerGrowing(UTreeGen.GOLDEN_OAK_TREE.sapling().get(), Blocks.RAW_GOLD_BLOCK, Blocks.OAK_SAPLING);
        offerGrowing(UBlocks.PLUNDER_VINE_BUD, Blocks.NETHERRACK, Blocks.WITHER_ROSE);
        offerGrowing(UTreeGen.ZAP_APPLE_TREE.sapling().get(), UBlocks.CHITIN, Blocks.DARK_OAK_SAPLING);
    }

    private ShapelessRecipeJsonBuilder appendIngredients(ShapelessRecipeJsonBuilder builder, ItemConvertible...ingredients) {
        for (ItemConvertible ingredient : ingredients) {
            builder.input(ingredient).criterion(hasItem(ingredient), conditionsFromItem(ingredient));
        }
        return builder;
    }

    public void offerShapelessRecipe(ItemConvertible output, TagKey<Item> input, @Nullable String group, int outputCount) {
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, output, outputCount)
            .input(input).criterion(hasTag(input), conditionsFromTag(input))
            .group(group)
            .offerTo(exporter, getItemPath(output) + "_from_" + input.id().getPath());
    }

    public void offerPieRecipe(ItemConvertible pie, ItemConvertible slice, ItemConvertible crust, TagKey<Item> filling) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.FOOD, pie)
            .input('*', crust).criterion("has_crust", conditionsFromItem(crust))
            .input('#', filling).criterion("has_filling", conditionsFromTag(filling))
            .pattern("***")
            .pattern("###")
            .pattern("***")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.FOOD, pie)
            .input(slice, 4)
            .criterion(hasItem(slice), conditionsFromItem(slice))
            .offerTo(exporter, getItemPath(pie) + "_from_" + getItemPath(slice));
    }

    public void offerBasketRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        input(ShapedRecipeJsonBuilder.create(items, RecipeCategory.TRANSPORTATION, output), '#', input)
            .criterion(hasEither(input), conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .group("basket")
            .offerTo(exporter);
    }

    public void offerHorseshoeRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        input(ShapedRecipeJsonBuilder.create(items, RecipeCategory.COMBAT, output), '#', input)
            .criterion(hasEither(input), conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .group("horseshoe")
            .offerTo(exporter);
    }

    public void offerHiveRecipe(ItemConvertible output, ItemConvertible chitin, ItemConvertible egg) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.MISC, output)
            .input('#', chitin)
            .input('o', egg).criterion(hasItem(egg), conditionsFromItem(egg))
            .pattern(" # ")
            .pattern("#o#")
            .pattern(" # ")
            .group("chitin")
            .offerTo(exporter);
    }

    public void offerPolearmRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        input(ShapedRecipeJsonBuilder.create(items, RecipeCategory.TOOLS, output), 'o', input).criterion(hasEither(input), conditionsFromEither(input))
            .input('#', ConventionalItemTags.WOODEN_RODS)
            .input('s', ConventionalItemTags.STRINGS)
            .pattern("  o")
            .pattern(" #s")
            .pattern("#  ")
            .group("polearm")
            .offerTo(exporter);
    }

    public void offerHullRecipe(ItemConvertible output, ItemConvertible outside, ItemConvertible inside) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.BUILDING_BLOCKS, output, 4)
            .input('#', outside).criterion(hasItem(outside), conditionsFromItem(outside))
            .input('o', inside).criterion(hasItem(inside), conditionsFromItem(inside))
            .pattern("##")
            .pattern("oo")
            .group("hull")
            .offerTo(exporter);
    }

    public void offerSpikesRecipe(ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output, 8)
            .input('#', input).criterion(hasItem(input), conditionsFromItem(input))
            .pattern(" # ")
            .pattern("###")
            .group("spikes")
            .offerTo(exporter);
    }

    public void offerChestRecipe(ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output)
            .input('#', input)
            .pattern("###")
            .pattern("# #")
            .pattern("###")
            .criterion("has_lots_of_items", Criteria.INVENTORY_CHANGED.create(new InventoryChangedCriterion.Conditions(
                    Optional.empty(),
                    new InventoryChangedCriterion.Conditions.Slots(NumberRange.IntRange.atLeast(10), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY),
                    List.of())))
            .offerTo(exporter);
    }

    public void offer2x3Recipe(ItemConvertible output, ItemConvertible input, String group) {
        createDoorRecipe(output, Ingredient.ofItems(input))
            .criterion(hasItem(input), conditionsFromItem(input))
            .group(group)
            .offerTo(exporter);
    }

    public void offerStableDoorRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> body, ItemConvertible trim) {
        input(ShapedRecipeJsonBuilder.create(items, RecipeCategory.REDSTONE, output, 3), '#', body).criterion(hasEither(body), conditionsFromEither(body))
            .input('*', trim).criterion(hasItem(trim), conditionsFromItem(trim))
            .pattern("*#*")
            .pattern("*#*")
            .pattern("*#*")
            .group("stable_door")
            .offerTo(exporter);
    }

    public void offerWeatherVaneRecipe(ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output)
            .input('*', input).criterion(hasItem(input), conditionsFromItem(input))
            .pattern(" **")
            .pattern("** ")
            .pattern(" * ")
            .offerTo(exporter);
    }

    public ShapedRecipeJsonBuilder createCustomBedRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input, Either<ItemConvertible, TagKey<Item>> planks) {
        var builder = ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output);
        input(builder, '#', input).criterion(hasEither(input), conditionsFromEither(input));
        return input(builder, 'X', planks)
            .pattern("###")
            .pattern("XXX")
            .group("bed");
    }

    private void offerBedSheetRecipes() {
        PatternTemplate.ONE_COLOR.offerWithoutConversion(this, items, exporter, UItems.KELP_BED_SHEETS, Items.KELP);
        WOOLS.forEach(wool -> PatternTemplate.ONE_COLOR.offerTo(this, items, exporter, getItem(Unicopia.id(Registries.ITEM.getId(wool).getPath().replace("_wool", "_bed_sheets"))), wool));

        PatternTemplate.TWO_COLOR.offerTo(this, items, exporter, UItems.APPLE_BED_SHEETS, Items.GREEN_WOOL, Items.LIME_WOOL);
        PatternTemplate.TWO_COLOR.offerTo(this, items, exporter, UItems.BARRED_BED_SHEETS, Items.LIGHT_BLUE_WOOL, Items.WHITE_WOOL);
        PatternTemplate.TWO_COLOR.offerTo(this, items, exporter, UItems.CHECKERED_BED_SHEETS, Items.GREEN_WOOL, Items.BROWN_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(this, items, exporter, UItems.RAINBOW_PWR_BED_SHEETS, Items.WHITE_WOOL, Items.PINK_WOOL, Items.RED_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(this, items, exporter, UItems.RAINBOW_BPY_BED_SHEETS, Items.PINK_WOOL, Items.YELLOW_WOOL, Items.LIGHT_BLUE_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(this, items, exporter, UItems.RAINBOW_BPW_BED_SHEETS, Items.PINK_WOOL, Items.LIGHT_BLUE_WOOL, Items.WHITE_WOOL);
        PatternTemplate.FOUR_COLOR.offerTo(this, items, exporter, UItems.RAINBOW_PBG_BED_SHEETS, Items.PURPLE_WOOL, Items.WHITE_WOOL, Items.LIGHT_GRAY_WOOL, Items.BLACK_WOOL);
        PatternTemplate.SEVEN_COLOR.offerTo(this, items, exporter, UItems.RAINBOW_BED_SHEETS, UItems.RAINBOW_BED_SHEETS, Items.LIGHT_BLUE_WOOL, Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL, Items.BLUE_WOOL, Items.GREEN_WOOL, Items.PURPLE_WOOL);
    }

    private void offerFarmersDelightCuttingRecipes() {
        // unwaxing
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, waxed) -> {
            if (variant == Variant.WALL_SIGN) return;
            var unwaxed = UBlockFamilies.ZAP.getVariant(variant);
            CuttingBoardRecipeJsonBuilder.create(unwaxed, "axe_strip")
                .input(waxed).criterion(hasItem(waxed), conditionsFromItem(waxed))
                .result(unwaxed)
                .result(Items.HONEYCOMB)
                .sound(SoundEvents.ITEM_AXE_WAX_OFF)
                .offerTo(farmersDelightExporter, getItemPath(unwaxed) + "_from_waxed");
        });
        List.of(UBlockFamilies.ZAP, UBlockFamilies.PALM).forEach(family -> {
            family.getVariants().forEach((variant, block) -> {
                if (variant == Variant.WALL_SIGN) return;
                CuttingBoardRecipeJsonBuilder.create(family.getBaseBlock(), "axe_strip")
                    .input(block).criterion(hasItem(block), conditionsFromItem(block))
                    .result(family.getBaseBlock())
                    .sound(SoundEvents.ITEM_AXE_STRIP)
                    .offerTo(farmersDelightExporter, getItemPath(block));
            });
        });
        CuttingBoardRecipeJsonBuilder.create(UBlocks.PALM_PLANKS, "axe_dig")
            .input(UBlocks.PALM_HANGING_SIGN).criterion(hasItem(UBlocks.PALM_HANGING_SIGN), conditionsFromItem(UBlocks.PALM_HANGING_SIGN))
            .sound(SoundEvents.ITEM_AXE_STRIP)
            .result(UBlocks.PALM_PLANKS)
            .offerTo(farmersDelightExporter);

        Map.of(
                UBlocks.PALM_LOG, UBlocks.STRIPPED_PALM_LOG,
                UBlocks.PALM_WOOD, UBlocks.STRIPPED_PALM_WOOD,
                UBlocks.ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG,
                UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(hasItem(unstripped), conditionsFromItem(unstripped))
                .sound(SoundEvents.ITEM_AXE_STRIP)
                .result(stripped)
                .result(Identifier.of("farmersdelight:tree_bark"))
                .offerTo(farmersDelightExporter, convertBetween(stripped, unstripped));
        });
        Map.of(
                UBlocks.GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_LOG,
                UBlocks.GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(hasItem(unstripped), conditionsFromItem(unstripped))
                .sound(SoundEvents.ITEM_AXE_STRIP)
                .result(stripped)
                .result(Items.GOLD_NUGGET, 8)
                .offerTo(exporter, convertBetween(stripped, unstripped));
        });

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.APPLE_PIE)
            .input(FarmersDelightContent.APPLE_PIE).criterion(hasItem(FarmersDelightContent.APPLE_PIE), conditionsFromItem(FarmersDelightContent.APPLE_PIE))
            .offerTo(exporter, "apple_pie_to_apple_pie");
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, FarmersDelightContent.APPLE_PIE)
            .input(UItems.APPLE_PIE).criterion(hasItem(UItems.APPLE_PIE), conditionsFromItem(UItems.APPLE_PIE))
            .offerTo(exporter, "apple_pie_from_apple_pie");

        CuttingBoardRecipeJsonBuilder.create(UItems.HAY_FRIES, "axe_dig")
                .input(Blocks.HAY_BLOCK).criterion(hasItem(Blocks.HAY_BLOCK), conditionsFromItem(Blocks.HAY_BLOCK))
                .sound(SoundEvents.ITEM_AXE_SCRAPE)
                .result(UItems.HAY_FRIES, 9)
                .offerTo(exporter);

        CuttingBoardRecipeJsonBuilder.create(UItems.APPLE_PIE_SLICE, Ingredient.fromTag(items.getOrThrow(UConventionalTags.Items.TOOL_KNIVES)))
            .input(UBlocks.APPLE_PIE).criterion(hasItem(UBlocks.APPLE_PIE), conditionsFromItem(UBlocks.APPLE_PIE))
            .sound(USounds.BLOCK_PIE_SLICE)
            .result(UItems.APPLE_PIE_SLICE, 4)
            .offerTo(exporter);
    }

    public void offerCompactingRecipe(RecipeCategory category, ItemConvertible output, ItemConvertible input, int resultCount) {
        offerCompactingRecipe(category, output, input, hasItem(input), resultCount);
    }

    public void offerCompactingRecipe(RecipeCategory category, ItemConvertible output, ItemConvertible input, String criterionName, int resultCount) {
        ShapelessRecipeJsonBuilder.create(items, category, output, resultCount)
            .input(input, 9).criterion(criterionName, conditionsFromItem(input))
            .offerTo(exporter);
    }

    public void offerWaxingRecipes() {
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, output) -> {
            Block input = UBlockFamilies.ZAP.getVariant(variant);
            offerWaxingRecipe(output, input);
        });
        offerWaxingRecipe(UBlocks.WAXED_ZAP_PLANKS, UBlocks.ZAP_PLANKS);
        offerWaxingRecipe(UBlocks.WAXED_ZAP_LOG, UBlocks.ZAP_LOG);
        offerWaxingRecipe(UBlocks.WAXED_ZAP_WOOD, UBlocks.ZAP_WOOD);
        offerWaxingRecipe(UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG);
        offerWaxingRecipe(UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD);
    }

    public void offerWaxingRecipe(ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.BUILDING_BLOCKS, output)
            .input(Items.HONEYCOMB)
            .input(input).criterion(hasItem(input), conditionsFromItem(input))
            .group(getItemPath(output))
            .offerTo(exporter, convertBetween(output, Items.HONEYCOMB));
    }

    public void offerCloudShapingRecipe(RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        offerCloudShapingRecipe(category, output, input, 1);
    }

    public void offerCloudShapingRecipe(RecipeCategory category, ItemConvertible output, ItemConvertible input, int count) {
        createCloudShaping(Ingredient.ofItems(input), category, output, count)
            .criterion(hasItem(input), conditionsFromItem(input))
            .offerTo(exporter, convertBetween(output, input) + "_cloud_shaping");
    }

    public void offerGrowing(Block output, Block fuel, Block target) {
        GrowingRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output.getDefaultState())
            .fuel(fuel.getDefaultState())
            .target(target).criterion(hasItem(target), conditionsFromItem(target))
            .offerTo(exporter);
    }

    public void offerSpell(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromSpell(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromTwoSpells(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input1, SpellType<?> input2, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input1)
            .input(gemstone, input2)
            .traits(traits)
            .offerTo(exporter);
    }

    public AdvancementCriterion<?> conditionsFromMultipleItems(ItemConvertible... items) {
        return conditionsFromItemPredicates(
            Stream.of(items).map(item -> ItemPredicate.Builder.create().items(this.items, item).build()).toArray(ItemPredicate[]::new)
        );
    }
}
