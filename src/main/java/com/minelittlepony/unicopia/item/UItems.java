package com.minelittlepony.unicopia.item;

import static com.minelittlepony.unicopia.EquinePredicates.*;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.magic.spell.ScorchSpell;
import com.minelittlepony.unicopia.toxin.ToxicItem;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.toxin.Toxin;
import com.minelittlepony.unicopia.toxin.Toxics;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.TallBlockItem;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

public interface UItems {

    AppleItem GREEN_APPLE = register("green_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem SWEET_APPLE = register("sweet_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem SOUR_APPLE = register("sour_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));

    ZapAppleItem ZAP_APPLE = register("zap_apple", new ZapAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(UFoodComponents.ZAP_APPLE)));

    AppleItem ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem COOKED_ZAP_APPLE = register("cooked_zap_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));

    Item CLOUD_MATTER = register("cloud_matter", new Item(new Settings().group(ItemGroup.MATERIALS)));
    Item DEW_DROP = register("dew_drop", new Item(new Settings().group(ItemGroup.MATERIALS)));

    CloudPlacerItem RACING_CLOUD_SPAWNER = register("racing_cloud_spawner", new CloudPlacerItem(UEntities.RACING_CLOUD));
    CloudPlacerItem CONSTRUCTION_CLOUD_SPAWNER = register("construction_cloud_spawner", new CloudPlacerItem(UEntities.CONSTRUCTION_CLOUD));
    CloudPlacerItem WILD_CLOUD_SPAWNER = register("wild_cloud_spawner", new CloudPlacerItem(UEntities.WILD_CLOUD));

    Item CLOUD_BLOCK = register("cloud_block", new PredicatedBlockItem(UBlocks.CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS));
    Item ENCHANTED_CLOUD_BLOCK = register("enchanted_cloud_block", new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS));
    Item DENSE_CLOUD_BLOCK = register("dense_cloud_block", new PredicatedBlockItem(UBlocks.DENSE_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS));

    Item CLOUD_STAIRS = register("cloud_stairs", new PredicatedBlockItem(UBlocks.CLOUD_STAIRS, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS));
    Item CLOUD_FENCE = register("cloud_fence", new PredicatedBlockItem(UBlocks.CLOUD_FENCE, new Settings().group(ItemGroup.DECORATIONS), PLAYER_PEGASUS));
    Item CLOUD_ANVIL = register("cloud_anvil", new PredicatedBlockItem(UBlocks.CLOUD_ANVIL, new Settings().group(ItemGroup.DECORATIONS), PLAYER_PEGASUS));

    Item MUSIC_DISC_CRUSADE = register("music_disc_crusade", USounds.RECORD_CRUSADE);
    Item MUSIC_DISC_PET = register("music_disc_pet", USounds.RECORD_PET);
    Item MUSIC_DISC_POPULAR = register("music_disc_popular", USounds.RECORD_POPULAR);
    Item MUSIC_DISC_FUNK = register("music_disc_funk", USounds.RECORD_FUNK);

    Item HIVE_WALL_BLOCK = register("hive_wall_block", new BlockItem(UBlocks.HIVE_WALL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)));
    Item CHITIN_SHELL = register("chitin_shell", new Item(new Settings().group(ItemGroup.MATERIALS)));
    Item CHITIN_SHELL_BLOCK = register("chitin_shell_block", new BlockItem(UBlocks.CHITIN_SHELL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)));
    Item CHISELED_CHITIN_SHELL_BLOCK = register("chiseled_chitin_shell_block", new BlockItem(UBlocks.CHISELED_CHITIN_SHELL_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)));
    Item SLIME_DROP = register("slime_drop", new BlockItem(UBlocks.SLIME_DROP, new Settings().group(ItemGroup.MATERIALS)));
    Item SLIME_LAYER = register("slime_layer", new BlockItem(UBlocks.SLIME_LAYER, new Settings().group(ItemGroup.DECORATIONS)));

    Item MISTED_GLASS_DOOR = register("misted_glass_door", new TallBlockItem(UBlocks.MISTED_GLASS_DOOR, new Settings().group(ItemGroup.REDSTONE)));
    Item LIBRARY_DOOR = register("library_door", new TallBlockItem(UBlocks.LIBRARY_DOOR, new Settings().group(ItemGroup.REDSTONE)));
    Item BAKERY_DOOR = register("bakery_door", new TallBlockItem(UBlocks.BAKERY_DOOR, new Settings().group(ItemGroup.REDSTONE)));
    Item DIAMOND_DOOR = register("diamond_door", new TallBlockItem(UBlocks.DIAMOND_DOOR, new Settings().group(ItemGroup.REDSTONE)));

    Item SUGAR_BLOCK = register("sugar_block", new BlockItem(UBlocks.SUGAR_BLOCK, new Settings().group(ItemGroup.BUILDING_BLOCKS)));

    Item CLOUD_SLAB = register("cloud_slab", new PredicatedBlockItem(UBlocks.CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS));
    Item ENCHANTED_CLOUD_SLAB = register("enchanted_cloud_slab", new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS));
    Item DENSE_CLOUD_SLAB = register("dense_cloud_slab", new PredicatedBlockItem(UBlocks.DENSE_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS));

    MagicGemItem GEM = register("gem", new MagicGemItem(new Settings().maxCount(16).group(ItemGroup.BREWING)));
    MagicGemItem CORRUPTED_GEM = register("corrupted_gem", new CursedMagicGemItem(new Settings().maxCount(16).group(ItemGroup.BREWING)));

    BagOfHoldingItem BAG_OF_HOLDING = register("bag_of_holding", new BagOfHoldingItem(new Settings().group(ItemGroup.TRANSPORTATION).rarity(Rarity.UNCOMMON).maxCount(1)));
    AlicornAmuletItem ALICORN_AMULET = register("alicorn_amulet", new AlicornAmuletItem(new Settings().group(ItemGroup.COMBAT).rarity(Rarity.RARE).maxCount(1)));

    SpellbookItem SPELLBOOK = register("spellbook", new SpellbookItem(new Item.Settings().maxCount(1).group(ItemGroup.BREWING)));
    Item MEADOW_BROOK_STAFF = register("meadow_brook_staff", new StaffItem(new Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(2)));
    Item REMEMBERANCE_STAFF = register("remembrance_staff", new EnchantedStaffItem(new Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1), new ScorchSpell()));

    Item SPEAR = register("spear", new SpearItem(new Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(500)));

    MossItem MOSS = register("moss", new MossItem(new Settings().group(ItemGroup.FOOD).food(UFoodComponents.RANDOM_FOLIAGE)));

    Item ALFALFA_SEEDS = register("alfalfa_seeds", new AliasedBlockItem(UBlocks.ALFALFA_CROPS, new Settings().group(ItemGroup.MATERIALS).food(UFoodComponents.ALFALFA_SEEDS)));
    Item ALFALFA_LEAVES = register("alfalfa_leaves", new Item(new Settings().group(ItemGroup.FOOD).food(UFoodComponents.ALFALFA_LEAVES)));

    Item ENCHANTED_TORCH = register("enchanted_torch", new WallStandingBlockItem(UBlocks.ENCHANTED_TORCH, UBlocks.ENCHANTED_WALL_TORCH, new Settings().group(ItemGroup.DECORATIONS)));

    Item CEREAL = register("cereal", new SugaryItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.CEREAL)
                    .maxCount(1)
                    .recipeRemainder(Items.BOWL), 1));
    Item BOOP_O_ROOPS = register("boop_o_roops", new SugaryItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.SUGAR)
                    .maxCount(1)
                    .recipeRemainder(Items.BOWL), 110));

    TomatoSeedsItem TOMATO_SEEDS = register("tomato_seeds", new TomatoSeedsItem(new Settings().group(ItemGroup.MATERIALS)));
    TomatoItem TOMATO = register("tomato", new TomatoItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.TOMATO)));
    RottenTomatoItem ROTTEN_TOMATO = register("rotten_tomato", new RottenTomatoItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.BAD_TOMATO)));

    TomatoItem CLOUDSDALE_TOMATO = register("cloudsdale_tomato", new TomatoItem(new Settings()
                    .rarity(Rarity.UNCOMMON)
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.GOOD_TOMATO)));
    RottenTomatoItem ROTTEN_CLOUDSDALE_TOMATO = register("rotten_cloudsdale_tomato", new RottenTomatoItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .rarity(Rarity.UNCOMMON)
                    .food(UFoodComponents.BAD_TOMATO)));

    Item APPLE_SEEDS = register("apple_seeds", new BlockItem(UBlocks.APPLE_SAPLING, new Settings().group(ItemGroup.DECORATIONS)));
    Item APPLE_LEAVES = register("apple_leaves", new BlockItem(UBlocks.APPLE_LEAVES, new Settings().group(ItemGroup.DECORATIONS)));

    Item DAFFODIL_DAISY_SANDWICH = register("daffodil_daisy_sandwich", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.DAFODIL_DAISY_SANDWICH), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD));
    Item HAY_BURGER = register("hay_burger", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.HAY_BURGER), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD));
    Item HAY_FRIES = register("hay_fries", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.HAY_FRIES), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD));
    Item SALAD = register("salad", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.SALAD)
                    .recipeRemainder(Items.BOWL), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD));

    Item WHEAT_WORMS = register("wheat_worms", new ToxicItem(new Settings()
                    .group(ItemGroup.MISC)
                    .food(UFoodComponents.WORMS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD));
    Item MUG = register("mug", new Item(new Settings().group(ItemGroup.MATERIALS)));
    Item CIDER = register("apple_cider", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .food(UFoodComponents.SALAD)
                    .recipeRemainder(MUG), UseAction.DRINK, Toxicity.MILD, Toxin.FOOD));
    Item JUICE = register("juice", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .recipeRemainder(Items.GLASS_BOTTLE)
                    .food(UFoodComponents.JUICE), UseAction.DRINK, Toxicity.SAFE, Toxin.FOOD));
    Item BURNED_JUICE = register("burned_juice", new ToxicItem(new Settings()
                    .group(ItemGroup.FOOD)
                    .recipeRemainder(Items.GLASS_BOTTLE)
                    .food(UFoodComponents.BURNED_JUICE), UseAction.DRINK, Toxicity.FAIR, Toxin.FOOD));

    Item CLOUD_SPAWN_EGG = register("cloud_spawn_egg", new SpawnEggItem(UEntities.CLOUD, 0x4169e1, 0x7fff00, new Settings().group(ItemGroup.MISC)));
    Item BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg", new SpawnEggItem(UEntities.BUTTERFLY, 0x222200, 0xaaeeff, new Settings().group(ItemGroup.MISC)));

    static <T extends Item> T register(String name, T item) {
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
        return Registry.ITEM.add(new Identifier("unicopia", name), item);
    }

    static MusicDiscItem register(String name, SoundEvent sound) {
        return register(name, new MusicDiscItem(1, sound, new Settings()
                .maxCount(1)
                .group(ItemGroup.MISC)
                .rarity(Rarity.RARE)
            ) {});
    }

    static void bootstrap() {
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(cuccoon), new ItemStack(chitin_shell), 0.3F);

        Toxics.bootstrap();
    }
}
