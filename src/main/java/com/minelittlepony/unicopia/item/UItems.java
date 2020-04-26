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
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

public interface UItems {

    AppleItem GREEN_APPLE = register(new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)), "green_apple");
    AppleItem SWEET_APPLE = register(new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)), "sweet_apple");
    AppleItem SOUR_APPLE = register(new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)), "sour_apple");

    ZapAppleItem ZAP_APPLE = register(new ZapAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(UFoodComponents.ZAP_APPLE)), "zap_apple");

    AppleItem ROTTEN_APPLE = register(new RottenAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)), "rotten_apple");
    AppleItem COOKED_ZAP_APPLE = register(new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)), "cooked_zap_apple");

    Item CLOUD_MATTER = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "cloud_matter");
    Item DEW_DROP = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "dew_drop");

    CloudPlacerItem RACING_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.RACING_CLOUD), "racing_cloud_spawner");
    CloudPlacerItem CONSTRUCTION_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.CONSTRUCTION_CLOUD), "construction_cloud_spawner");
    CloudPlacerItem WILD_CLOUD_SPAWNER = register(new CloudPlacerItem(UEntities.WILD_CLOUD), "wild_cloud_spawner");

    Item CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS), "cloud_block");
    Item ENCHANTED_CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS), "enchanted_cloud_block");
    Item DENSE_CLOUD_BLOCK = register(new PredicatedBlockItem(UBlocks.DENSE_CLOUD_BLOCK, new Settings().group(ItemGroup.MATERIALS), PLAYER_PEGASUS), "dense_cloud_block");

    Item CLOUD_STAIRS = register(new PredicatedBlockItem(UBlocks.CLOUD_STAIRS, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS), "cloud_stairs");
    Item CLOUD_FENCE = register(new PredicatedBlockItem(UBlocks.CLOUD_FENCE, new Settings().group(ItemGroup.DECORATIONS), PLAYER_PEGASUS), "cloud_fence");
    Item CLOUD_ANVIL = register(new PredicatedBlockItem(UBlocks.CLOUD_ANVIL, new Settings().group(ItemGroup.DECORATIONS), PLAYER_PEGASUS), "cloud_anvil");

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

    Item CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS), "cloud_slab");
    Item ENCHANTED_CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.ENCHANTED_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS), "enchanted_cloud_slab");
    Item DENSE_CLOUD_SLAB = register(new PredicatedBlockItem(UBlocks.DENSE_CLOUD_SLAB, new Settings().group(ItemGroup.BUILDING_BLOCKS), PLAYER_PEGASUS), "dense_cloud_slab");

    MagicGemItem GEM = register(new MagicGemItem(new Settings().maxCount(16).group(ItemGroup.BREWING)), "gem");
    MagicGemItem CORRUPTED_GEM = register(new CursedMagicGemItem(new Settings().maxCount(16).group(ItemGroup.BREWING)), "corrupted_gem");

    BagOfHoldingItem BAG_OF_HOLDING = register(new BagOfHoldingItem(new Settings().group(ItemGroup.TRANSPORTATION).rarity(Rarity.UNCOMMON).maxCount(1)), "bag_of_holding");
    AlicornAmuletItem ALICORN_AMULET = register(new AlicornAmuletItem(new Settings().group(ItemGroup.COMBAT).rarity(Rarity.RARE).maxCount(1)), "alicorn_amulet");

    SpellbookItem SPELLBOOK = register(new SpellbookItem(new Item.Settings().maxCount(1).group(ItemGroup.BREWING)), "spellbook");
    Item MEADOW_BROOK_STAFF = register(new StaffItem(new Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(2)), "meadow_brook_staff");
    Item REMEMBERANCE_STAFF = register(new EnchantedStaffItem(new Settings().group(ItemGroup.TOOLS).rarity(Rarity.UNCOMMON).maxCount(1), new ScorchSpell()), "remembrance_staff");

    Item SPEAR = register(new SpearItem(new Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(500)), "spear");

    MossItem MOSS = register(new MossItem(new Settings().group(ItemGroup.FOOD).food(UFoodComponents.RANDOM_FOLIAGE)), "moss");

    Item ALFALFA_SEEDS = register(new AliasedBlockItem(UBlocks.ALFALFA_CROPS, new Settings().group(ItemGroup.MATERIALS).food(UFoodComponents.ALFALFA_SEEDS)), "alfalfa_seeds");
    Item ALFALFA_LEAVES = register(new Item(new Settings().group(ItemGroup.FOOD).food(UFoodComponents.ALFALFA_LEAVES)), "alfalfa_leaves");

    Item ENCHANTED_TORCH = register(new BlockItem(UBlocks.ENCHANTED_TORCH, new Settings().group(ItemGroup.DECORATIONS)), "enchanted_torch");

    Item CEREAL = register(new SugaryItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.CEREAL)
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 1), "cereal");
    Item BOOP_O_ROOPS = register(new SugaryItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.SUGAR)
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 110), "boop_o_roops");

    TomatoSeedsItem TOMATO_SEEDS = register(new TomatoSeedsItem(new Settings().group(ItemGroup.MATERIALS)), "tomato_seeds");
    TomatoItem TOMATO = register(new TomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.TOMATO)), "tomato");
    RottenTomatoItem ROTTEN_TOMATO = register(new RottenTomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.BAD_TOMATO)), "rotten_tomato");

    TomatoItem CLOUDSDALE_TOMATO = register(new TomatoItem(new Settings()
            .rarity(Rarity.UNCOMMON)
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.GOOD_TOMATO)), "cloudsdale_tomato");
    RottenTomatoItem ROTTEN_CLOUDSDALE_TOMATO = register(new RottenTomatoItem(new Settings()
            .group(ItemGroup.FOOD)
            .rarity(Rarity.UNCOMMON)
            .food(UFoodComponents.BAD_TOMATO)), "rotten_cloudsdale_tomato");

    Item APPLE_SEEDS = register(new BlockItem(UBlocks.APPLE_SAPLING, new Settings().group(ItemGroup.DECORATIONS)), "apple_seeds");
    Item APPLE_LEAVES = register(new BlockItem(UBlocks.APPLE_LEAVES, new Settings().group(ItemGroup.DECORATIONS)), "apple_leaves");

    Item DAFFODIL_DAISY_SANDWICH = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.DAFODIL_DAISY_SANDWICH), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "daffodil_daisy_sandwich");
    Item HAY_BURGER = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.HAY_BURGER), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "hay_burger");
    Item HAY_FRIES = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.HAY_FRIES), UseAction.EAT, Toxicity.SAFE, Toxin.FOOD), "hay_fries");
    Item SALAD = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.SALAD)
            .recipeRemainder(Items.BOWL), UseAction.EAT, Toxicity::fromStack, Toxin.FOOD), "salad");

    Item WHEAT_WORMS = register(new ToxicItem(new Settings()
            .group(ItemGroup.MISC)
            .food(UFoodComponents.WORMS), UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD), "wheat_worms");
    Item MUG = register(new Item(new Settings().group(ItemGroup.MATERIALS)), "mug");
    Item CIDER = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .food(UFoodComponents.SALAD)
            .recipeRemainder(MUG), UseAction.DRINK, Toxicity.MILD, Toxin.FOOD), "apple_cider");
    Item JUICE = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .recipeRemainder(Items.GLASS_BOTTLE)
            .food(UFoodComponents.JUICE), UseAction.DRINK, Toxicity.SAFE, Toxin.FOOD), "juice");
    Item BURNED_JUICE = register(new ToxicItem(new Settings()
            .group(ItemGroup.FOOD)
            .recipeRemainder(Items.GLASS_BOTTLE)
            .food(UFoodComponents.BURNED_JUICE), UseAction.DRINK, Toxicity.FAIR, Toxin.FOOD), "burned_juice");

    Item CLOUD_SPAWN_EGG = register(new SpawnEggItem(UEntities.CLOUD, 0x4169e1, 0x7fff00, new Settings().group(ItemGroup.MISC)), "cloud_spawn_egg");
    Item BUTTERFLY_SPAWN_EGG = register(new SpawnEggItem(UEntities.BUTTERFLY, 0x222200, 0xaaeeff, new Settings().group(ItemGroup.MISC)), "butterfly_spawn_egg");

    static <T extends Item> T register(T item, String name) {
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
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

        Toxics.bootstrap();
    }
}
