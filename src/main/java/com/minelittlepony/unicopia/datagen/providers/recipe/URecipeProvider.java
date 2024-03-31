package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.datagen.providers.recipe.BedSheetPatternRecipeBuilder.PatternTemplate;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public class URecipeProvider extends FabricRecipeProvider {
    private static final List<Item> WOOLS = List.of(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL, Items.GRAY_WOOL, Items.GREEN_WOOL, Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL, Items.PURPLE_WOOL, Items.RED_WOOL, Items.YELLOW_WOOL, Items.WHITE_WOOL);
    public URecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        generateVanillaRecipeExtensions(exporter);
        offerJarRecipes(exporter);
        offerWoodBlocksRecipes(exporter);
        offerChitinBlocksRecipes(exporter);
        offerCloudRecipes(exporter);
        offerFoodRecipes(exporter);
        offerGemstoneAndMagicRecipes(exporter);
        offerMagicSpellRecipes(exporter);
        offerSeaponyRecipes(exporter);
        offerEarthPonyRecipes(exporter);

        // beds
        createCustomBedRecipe(UItems.CLOUD_BED, Either.left(UBlocks.DENSE_CLOUD), Either.left(UBlocks.CLOUD_PLANKS)).offerTo(exporter);
        createCustomBedRecipe(UItems.CLOTH_BED, Either.right(ItemTags.WOOL), Either.right(ItemTags.LOGS)).offerTo(exporter);
        offerBedSheetRecipes(exporter);

        // sunglasses
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.SUNGLASSES)
            .input('#', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass_block", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("##")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.SUNGLASSES)
            .input(ConventionalItemTags.GLASS_BLOCKS)
            .input(UItems.SUNGLASSES).criterion("has_broken_sunglasses", conditionsFromItem(UItems.BROKEN_SUNGLASSES))
            .offerTo(exporter, convertBetween(UItems.SUNGLASSES, UItems.BROKEN_SUNGLASSES));
    }

    private void generateVanillaRecipeExtensions(Consumer<RecipeJsonProvider> exporter) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.WRITABLE_BOOK)
            .input(Items.BOOK).criterion("has_book", conditionsFromItem(Items.BOOK))
            .input(Items.INK_SAC)
            .input(UTags.Items.MAGIC_FEATHERS)
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.ARROW, 4)
            .input('#', UConventionalTags.Items.STICKS)
            .input('X', Items.FLINT).criterion("has_flint", conditionsFromItem(Items.FLINT))
            .input('Y', UTags.Items.MAGIC_FEATHERS).criterion("has_feather", conditionsFromTag(UTags.Items.MAGIC_FEATHERS))
            .pattern("X")
            .pattern("#")
            .pattern("Y")
            .offerTo(exporter);
    }

    private void offerJarRecipes(Consumer<RecipeJsonProvider> exporter) {
        ComplexRecipeJsonBuilder.create(URecipes.JAR_EXTRACT_SERIALIZER).offerTo(exporter, "empty_jar_from_filled_jar");
        ComplexRecipeJsonBuilder.create(URecipes.JAR_INSERT_SERIALIZER).offerTo(exporter, "filled_jar");
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.EMPTY_JAR, 7)
            .input('#', ItemTags.PLANKS)
            .input('*', ConventionalItemTags.GLASS_BLOCKS).criterion("has_glass", conditionsFromTag(ConventionalItemTags.GLASS_BLOCKS))
            .pattern("*#*")
            .pattern("* *")
            .pattern("***")
            .offerTo(exporter);
    }

    private void offerCloudRecipes(Consumer<RecipeJsonProvider> exporter) {
        offerShapelessRecipe(exporter, UItems.CLOUD_LUMP, UTags.Items.CLOUD_JARS, "cloud", 4);
        generateFamily(exporter, UBlockFamilies.CLOUD);
        offer2x3Recipe(exporter, UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, "pillar");
        offer2x2CompactingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD, UItems.CLOUD_LUMP);
        offerPolishedStoneRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        generateFamily(exporter, UBlockFamilies.CLOUD_PLANKS);
        offerChestRecipe(exporter, UBlocks.CLOUD_CHEST, UBlocks.CLOUD_PLANKS);

        offer2x2CompactingRecipe(exporter, RecipeCategory.DECORATIONS, UBlocks.SHAPING_BENCH, UBlocks.DENSE_CLOUD);
        generateFamily(exporter, UBlockFamilies.CLOUD_BRICKS);

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CARVED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD, UBlocks.CLOUD);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PILLAR, UBlocks.CLOUD);

        // TODO: Cut Cloud, Smooth Cloud, Polished Cloud, Raked Cloud

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_SLAB, UBlocks.CLOUD, 2);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_STAIRS, UBlocks.CLOUD);

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_SLAB, UBlocks.CLOUD_BRICKS, 2);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_BRICK_STAIRS, UBlocks.CLOUD_BRICKS);

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_PLANKS, 2);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_PLANKS);

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_SLAB, UBlocks.DENSE_CLOUD, 2);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD_STAIRS, UBlocks.DENSE_CLOUD);

        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_SLAB, UBlocks.ETCHED_CLOUD, 2);
        offerCloudShapingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.ETCHED_CLOUD_STAIRS, UBlocks.ETCHED_CLOUD);

        offerCompactingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.DENSE_CLOUD, UBlocks.CLOUD, 4);
        generateFamily(exporter, UBlockFamilies.DENSE_CLOUD);
        offer2x3Recipe(exporter, UBlocks.CLOUD_DOOR, UBlocks.DENSE_CLOUD, "door");

        ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, UBlocks.UNSTABLE_CLOUD, 8)
            .input(UBlocks.CLOUD, 8)
            .input(Ingredient.ofItems(UItems.LIGHTNING_JAR, UItems.ZAP_APPLE_JAM_JAR))
            .criterion("has_lightning_jar", conditionsFromItem(UItems.LIGHTNING_JAR))
            .criterion("has_zap_jar", conditionsFromItem(UItems.ZAP_APPLE_JAM_JAR))
            .offerTo(exporter);
    }

    private void offerWoodBlocksRecipes(Consumer<RecipeJsonProvider> exporter) {
        // palm wood
        generateFamily(exporter, UBlockFamilies.PALM);
        offerPlanksRecipe(exporter, UBlocks.PALM_PLANKS, UTags.Items.PALM_LOGS, 4);
        offerBarkBlockRecipe(exporter, UBlocks.PALM_WOOD, UBlocks.PALM_LOG);
        offerBarkBlockRecipe(exporter, UBlocks.STRIPPED_PALM_WOOD, UBlocks.STRIPPED_PALM_LOG);
        offerBoatRecipe(exporter, UItems.PALM_BOAT, UBlocks.PALM_PLANKS);
        offerChestBoatRecipe(exporter, UItems.PALM_CHEST_BOAT, UItems.PALM_BOAT);
        offerHangingSignRecipe(exporter, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_PLANKS);

        // zap wood
        generateFamily(exporter, UBlockFamilies.ZAP);
        offerPlanksRecipe(exporter, UBlocks.ZAP_PLANKS, UTags.Items.ZAP_LOGS, 4);
        offerBarkBlockRecipe(exporter, UBlocks.ZAP_WOOD, UBlocks.ZAP_LOG);
        offerBarkBlockRecipe(exporter, UBlocks.STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG);

        // waxed zap wood
        offerPlanksRecipe(exporter, UBlocks.WAXED_ZAP_PLANKS, UTags.Items.WAXED_ZAP_LOGS, 4);
        offerBarkBlockRecipe(exporter, UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_ZAP_LOG);
        generateFamily(exporter, UBlockFamilies.WAXED_ZAP);
        offerBarkBlockRecipe(exporter, UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.WAXED_STRIPPED_ZAP_LOG);

        offerWaxingRecipes(exporter);

        // other doors
        offer2x3Recipe(exporter, UBlocks.CRYSTAL_DOOR, UItems.CRYSTAL_SHARD, "door");
        offerStableDoorRecipe(exporter, UBlocks.STABLE_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK_CANDY);
        offerStableDoorRecipe(exporter, UBlocks.DARK_OAK_DOOR, Either.right(ItemTags.PLANKS), UItems.ROCK);
    }

    private void offerChitinBlocksRecipes(Consumer<RecipeJsonProvider> exporter) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, UItems.CARAPACE, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHITIN);
        generateFamily(exporter, UBlockFamilies.CHISELED_CHITIN);
        offerHiveRecipe(exporter, UBlocks.HIVE, UBlocks.CHITIN, UBlocks.MYSTERIOUS_EGG);
        offerHullRecipe(exporter, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);
        offerSpikesRecipe(exporter, UBlocks.CHITIN_SPIKES, UBlocks.CHITIN);

        // TODO: polished chitin
        offerPolishedStoneRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN, UBlocks.CHITIN);

        offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN);
        offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CHISELLED_CHITIN, 2);
        offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, UBlocks.CHISELLED_CHITIN_STAIRS, UBlocks.CHISELLED_CHITIN);
    }

    private void offerGemstoneAndMagicRecipes(Consumer<RecipeJsonProvider> exporter) {
        offerShapelessRecipe(exporter, UItems.CRYSTAL_SHARD, Items.DIAMOND, "crystal_shard", 6);
        offerShapelessRecipe(exporter, UItems.CRYSTAL_SHARD, Items.AMETHYST_SHARD, "crystal_shard", 3);
        offer2x2CompactingRecipe(exporter, RecipeCategory.MISC, UItems.GEMSTONE, UItems.CRYSTAL_SHARD);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.SPELLBOOK)
            .input(Items.BOOK).criterion("has_book", conditionsFromItem(Items.BOOK))
            .input(UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .offerTo(exporter);

        // magic staff
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.MAGIC_STAFF)
            .input('*', UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .input('#', UConventionalTags.Items.STICKS).criterion("has_stick", conditionsFromTag(UConventionalTags.Items.STICKS))
            .pattern("  *")
            .pattern(" # ")
            .pattern("#  ")
            .offerTo(exporter);

        // crystal heart
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, UItems.CRYSTAL_HEART)
            .input('#', UItems.CRYSTAL_SHARD).criterion("has_crystal_shard", conditionsFromItem(UItems.CRYSTAL_SHARD))
            .pattern("# #")
            .pattern("###")
            .pattern(" # ")
            .offerTo(exporter);

        // pegasus amulet
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.GOLDEN_FEATHER)
            .input('*', Items.GOLD_NUGGET).criterion("has_nugget", conditionsFromItem(Items.GOLD_NUGGET))
            .input('#', UTags.Items.MAGIC_FEATHERS).criterion("has_feather", conditionsFromTag(UTags.Items.MAGIC_FEATHERS))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .offerTo(exporter);
        offerCompactingRecipe(exporter, RecipeCategory.COMBAT, UItems.GOLDEN_WING, UItems.GOLDEN_FEATHER);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.PEGASUS_AMULET)
            .input('*', UItems.GOLDEN_WING).criterion("has_wing", conditionsFromItem(UItems.GOLDEN_WING))
            .input('#', UItems.GEMSTONE).criterion("has_gemstone", conditionsFromItem(UItems.GEMSTONE))
            .pattern("*#*")
            .offerTo(exporter);

        // friendship bracelet
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.FRIENDSHIP_BRACELET)
            .input('*', Items.STRING)
            .input('#', Items.LEATHER).criterion(hasItem(Items.LEATHER), conditionsFromItem(Items.LEATHER))
            .pattern("*#*")
            .pattern("# #")
            .pattern("*#*")
            .offerTo(exporter);
        ComplexRecipeJsonBuilder.create(URecipes.GLOWING_SERIALIZER).offerTo(exporter, "friendship_bracelet_glowing");

        // meadowbrook's staff
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.MEADOWBROOKS_STAFF)
            .input('*', UItems.GEMSTONE).criterion(hasItem(UItems.GEMSTONE), conditionsFromItem(UItems.GEMSTONE))
            .input('/', UConventionalTags.Items.STICKS).criterion(hasItem(Items.STICK), conditionsFromTag(UConventionalTags.Items.STICKS))
            .pattern("  *")
            .pattern(" / ")
            .pattern("/  ")
            .offerTo(exporter);
        offerShapelessRecipe(exporter, Items.STICK, UItems.MEADOWBROOKS_STAFF, "stick", 2);
    }

    private void offerMagicSpellRecipes(Consumer<RecipeJsonProvider> exporter) {
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

        ComplexSpellcraftingRecipeJsonBuilder.create(URecipes.SPELL_DUPLICATING, UItems.BOTCHED_GEM).offerTo(exporter, "spell_duplicating");
        ComplexSpellcraftingRecipeJsonBuilder.create(URecipes.TRAIT_COMBINING, UItems.BOTCHED_GEM).offerTo(exporter, "trait_combining_botched_gem");
        ComplexSpellcraftingRecipeJsonBuilder.create(URecipes.TRAIT_COMBINING, UItems.GEMSTONE).offerTo(exporter, "trait_combining_gemstone");
    }

    private void offerFoodRecipes(Consumer<RecipeJsonProvider> exporter) {
        offerShapelessRecipe(exporter, UItems.PINEAPPLE_CROWN, UItems.PINEAPPLE, "seeds", 1);
        offerShapelessRecipe(exporter, UItems.SWEET_APPLE_SEEDS, UItems.SWEET_APPLE, "seeds", 3);
        offerShapelessRecipe(exporter, UItems.SOUR_APPLE_SEEDS, UItems.SOUR_APPLE, "seeds", 3);
        offerShapelessRecipe(exporter, UItems.GREEN_APPLE_SEEDS, UItems.GREEN_APPLE, "seeds", 3);
        offerShapelessRecipe(exporter, UItems.GOLDEN_OAK_SEEDS, Items.GOLDEN_APPLE, "seeds", 1);
        offerPieRecipe(exporter, UItems.APPLE_PIE, UItems.APPLE_PIE_SLICE, Items.WHEAT, UTags.Items.FRESH_APPLES);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.ROCK_STEW)
            .input(UItems.ROCK, 3).criterion(hasItem(UItems.ROCK), conditionsFromItem(UItems.ROCK))
            .input(Items.BOWL)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.BOWL_OF_NUTS)
            .input(UItems.ACORN, 3).criterion(hasItem(UItems.ACORN), conditionsFromItem(UItems.ACORN))
            .input(Items.BOWL)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.OATMEAL_COOKIE)
            .input(UItems.OATS, 2).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.CHOCOLATE_OATMEAL_COOKIE)
            .input(UItems.OATS, 2)
            .input(Items.COCOA_BEANS).criterion(hasItem(Items.COCOA_BEANS), conditionsFromItem(Items.COCOA_BEANS))
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.PINECONE_COOKIE)
            .input(UItems.PINECONE).criterion(hasItem(UItems.PINECONE), conditionsFromItem(UItems.PINECONE))
            .input(Items.WHEAT, 2)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.SCONE)
            .input(UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .input(Items.WHEAT, 2)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.ROCK_CANDY, 3)
            .input(Items.SUGAR, 6).criterion(hasItem(Items.SUGAR), conditionsFromItem(Items.SUGAR))
            .input(UItems.PEBBLES, 3)
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, Items.BREAD)
            .input('#', UItems.OATS).criterion("has_oats", conditionsFromItem(UItems.OATS))
            .pattern("###")
            .offerTo(exporter, convertBetween(UItems.OATS, Items.WHEAT));
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.JUICE)
            .input(Ingredient.fromTag(UTags.Items.FRESH_APPLES), 6).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .input(Items.GLASS_BOTTLE)
            .group("juice")
            .offerTo(exporter);
        appendIngredients(ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.MUFFIN), Items.SUGAR, Items.EGG, Items.POTATO, UItems.JUICE, UItems.WHEAT_WORMS).offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.MUG)
            .input('*', Items.IRON_NUGGET).criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
            .input('#', UConventionalTags.Items.STICKS).criterion(hasItem(Items.STICK), conditionsFromTag(UConventionalTags.Items.STICKS))
            .pattern("# #")
            .pattern("* *")
            .pattern(" # ")
            .offerTo(exporter);
        appendIngredients(ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.CIDER), UItems.BURNED_JUICE, UItems.MUG)
            .input(Ingredient.fromTag(UTags.Items.FRESH_APPLES)).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.HAY_FRIES)
            .input('#', UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .pattern("#")
            .pattern("#")
            .pattern("#")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.HAY_BURGER)
            .input('~', Items.BREAD).criterion(hasItem(Items.BREAD), conditionsFromItem(Items.BREAD))
            .input('#', UItems.OATS).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.DAFFODIL_DAISY_SANDWICH)
            .input('#', Items.BREAD).criterion(hasItem(Items.BREAD), conditionsFromItem(Items.BREAD))
            .input('~', ItemTags.SMALL_FLOWERS).criterion("has_flower", conditionsFromTag(ItemTags.SMALL_FLOWERS))
            .pattern(" # ")
            .pattern("~~~")
            .pattern(" # ")
            .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.HORSE_SHOE_FRIES, 15)
            .input('#', Items.BAKED_POTATO).criterion(hasItem(Items.BAKED_POTATO), conditionsFromItem(Items.BAKED_POTATO))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.OATMEAL)
            .input(UItems.OATS, 3).criterion(hasItem(UItems.OATS), conditionsFromItem(UItems.OATS))
            .input(ConventionalItemTags.MILK_BUCKETS)
            .input(Items.BOWL)
            .offerTo(exporter);

        offerSmelting(exporter, List.of(UItems.JUICE), RecipeCategory.FOOD, UItems.BURNED_JUICE, 0, 100, "juice");
        offerSmelting(exporter, List.of(Items.BREAD), RecipeCategory.FOOD, UItems.TOAST, 0.2F, 430, "bread");
        offerSmelting(exporter, List.of(UItems.TOAST), RecipeCategory.FOOD, UItems.BURNED_TOAST, 0.2F, 30, "bread");
        offerSmelting(exporter, List.of(UItems.BURNED_JUICE, UItems.BURNED_TOAST), RecipeCategory.FOOD, Items.CHARCOAL, 1, 20, "coal");
        offerSmelting(exporter, List.of(UItems.HAY_FRIES), RecipeCategory.FOOD, UItems.CRISPY_HAY_FRIES, 1F, 25, "hay_fries");
        offerSmelting(exporter, List.of(UItems.ZAP_APPLE), RecipeCategory.FOOD, UItems.COOKED_ZAP_APPLE, 1.2F, 430, "zap_apple");

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.ZAP_APPLE_JAM_JAR)
            .input(UItems.COOKED_ZAP_APPLE, 6).criterion(hasItem(UItems.COOKED_ZAP_APPLE), conditionsFromItem(UItems.COOKED_ZAP_APPLE))
            .input(UItems.EMPTY_JAR)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.JAM_TOAST, 8)
            .input(UItems.ZAP_APPLE_JAM_JAR).criterion(hasItem(UItems.ZAP_APPLE_JAM_JAR), conditionsFromItem(UItems.ZAP_APPLE_JAM_JAR))
            .input(UItems.TOAST, 8)
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, UItems.CANDIED_APPLE)
            .input(UConventionalTags.Items.STICKS)
            .input(UTags.Items.FRESH_APPLES).criterion(hasItem(Items.APPLE), conditionsFromTag(UTags.Items.FRESH_APPLES))
            .input(Items.SUGAR, 4)
            .offerTo(exporter);

        // trick apples
        offerTrickRecipe(exporter, UItems.GREEN_APPLE, Items.GREEN_DYE);
        offerTrickRecipe(exporter, Items.APPLE, Items.RED_DYE);
        offerTrickRecipe(exporter, UItems.SOUR_APPLE, Items.YELLOW_DYE);
        offerTrickRecipe(exporter, UItems.SWEET_APPLE, Items.ORANGE_DYE);
        offerTrickRecipe(exporter, UItems.ROTTEN_APPLE, Items.ROTTEN_FLESH);
        offerTrickRecipe(exporter, UItems.COOKED_ZAP_APPLE, Items.SPIDER_EYE);
        offerTrickRecipe(exporter, Items.GOLDEN_APPLE, Items.GOLD_NUGGET);
        offerTrickRecipe(exporter, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLD_INGOT);
        offerTrickRecipe(exporter, UItems.MANGO, Items.LIME_DYE);
        offerTrickRecipe(exporter, UItems.PINEAPPLE, UItems.PINEAPPLE_CROWN);
        offerTrickRecipe(exporter, UItems.HORSE_SHOE_FRIES, UItems.IRON_HORSE_SHOE);
        offerTrickRecipe(exporter, UItems.MUFFIN, UItems.ROCK);
    }

    public static void offerTrickRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        TrickCraftingRecipeJsonBuilder.create(RecipeCategory.FOOD, output)
            .input(UItems.ZAP_APPLE).criterion(hasItem(UItems.ZAP_APPLE), conditionsFromItem(UItems.ZAP_APPLE))
            .input(input)
            .offerTo(exporter, convertBetween(output, input) + "_trick");
    }

    private void offerSeaponyRecipes(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.SHELLY)
            .input('C', UItems.CLAM_SHELL).criterion("has_clam_shell", conditionsFromItem(UItems.CLAM_SHELL))
            .input('o', UItems.ROCK_CANDY)
            .pattern("o o")
            .pattern(" C ")
            .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, UItems.PEARL_NECKLACE)
            .input('#', UTags.Items.SHELLS).criterion("has_shell", conditionsFromTag(UTags.Items.SHELLS))
            .input('~', Items.STRING)
            .pattern("# #")
            .pattern("# #")
            .pattern("~#~")
            .offerTo(exporter);
    }

    private void offerEarthPonyRecipes(Consumer<RecipeJsonProvider> exporter) {
        Arrays.stream(ItemFamilies.BASKETS).forEach(basket -> offerBasketRecipe(exporter, basket, CraftingMaterialHelper.getMaterial(basket, "_basket", "_planks")));
        Arrays.stream(ItemFamilies.HORSE_SHOES).forEach(horseshoe -> offerHorseshoeRecipe(exporter, horseshoe, CraftingMaterialHelper.getMaterial(horseshoe, "_horse_shoe", "_ingot")));
        Arrays.stream(ItemFamilies.POLEARMS).forEach(polearm -> {
            if (polearm == UItems.NETHERITE_POLEARM) {
                offerNetheriteUpgradeRecipe(exporter, UItems.DIAMOND_POLEARM, RecipeCategory.TOOLS, UItems.NETHERITE_POLEARM);
            } else {
                offerPolearmRecipe(exporter, polearm, CraftingMaterialHelper.getMaterial(polearm, "_polearm", "_ingot"));
            }
        });
        // weather vane
        offerWeatherVaneRecipe(exporter, UBlocks.WEATHER_VANE, Items.IRON_NUGGET);

        // Giant balloons
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.GIANT_BALLOON)
            .input('-', ItemTags.WOOL_CARPETS).criterion("has_carpet", conditionsFromTag(ItemTags.WOOL_CARPETS))
            .input('#', ItemTags.WOOL).criterion("has_wool", conditionsFromTag(ItemTags.WOOL))
            .pattern("---")
            .pattern("# #")
            .pattern("---")
            .offerTo(exporter);

        // worms
        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, UItems.WHEAT_WORMS, RecipeCategory.BUILDING_BLOCKS, UBlocks.WORM_BLOCK);
        // fishing
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, UItems.BAITED_FISHING_ROD)
            .input(Items.FISHING_ROD).criterion(hasItem(Items.FISHING_ROD), conditionsFromItem(Items.FISHING_ROD))
            .input(UItems.WHEAT_WORMS)
            .group("fishing_rod")
            .offerTo(exporter);

        // utility
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.DIRT)
            .input('*', UItems.WHEAT_WORMS).criterion("has_wheat_worms", conditionsFromItem(UItems.WHEAT_WORMS))
            .input('#', ItemTags.SAND).criterion("has_sand", conditionsFromTag(ItemTags.SAND))
            .pattern("*#")
            .pattern("#*")
            .offerTo(exporter, convertBetween(Items.DIRT, UItems.WHEAT_WORMS));

        offerShapelessRecipe(exporter, Items.BONE_MEAL, UTags.Items.SHELLS, "bonemeal", 3);

        // pegasus feathers for non pegasi
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, UItems.PEGASUS_FEATHER)
            .input('*', Items.GHAST_TEAR).criterion("has_ghast_tear", conditionsFromItem(Items.GHAST_TEAR))
            .input('#', UItems.GRYPHON_FEATHER).criterion("has_feather", conditionsFromItem(UItems.GRYPHON_FEATHER))
            .pattern("***")
            .pattern("*#*")
            .pattern("***")
            .offerTo(exporter);

        offer2x2CompactingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, Items.COBBLESTONE, UItems.ROCK);
        offerReversibleCompactingRecipesWithReverseRecipeGroup(exporter, RecipeCategory.MISC, UItems.PEBBLES, RecipeCategory.BUILDING_BLOCKS, Blocks.GRAVEL, convertBetween(UItems.PEBBLES, Blocks.GRAVEL), "pebbles");
        offerShapelessRecipe(exporter, UItems.PEBBLES, Blocks.SUSPICIOUS_GRAVEL, "pebbles", 9);
        offerSmelting(exporter, List.of(UItems.GOLDEN_OAK_SEEDS, UItems.GOLDEN_FEATHER), RecipeCategory.MISC, Items.GOLD_NUGGET, 3F, 10, "gold_nugget");

        offerGrowing(exporter, UBlocks.CURING_JOKE, Blocks.LAPIS_BLOCK, Blocks.CORNFLOWER);
        offerGrowing(exporter, UBlocks.GOLD_ROOT, Blocks.RAW_GOLD_BLOCK, Blocks.CARROTS);
        offerGrowing(exporter, UTreeGen.GOLDEN_APPLE_TREE.sapling().get(), Blocks.RAW_GOLD_BLOCK, Blocks.OAK_SAPLING);
        offerGrowing(exporter, UBlocks.PLUNDER_VINE_BUD, Blocks.NETHERRACK, Blocks.WITHER_ROSE);
        offerGrowing(exporter, UTreeGen.ZAP_APPLE_TREE.sapling().get(), UBlocks.CHITIN, Blocks.DARK_OAK_SAPLING);
    }

    private static ShapelessRecipeJsonBuilder appendIngredients(ShapelessRecipeJsonBuilder builder, ItemConvertible...ingredients) {
        for (ItemConvertible ingredient : ingredients) {
            builder.input(ingredient).criterion(hasItem(ingredient), conditionsFromItem(ingredient));
        }
        return builder;
    }

    public static void offerShapelessRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, TagKey<Item> input, @Nullable String group, int outputCount) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, output, outputCount)
            .input(input).criterion(CraftingMaterialHelper.hasTag(input), conditionsFromTag(input))
            .group(group)
            .offerTo(exporter, getItemPath(output) + "_from_" + input.id().getPath());
    }

    public static void offerPieRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible pie, ItemConvertible slice, ItemConvertible crust, TagKey<Item> filling) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.FOOD, pie)
            .input('*', crust).criterion("has_crust", conditionsFromItem(crust))
            .input('#', filling).criterion("has_filling", conditionsFromTag(filling))
            .pattern("***")
            .pattern("###")
            .pattern("***")
            .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, pie)
            .input(slice, 4)
            .criterion(hasItem(slice), conditionsFromItem(slice))
            .offerTo(exporter, getItemPath(pie) + "_from_" + getItemPath(slice));
    }

    public static void offerBasketRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        CraftingMaterialHelper.input(ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output), '#', input)
            .criterion(CraftingMaterialHelper.hasEither(input), CraftingMaterialHelper.conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .group("basket")
            .offerTo(exporter);
    }

    public static void offerHorseshoeRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        CraftingMaterialHelper
            .input(ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output), '#', input)
            .criterion(CraftingMaterialHelper.hasEither(input), CraftingMaterialHelper.conditionsFromEither(input))
            .pattern("# #")
            .pattern("# #")
            .pattern(" # ")
            .group("horseshoe")
            .offerTo(exporter);
    }

    public static void offerHiveRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible chitin, ItemConvertible egg) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output)
            .input('#', chitin)
            .input('o', egg).criterion(hasItem(egg), conditionsFromItem(egg))
            .pattern(" # ")
            .pattern("#o#")
            .pattern(" # ")
            .group("chitin")
            .offerTo(exporter);
    }

    public static void offerPolearmRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input) {
        CraftingMaterialHelper
            .input(ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, output), 'o', input).criterion(CraftingMaterialHelper.hasEither(input), CraftingMaterialHelper.conditionsFromEither(input))
            .input('#', UConventionalTags.Items.STICKS)
            .pattern("  o")
            .pattern(" # ")
            .pattern("#  ")
            .group("polearm")
            .offerTo(exporter);
    }

    public static void offerHullRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible outside, ItemConvertible inside) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4)
            .input('#', outside).criterion(hasItem(outside), conditionsFromItem(outside))
            .input('o', inside).criterion(hasItem(inside), conditionsFromItem(inside))
            .pattern("##")
            .pattern("oo")
            .group("hull")
            .offerTo(exporter);
    }

    public static void offerSpikesRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 8)
            .input('#', input).criterion(hasItem(input), conditionsFromItem(input))
            .pattern(" # ")
            .pattern("###")
            .group("spikes")
            .offerTo(exporter);
    }

    public static void offerChestRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output)
            .input('#', input)
            .pattern("###")
            .pattern("# #")
            .pattern("###")
            .criterion("has_lots_of_items", new InventoryChangedCriterion.Conditions(LootContextPredicate.EMPTY, NumberRange.IntRange.atLeast(10), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, new ItemPredicate[0]))
            .offerTo(exporter);
    }

    public static void offer2x3Recipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input, String group) {
        createDoorRecipe(output, Ingredient.ofItems(input))
            .criterion(hasItem(input), conditionsFromItem(input))
            .group(group)
            .offerTo(exporter);
    }

    public static void offerStableDoorRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, Either<ItemConvertible, TagKey<Item>> body, ItemConvertible trim) {
        CraftingMaterialHelper
            .input(ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 3), '#', body).criterion(CraftingMaterialHelper.hasEither(body), CraftingMaterialHelper.conditionsFromEither(body))
            .input('*', trim).criterion(hasItem(trim), conditionsFromItem(trim))
            .pattern("*#*")
            .pattern("*#*")
            .pattern("*#*")
            .group("stable_door")
            .offerTo(exporter);
    }

    public static void offerWeatherVaneRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output)
            .input('*', input).criterion(hasItem(input), conditionsFromItem(input))
            .pattern(" **")
            .pattern("** ")
            .pattern(" * ")
            .offerTo(exporter);
    }

    public static ShapedRecipeJsonBuilder createCustomBedRecipe(ItemConvertible output, Either<ItemConvertible, TagKey<Item>> input, Either<ItemConvertible, TagKey<Item>> planks) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output);
        CraftingMaterialHelper.input(builder, '#', input).criterion(CraftingMaterialHelper.hasEither(input), CraftingMaterialHelper.conditionsFromEither(input));
        return CraftingMaterialHelper.input(builder, 'X', planks)
            .pattern("###")
            .pattern("XXX")
            .group("bed");
    }

    private void offerBedSheetRecipes(Consumer<RecipeJsonProvider> exporter) {
        PatternTemplate.ONE_COLOR.offerWithoutConversion(exporter, UItems.KELP_BED_SHEETS, Items.KELP);
        WOOLS.forEach(wool -> PatternTemplate.ONE_COLOR.offerTo(exporter, CraftingMaterialHelper.getItem(Unicopia.id(Registries.ITEM.getId(wool).getPath().replace("_wool", "_bed_sheets"))), wool));

        PatternTemplate.TWO_COLOR.offerTo(exporter, UItems.APPLE_BED_SHEETS, Items.GREEN_WOOL, Items.LIME_WOOL);
        PatternTemplate.TWO_COLOR.offerTo(exporter, UItems.BARRED_BED_SHEETS, Items.LIGHT_BLUE_WOOL, Items.WHITE_WOOL);
        PatternTemplate.TWO_COLOR.offerTo(exporter, UItems.CHECKERED_BED_SHEETS, Items.GREEN_WOOL, Items.BROWN_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(exporter, UItems.RAINBOW_PWR_BED_SHEETS, Items.WHITE_WOOL, Items.PINK_WOOL, Items.RED_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(exporter, UItems.RAINBOW_BPY_BED_SHEETS, Items.PINK_WOOL, Items.YELLOW_WOOL, Items.LIGHT_BLUE_WOOL);
        PatternTemplate.THREE_COLOR.offerTo(exporter, UItems.RAINBOW_BPW_BED_SHEETS, Items.PINK_WOOL, Items.LIGHT_BLUE_WOOL, Items.WHITE_WOOL);
        PatternTemplate.FOUR_COLOR.offerTo(exporter, UItems.RAINBOW_PBG_BED_SHEETS, Items.PURPLE_WOOL, Items.WHITE_WOOL, Items.LIGHT_GRAY_WOOL, Items.BLACK_WOOL);
        PatternTemplate.SEVEN_COLOR.offerTo(exporter, UItems.RAINBOW_BED_SHEETS, UItems.RAINBOW_BED_SHEETS, Items.LIGHT_BLUE_WOOL, Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL, Items.BLUE_WOOL, Items.GREEN_WOOL, Items.PURPLE_WOOL);
    }

    public static void offerCompactingRecipe(Consumer<RecipeJsonProvider> exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, int resultCount) {
        offerCompactingRecipe(exporter, category, output, input, hasItem(input), resultCount);
    }

    public static void offerCompactingRecipe(Consumer<RecipeJsonProvider> exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, String criterionName, int resultCount) {
        ShapelessRecipeJsonBuilder.create(category, output, resultCount)
            .input(input, 9).criterion(criterionName, conditionsFromItem(input))
            .offerTo(exporter);
    }

    public static void offerWaxingRecipes(Consumer<RecipeJsonProvider> exporter) {
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, output) -> {
            Block input = UBlockFamilies.ZAP.getVariant(variant);
            offerWaxingRecipe(exporter, output, input);
        });
        offerWaxingRecipe(exporter, UBlocks.WAXED_ZAP_PLANKS, UBlocks.ZAP_PLANKS);
        offerWaxingRecipe(exporter, UBlocks.WAXED_ZAP_LOG, UBlocks.ZAP_LOG);
        offerWaxingRecipe(exporter, UBlocks.WAXED_ZAP_WOOD, UBlocks.ZAP_WOOD);
        offerWaxingRecipe(exporter, UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG);
        offerWaxingRecipe(exporter, UBlocks.WAXED_STRIPPED_ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD);
    }

    public static void offerWaxingRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output)
            .input(Items.HONEYCOMB)
            .input(input).criterion(hasItem(input), conditionsFromItem(input))
            .group(getItemPath(output))
            .offerTo(exporter, convertBetween(output, Items.HONEYCOMB));
    }

    public static void offerCloudShapingRecipe(Consumer<RecipeJsonProvider> exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        offerCloudShapingRecipe(exporter, category, output, input, 1);
    }

    public static void offerCloudShapingRecipe(Consumer<RecipeJsonProvider> exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, int count) {
        CraftingMaterialHelper.createCloudShaping(Ingredient.ofItems(input), category, output, count)
            .criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input))
            .offerTo(exporter, RecipeProvider.convertBetween(output, input) + "_cloud_shaping");
    }

    public static void offerGrowing(Consumer<RecipeJsonProvider> exporter, Block output, Block fuel, Block target) {
        GrowingRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output.getDefaultState())
            .fuel(fuel.getDefaultState())
            .target(target).criterion(hasItem(target), conditionsFromItem(target))
            .offerTo(exporter);
    }

    public void offerSpell(Consumer<RecipeJsonProvider> exporter, ItemConvertible gemstone, SpellType<?> output, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromSpell(Consumer<RecipeJsonProvider> exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromTwoSpells(Consumer<RecipeJsonProvider> exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input1, SpellType<?> input2, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input1)
            .input(gemstone, input2)
            .traits(traits)
            .offerTo(exporter);
    }

    public static InventoryChangedCriterion.Conditions conditionsFromMultipleItems(ItemConvertible... items) {
        return conditionsFromItemPredicates(
            Stream.of(items).map(item -> ItemPredicate.Builder.create().items(item).build()).toArray(ItemPredicate[]::new)
        );
    }
}
