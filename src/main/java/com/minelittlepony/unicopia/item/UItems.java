package com.minelittlepony.unicopia.item;

import static com.minelittlepony.unicopia.EquinePredicates.*;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.magic.spell.ScorchSpell;
import com.minelittlepony.unicopia.toxin.ToxicBlockItem;
import com.minelittlepony.unicopia.toxin.ToxicItem;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.toxin.Toxin;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.TallBlockItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

public interface UItems {

    AppleItem GREEN_APPLE = register(new AppleItem(FoodComponents.APPLE), "apple_green");
    AppleItem SWEET_APPLE = register(new AppleItem(FoodComponents.APPLE), "apple_sweet");
    AppleItem SOUR_APPLE = register(new AppleItem(FoodComponents.APPLE), "apple_sour");

    ZapAppleItem ZAP_APPLE = register(new ZapAppleItem(), "zap_apple");

    AppleItem ROTTEN_APPLE = register(new RottenAppleItem(FoodComponents.APPLE), "rotten_apple");
    AppleItem COOKED_ZAP_APPLE = register(new AppleItem(FoodComponents.APPLE), "cooked_zap_apple");

    Item CLOUD_MATTER = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "cloud_matter");
    Item DEW_DROP = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "dew_drop");

    CloudPlacerItem RACING_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.RACING_CLOUD), "racing_cloud_spawner");
    CloudPlacerItem CONSTRUCTION_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.CONSTRUCTION_CLOUD), "construction_cloud_spawner");
    CloudPlacerItem WILD_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.WILD_CLOUD), "wild_cloud_spawner");

    Item CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "cloud_block");
    Item ENCHANTED_CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "enchanted_cloud_block");
    Item PACKED_CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.DENSE_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "packed_cloud_block");

    Item CLOUD_STAIRS = register(new PredicatedBlockItem(UBlocks.CLOUD_STAIRS, new Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "cloud_stairs");

    Item CLOUD_FENCE = register(new PredicatedBlockItem(UBlocks.CLOUD_FENCE, new Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_fence");

    Item CLOUD_ANVIL = register(new PredicatedBlockItem(UBlocks.CLOUD_ANVIL, new Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_anvil");

    Item MUSIC_DISC_CRUSADE = register(createRecord(USounds.RECORD_CRUSADE), "music_disc_crusade");
    Item MUSIC_DISC_PET = register(createRecord(USounds.RECORD_PET), "music_disc_pet");
    Item MUSIC_DISC_POPULAR = register(createRecord(USounds.RECORD_POPULAR), "music_disc_popular");
    Item MUSIC_DISC_FUNK = register(createRecord(USounds.RECORD_FUNK), "music_disc_funk");

    Item HIVE_WALL_BLOCK = register(new BlockItem(UBlocks.HIVE_WALL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)), "hive_wall_block");
    Item CHITIN_SHELL = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "chitin_shell");
    Item CHITIN_SHELL_BLOCK = register(new BlockItem(UBlocks.CHITIN_SHELL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)), "chitin_shell_block");
    Item CHISELED_CHITIN_SHELL_BLOCK = register(new BlockItem(UBlocks.CHISELED_CHITIN_SHELL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)), "chiseled_chitin_shell_block");
    Item SLIME_DROP = register(new BlockItem(UBlocks.SLIME_DROP, new Settings().group(ItemGroup.MATERIALS)), "slime_drop");
    Item SLIME_LAYER = register(new BlockItem(UBlocks.SLIME_LAYER, new Settings().group(ItemGroup.DECORATIONS)), "slime_layer");

    Item MISTED_GLASS_DOOR = register(new TallBlockItem(UBlocks.MISTED_GLASS_DOOR, new Settings().group(ItemGroup.REDSTONE)), "misted_glass_door");
    Item LIBRARY_DOOR = register(new TallBlockItem(UBlocks.LIBRARY_DOOR, new Settings().group(ItemGroup.REDSTONE)), "library_door");
    Item BAKERY_DOOR = register(new TallBlockItem(UBlocks.BAKERY_DOOR, new Settings().group(ItemGroup.REDSTONE)), "bakery_door");
    Item DIAMOND_DOOR = register(new TallBlockItem(UBlocks.DIAMOND_DOOR, new Settings().group(ItemGroup.REDSTONE)), "diamond_door");

    Item SUGAR_BLOCK = register(new BlockItem(UBlocks.SUGAR_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)), "sugar_block");

    Item CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "cloud_slab");
    Item ENCHANTED_CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "enchanted_cloud_slab");
    Item DENSE_CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.DENSE_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "dense_cloud_slab");

    MagicGemItem GEM = register(new MagicGemItem(), "gem");
    MagicGemItem CORRUPTED_GEM = register(new CursedMagicGemItem(), "corrupted_gem");

    BagOfHoldingItem BAG_OF_HOLDING = register(new BagOfHoldingItem(), "bag_of_holding");
    AlicornAmuletItem ALICORN_AMULET = register(new AlicornAmuletItem(), "alicorn_amulet");

    SpellbookItem SPELLBOOK = register(new SpellbookItem(), "spellbook");
    Item STAFF_MEADOW_BROOK = register(new StaffItem(new Settings().maxCount(1).maxDamage(2)), "staff_meadow_brook");
    Item STAFF_REMEMBERANCE = register(new EnchantedStaffItem(new Settings(), new ScorchSpell()), "staff_remembrance");

    Item SPEAR = register(new SpearItem(new Settings().maxCount(1).maxDamage(500)), "spear");

    MossItem MOSS = register(new MossItem(new Settings()
            .food(UFoodComponents.RANDOM_FOLIAGE)), "moss");

    Item ALFALFA_SEEDS = register(new AliasedBlockItem(UBlocks.ALFALFA_CROPS, new Settings()
            .group(ItemGroup.MATERIALS)
            .food(UFoodComponents.ALFALFA_SEEDS)), "alfalfa_seeds");
    Item ALFALFA_LEAVES = register(new Item(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.ALFALFA_LEAVES)), "alfalfa_leaves");

    Item ENCHANTED_TORCH = register(new BlockItem(UBlocks.ENCHANTED_TORCH, new Settings().group(ItemGroup.DECORATIONS)), "enchanted_torch");


    Item CEREAL = register(new SugaryItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(9)
                    .saturationModifier(0.8F)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 1), "cereal");
    Item SUGAR_CEREAL = register(new SugaryItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(20)
                    .saturationModifier(-2)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 110), "sugar_cereal");

    TomatoSeedsItem TOMATO_SEEDS = register(new TomatoSeedsItem(new Settings().group(ItemGroup.MATERIALS)), "tomato_seeds");
    TomatoItem TOMATO = register(new TomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.TOMATO)), "tomato");
    RottenTomatoItem ROTTEN_TOMATO = register(new RottenTomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.BAD_TOMATO)), "rotten_tomato");

    TomatoItem CLOUDSDALE_TOMATO = register(new TomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.GOOD_TOMATO)), "cloudsdale_tomato");
    RottenTomatoItem ROTTEN_CLOUDSDALE_TOMATO = register(new RottenTomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.BAD_TOMATO)), "rotten_cloudsdale_tomato");

    Item APPLE_SEEDS = register(new BlockItem(UBlocks.APPLE_SAPLING, new Settings().group(ItemGroup.DECORATIONS)), "apple_seeds");
    Item APPLE_LEAVES = register(new BlockItem(UBlocks.APPLE_LEAVES, new Settings().group(ItemGroup.DECORATIONS)), "apple_leaves");

    Item DAFFODIL_DAISY_SANDWICH = register(new ToxicItem(new Settings()
            .food(UFoodComponents.DAFODIL_DAISY_SANDWICH), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "daffodil_daisy_sandwich");
    Item HAY_BURGER = register(new ToxicItem(new Settings()
            .food(UFoodComponents.HAY_BURGER), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "hay_burger");
    Item HAY_FRIES = register(new ToxicItem(new Settings()
            .food(UFoodComponents.HAY_FRIES), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), "hay_fries");
    Item SALAD = register(new ToxicItem(new Settings()
            .food(UFoodComponents.SALAD)
            .recipeRemainder(Items.BOWL), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "salad");

    Item WHEAT_WORMS = register(new ToxicItem(new Settings()
            .food(UFoodComponents.WORMS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD), "wheat_worms");
    Item MUG = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "mug");
    Item CIDER = register(new ToxicItem(new Settings()
            .food(UFoodComponents.SALAD)
            .recipeRemainder(MUG), UseAction.DRINK, Toxicity.MILD, Toxin.FOOD), "apple_cider");
    Item JUICE = register(new ToxicItem(new Settings()
            .recipeRemainder(Items.GLASS_BOTTLE)
            .food(UFoodComponents.JUICE), UseAction.DRINK, Toxicity.SAFE, Toxin.FOOD), "juice");
    Item BURNED_JUICE = register(new ToxicItem(new Settings()
            .recipeRemainder(Items.GLASS_BOTTLE)
            .food(UFoodComponents.BURNED_JUICE), UseAction.DRINK, Toxicity.FAIR, Toxin.FOOD), "burned_juice");

    Item CLOUD_SPAWN_EGG = register(new SpawnEggItem(UEntities.CLOUD, 0x4169e1, 0x7fff00, new Settings().group(ItemGroup.MISC)), "cloud_spawn_egg");
    Item BUTTERFLY_SPAWN_EGG = register(new SpawnEggItem(UEntities.BUTTERFLY, 0x222200, 0xaaeeff, new Settings().group(ItemGroup.MISC)), "butterfly_spawn_egg");

    static <T extends Item> T register(T item, String name) {
        return Registry.ITEM.add(new Identifier("unicopia", name), item);
    }

    static MusicDiscItem createRecord(SoundEvent sound) {
        return new MusicDiscItem(1, sound, new Settings()
                .maxCount(1)
                .group(ItemGroup.MISC)
                .rarity(Rarity.RARE)
            ) {};
    }

    static void bootstrap() {
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(cuccoon), new ItemStack(chitin_shell), 0.3F);
    }

    interface VanillaOverrides {
        StickItem STICK = register(new StickItem(), Items.STICK);
        ExtendedShearsItem SHEARS = register(new ExtendedShearsItem(), Items.SHEARS);

        AppleItem APPLE = register(new AppleItem(FoodComponents.APPLE), Items.APPLE);

        Item GRASS = register(new ToxicBlockItem(Blocks.GRASS, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.NAUSEA)), Items.GRASS);
        Item FERN = register(new ToxicBlockItem(Blocks.FERN, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.STRENGTH)), Items.FERN);
        Item DEAD_BUSH = register(new ToxicBlockItem(Blocks.DEAD_BUSH, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.NAUSEA)), Items.DEAD_BUSH);

        Item DANDELION = register(new ToxicBlockItem(Blocks.DANDELION, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.DANDELION);
        Item POPPY = register(new ToxicBlockItem(Blocks.POPPY, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD), Items.POPPY);
        Item BLUE_ORCHID = register(new ToxicBlockItem(Blocks.BLUE_ORCHID, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.BLUE_ORCHID);
        Item ALLIUM = register(new ToxicBlockItem(Blocks.ALLIUM, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.FAIR, Toxin.FOOD), Items.ALLIUM);
        Item AZUER_BLUET = register(new ToxicBlockItem(Blocks.AZURE_BLUET, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.RADIOACTIVITY)), Items.AZURE_BLUET);
        Item RED_TULIP = register(new ToxicBlockItem(Blocks.RED_TULIP, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.RED_TULIP);
        Item ORANGE_TULIP = register(new ToxicBlockItem(Blocks.ORANGE_TULIP, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.ORANGE_TULIP);
        Item WHITE_TULIP = register(new ToxicBlockItem(Blocks.WHITE_TULIP, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.FAIR, Toxin.FOOD), Items.WHITE_TULIP);
        Item PINK_TULIP = register(new ToxicBlockItem(Blocks.PINK_TULIP, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.PINK_TULIP);
        Item OXEYE_DAISY = register(new ToxicBlockItem(Blocks.OXEYE_DAISY, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.BLINDNESS)), Items.OXEYE_DAISY);
        Item CORNFLOWER = register(new ToxicBlockItem(Blocks.CORNFLOWER, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.CORNFLOWER);

        Item ROSE_BUSH = register(new ToxicBlockItem(Blocks.ROSE_BUSH, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.DAMAGE)), Items.ROSE_BUSH);
        Item PEONY = register(new ToxicBlockItem(Blocks.PEONY, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.PEONY);
        Item TALL_GRASS = register(new ToxicBlockItem(Blocks.TALL_GRASS, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), Items.TALL_GRASS);
        Item LARGE_FERN = register(new ToxicBlockItem(Blocks.LARGE_FERN, new Settings()
                .food(UFoodComponents.RANDOM_FOLIAGE)
                .group(ItemGroup.DECORATIONS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.DAMAGE)), Items.LARGE_FERN);

        static <T extends Item> T register(T newItem, Item oldItem) {
            return Registry.ITEM.set(Registry.ITEM.getRawId(oldItem), Registry.ITEM.getId(oldItem), newItem);
        }
    }
}
