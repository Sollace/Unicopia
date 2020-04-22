package com.minelittlepony.unicopia.item;

import static com.minelittlepony.unicopia.EquinePredicates.*;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.magic.spell.ScorchSpell;
import com.minelittlepony.unicopia.toxin.DynamicToxicBlockItem;
import com.minelittlepony.unicopia.toxin.DynamicToxicItem;
import com.minelittlepony.unicopia.toxin.ToxicBlockItem;
import com.minelittlepony.unicopia.toxin.ToxicItem;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.toxin.Toxin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.TallBlockItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

public interface UItems {

    AppleItem green_apple = register(new AppleItem(FoodComponents.APPLE), "apple_green");
    AppleItem sweet_apple = register(new AppleItem(FoodComponents.APPLE), "apple_sweet");
    AppleItem sour_apple = register(new AppleItem(FoodComponents.APPLE), "apple_sour");

    ZapAppleItem zap_apple = register(new ZapAppleItem(), "zap_apple");

    AppleItem rotten_apple = register(new RottenAppleItem(FoodComponents.APPLE), "rotten_apple");
    AppleItem cooked_zap_apple = register(new AppleItem(FoodComponents.APPLE), "cooked_zap_apple");

    Item cloud_matter = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "cloud_matter");
    Item dew_drop = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "dew_drop");

    CloudPlacerItem racing_cloud_spawner = register(new CloudPlacerItem(UEntities.RACING_CLOUD), "racing_cloud_spawner");
    CloudPlacerItem construction_cloud_spawner = register(new CloudPlacerItem(UEntities.CONSTRUCTION_CLOUD), "construction_cloud_spawner");
    CloudPlacerItem wild_cloud_spawner = register(new CloudPlacerItem(UEntities.WILD_CLOUD), "wild_cloud_spawner");

    Item cloud_block = register(new PredicatedBlockItem(UBlocks.normal_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "cloud_block");
    Item enchanted_cloud = register(new PredicatedBlockItem(UBlocks.enchanted_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "enchanted_cloud_block");
    Item packed_cloud = register(new PredicatedBlockItem(UBlocks.packed_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "packed_cloud_block");

    Item cloud_stairs = register(new PredicatedBlockItem(UBlocks.cloud_stairs, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "cloud_stairs");

    Item cloud_fence = register(new PredicatedBlockItem(UBlocks.cloud_fence, new Item.Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_fence");

    Item anvil = register(new PredicatedBlockItem(UBlocks.anvil, new Item.Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_anvil");

    Item record_crusade = register(createRecord(USounds.RECORD_CRUSADE), "crusade");
    Item record_pet = register(createRecord(USounds.RECORD_PET), "pet");
    Item record_popular = register(createRecord(USounds.RECORD_POPULAR), "popular");
    Item record_funk = register(createRecord(USounds.RECORD_FUNK), "funk");

    Item hive = register(new BlockItem(UBlocks.hive, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "hive");
    Item chitin_shell = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "chitin_shell");
    Item chitin = register(new BlockItem(UBlocks.chitin_block, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "chitin_block");
    Item chissled_chitin = register(new BlockItem(UBlocks.chissled_chitin, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "chissled_chitin");
    Item cuccoon = register(new BlockItem(UBlocks.cuccoon, new Item.Settings().group(ItemGroup.MATERIALS)), "cuccoon");
    Item slime_layer = register(new BlockItem(UBlocks.slime_layer, new Item.Settings().group(ItemGroup.DECORATIONS)), "slime_layer");

    Item mist_door = register(new TallBlockItem(UBlocks.mist_door, new Item.Settings().group(ItemGroup.REDSTONE)), "mist_door");
    Item library_door = register(new TallBlockItem(UBlocks.library_door, new Item.Settings().group(ItemGroup.REDSTONE)), "library_door");
    Item bakery_door = register(new TallBlockItem(UBlocks.bakery_door, new Item.Settings().group(ItemGroup.REDSTONE)), "bakery_door");
    Item diamond_door = register(new TallBlockItem(UBlocks.diamond_door, new Item.Settings().group(ItemGroup.REDSTONE)), "diamond_door");

    Item sugar_block = register(new BlockItem(UBlocks.sugar_block, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "sugar_block");

    Item cloud_slab = new PredicatedBlockItem(UBlocks.cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);
    Item enchanted_cloud_slab = new PredicatedBlockItem(UBlocks.enchanted_cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);
    Item packed_cloud_slab = new PredicatedBlockItem(UBlocks.packed_cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);

    MagicGemItem spell = register(new MagicGemItem(), "gem");
    MagicGemItem curse = register(new CursedMagicGemItem(), "corrupted_gem");

    BagOfHoldingItem bag_of_holding = register(new BagOfHoldingItem(), "bag_of_holding");
    AlicornAmuletItem alicorn_amulet = register(new AlicornAmuletItem(), "alicorn_amulet");

    SpellbookItem spellbook = register(new SpellbookItem(), "spellbook");
    Item staff_meadow_brook = register(new StaffItem(new Item.Settings().maxCount(1).maxDamage(2)), "staff_meadow_brook");
    Item staff_remembrance = register(new EnchantedStaffItem(new Item.Settings(), new ScorchSpell()), "staff_remembrance");

    Item spear = register(new SpearItem(new Item.Settings().maxCount(1).maxDamage(500)), "spear");

    MossItem moss = register(new MossItem(new Item.Settings()), "moss");

    Item alfalfa_seeds = register(new AliasedBlockItem(UBlocks.alfalfa, new Item.Settings()
            .group(ItemGroup.MATERIALS)
            .food(new FoodComponent.Builder()
                    .hunger(1)
                    .saturationModifier(4)
                    .build())), "alfalfa_seeds");

    Item enchanted_torch = register(new BlockItem(UBlocks.enchanted_torch, new Item.Settings().group(ItemGroup.DECORATIONS)), "enchanted_torch");

    Item alfalfa_leaves = register(new Item(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(1)
                    .saturationModifier(3)
                    .build())), "alfalfa_leaves");

    Item cereal = register(new SugaryItem(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(9)
                    .saturationModifier(0.8F)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 1), "cereal");
    Item sugar_cereal = register(new SugaryItem(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(20)
                    .saturationModifier(-2)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 110), "sugar_cereal");

    TomatoItem tomato = register(new TomatoItem(4, 34), "tomato");
    RottenTomatoItem rotten_tomato = register(new RottenTomatoItem(4, 34), "rotten_tomato");

    TomatoItem cloudsdale_tomato = register(new TomatoItem(16, 4), "cloudsdale_tomato");
    RottenTomatoItem rotten_cloudsdale_tomato = register(new RottenTomatoItem(5, 34), "rotten_cloudsdale_tomato");

    TomatoSeedsItem tomato_seeds = register(new TomatoSeedsItem(), "tomato_seeds");

    Item apple_seeds = register(new BlockItem(UBlocks.apple_tree, new Item.Settings().group(ItemGroup.DECORATIONS)), "apple_seeds");
    Item apple_leaves = register(new BlockItem(UBlocks.apple_leaves, new Item.Settings().group(ItemGroup.DECORATIONS)), "apple_leaves");

    Item daffodil_daisy_sandwich = register(new DynamicToxicItem(new Item.Settings(), 3, 2, UseAction.EAT, Toxicity::fromStack), "daffodil_daisy_sandwich");
    Item hay_burger = register(new DynamicToxicItem(new Item.Settings(), 3, 4, UseAction.EAT, Toxicity::fromStack), "hay_burger");
    Item hay_fries = register(new ToxicItem(new Item.Settings(), 1, 5, UseAction.EAT, Toxicity.SAFE), "hay_fries");
    Item salad = register(new DynamicToxicItem(new Item.Settings().recipeRemainder(Items.BOWL), 4, 2, UseAction.EAT, Toxicity::fromStack), "salad");

    Item wheat_worms = register(new ToxicItem(new Item.Settings(), 1, 0, UseAction.EAT, Toxicity.SEVERE), "wheat_worms");
    Item mug = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "mug");
    Item apple_cider = register(new ToxicItem(new Item.Settings().recipeRemainder(mug), 4, 2, UseAction.DRINK, Toxicity.MILD), "apple_cider");
    Item juice = register(new ToxicItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE), 2, 2, UseAction.DRINK, Toxicity.SAFE), "juice");
    Item burned_juice = register(new ToxicItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE), 3, 1, UseAction.DRINK, Toxicity.FAIR), "burned_juice");

    static <T extends Item> T register(T newItem, Item oldItem) {
        return Registry.ITEM.add(Registry.ITEM.getId(oldItem), newItem);
    }

    static <T extends Item> T register(T item, String name) {
        return register(item, name);
    }

    static MusicDiscItem createRecord(SoundEvent sound) {
        return new MusicDiscItem(1, sound, new Item.Settings()
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
        StickItem stick = register(new StickItem(), Items.STICK);
        ExtendedShearsItem shears = register(new ExtendedShearsItem(), Items.SHEARS);

        AppleItem red_apple = register(new AppleItem(FoodComponents.APPLE), Items.APPLE);

        Item grass = register(new DynamicToxicBlockItem(Blocks.GRASS, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.NAUSEA), Items.GRASS);
        Item fern = register(new DynamicToxicBlockItem(Blocks.FERN, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.STRENGTH), Items.FERN);
        Item dead_bush = register(new DynamicToxicBlockItem(Blocks.DEAD_BUSH, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.NAUSEA), Items.DEAD_BUSH);

        Item dandelion = register(new ToxicBlockItem(Blocks.DANDELION, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.DANDELION);
        Item poppy = register(new ToxicBlockItem(Blocks.POPPY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE), Items.POPPY);
        Item blue_orchid = register(new ToxicBlockItem(Blocks.BLUE_ORCHID, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.BLUE_ORCHID);
        Item allium = register(new ToxicBlockItem(Blocks.ALLIUM, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.FAIR), Items.ALLIUM);
        Item azure_bluet = register(new DynamicToxicBlockItem(Blocks.AZURE_BLUET, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.RADIOACTIVITY), Items.AZURE_BLUET);
        Item red_tulip = register(new ToxicBlockItem(Blocks.RED_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.RED_TULIP);
        Item orange_tulip = register(new ToxicBlockItem(Blocks.ORANGE_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.ORANGE_TULIP);
        Item white_tulip = register(new ToxicBlockItem(Blocks.WHITE_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.FAIR), Items.WHITE_TULIP);
        Item pink_tulip = register(new ToxicBlockItem(Blocks.PINK_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.PINK_TULIP);
        Item oxeye_daisy = register(new DynamicToxicBlockItem(Blocks.OXEYE_DAISY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.BLINDNESS), Items.OXEYE_DAISY);
        Item cornflower = register(new ToxicBlockItem(Blocks.CORNFLOWER, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.CORNFLOWER);

        Item rose_bush = register(new DynamicToxicBlockItem(Blocks.ROSE_BUSH, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.DAMAGE), Items.ROSE_BUSH);
        Item peony = register(new ToxicBlockItem(Blocks.PEONY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.PEONY);
        Item tall_grass = register(new ToxicBlockItem(Blocks.TALL_GRASS, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.TALL_GRASS);
        Item large_fern = register(new DynamicToxicBlockItem(Blocks.LARGE_FERN, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.DAMAGE), Items.LARGE_FERN);
    }
}
