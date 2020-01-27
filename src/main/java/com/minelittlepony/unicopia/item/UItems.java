package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.consumables.DynamicToxicItem;
import com.minelittlepony.unicopia.item.consumables.ToxicBlockItem;
import com.minelittlepony.unicopia.item.consumables.ToxicItem;
import com.minelittlepony.unicopia.item.consumables.Toxicity;
import com.minelittlepony.unicopia.item.consumables.Toxin;
import com.minelittlepony.unicopia.item.consumables.DynamicToxicBlockItem;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;
import com.minelittlepony.unicopia.magic.spells.SpellScorch;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Items;
import net.minecraft.item.TallBlockItem;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

import static com.minelittlepony.unicopia.EquinePredicates.*;

import com.minelittlepony.unicopia.ServerInteractionManager;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.USounds;

public class UItems {

    public static final AppleItem green_apple = register(new AppleItem(FoodComponents.APPLE), "apple_green");
    public static final AppleItem sweet_apple = register(new AppleItem(FoodComponents.APPLE), "apple_sweet");
    public static final AppleItem sour_apple = register(new AppleItem(FoodComponents.APPLE), "apple_sour");

    public static final ZapAppleItem zap_apple = register(new ZapAppleItem(), "zap_apple");

    public static final AppleItem rotten_apple = register(new RottenAppleItem(FoodComponents.APPLE), "rotten_apple");
    public static final AppleItem cooked_zap_apple = register(new AppleItem(FoodComponents.APPLE), "cooked_zap_apple");

    public static final Item cloud_matter = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "cloud_matter");
    public static final Item dew_drop = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "dew_drop");

    public static final CloudPlacerItem racing_cloud_spawner = register(new CloudPlacerItem(UEntities.RACING_CLOUD), "racing_cloud_spawner");
    public static final CloudPlacerItem construction_cloud_spawner = register(new CloudPlacerItem(UEntities.CONSTRUCTION_CLOUD), "construction_cloud_spawner");
    public static final CloudPlacerItem wild_cloud_spawner = register(new CloudPlacerItem(UEntities.WILD_CLOUD), "wild_cloud_spawner");

    public static final Item cloud_block = register(new PredicatedBlockItem(UBlocks.normal_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "cloud_block");
    public static final Item enchanted_cloud = register(new PredicatedBlockItem(UBlocks.enchanted_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "enchanted_cloud_block");
    public static final Item packed_cloud = register(new PredicatedBlockItem(UBlocks.packed_cloud, new Item.Settings().group(ItemGroup.MATERIALS), INTERACT_WITH_CLOUDS), "packed_cloud_block");

    public static final Item cloud_stairs = register(new PredicatedBlockItem(UBlocks.cloud_stairs, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS), "cloud_stairs");

    public static final Item cloud_fence = register(new PredicatedBlockItem(UBlocks.cloud_fence, new Item.Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_fence");
    public static final Item cloud_banister = register(new PredicatedBlockItem(UBlocks.cloud_banister, new Item.Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_banister");

    public static final Item anvil = register(new PredicatedBlockItem(UBlocks.anvil, new Item.Settings().group(ItemGroup.DECORATIONS), INTERACT_WITH_CLOUDS), "cloud_anvil");

    public static final Item record_crusade = register(new URecord(USounds.RECORD_CRUSADE), "crusade");
    public static final Item record_pet = register(new URecord(USounds.RECORD_PET), "pet");
    public static final Item record_popular = register(new URecord(USounds.RECORD_POPULAR), "popular");
    public static final Item record_funk = register(new URecord(USounds.RECORD_FUNK), "funk");

    public static final Item hive = register(new BlockItem(UBlocks.hive, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "hive");
    public static final Item chitin_shell = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "chitin_shell");
    public static final Item chitin = register(new BlockItem(UBlocks.chitin, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "chitin_block");
    public static final Item chissled_chitin = register(new BlockItem(UBlocks.chissled_chitin, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "chissled_chitin");
    public static final Item cuccoon = register(new BlockItem(UBlocks.cuccoon, new Item.Settings().group(ItemGroup.MATERIALS)), "cuccoon");
    public static final Item slime_layer = register(new BlockItem(UBlocks.slime_layer, new Item.Settings().group(ItemGroup.DECORATIONS)), "slime_layer");

    public static final Item mist_door = register(new TallBlockItem(UBlocks.mist_door, new Item.Settings().group(ItemGroup.REDSTONE)), "mist_door");
    public static final Item library_door = register(new TallBlockItem(UBlocks.library_door, new Item.Settings().group(ItemGroup.REDSTONE)), "library_door");
    public static final Item bakery_door = register(new TallBlockItem(UBlocks.bakery_door, new Item.Settings().group(ItemGroup.REDSTONE)), "bakery_door");
    public static final Item diamond_door = register(new TallBlockItem(UBlocks.diamond_door, new Item.Settings().group(ItemGroup.REDSTONE)), "diamond_door");

    public static final Item sugar_block = register(new BlockItem(UBlocks.sugar_block, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)), "sugar_block");

    public static final Item cloud_slab = new PredicatedBlockItem(UBlocks.cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);
    public static final Item enchanted_cloud_slab = new PredicatedBlockItem(UBlocks.enchanted_cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);
    public static final Item packed_cloud_slab = new PredicatedBlockItem(UBlocks.packed_cloud_slab, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS), INTERACT_WITH_CLOUDS);

    public static final MagicGemItem spell = register(new MagicGemItem(), "gem");
    public static final MagicGemItem curse = register(new CursedMagicGemItem(), "corrupted_gem");

    public static final BagOfHoldingItem bag_of_holding = register(new BagOfHoldingItem(), "bag_of_holding");
    public static final AlicornAmuletItem alicorn_amulet = register(new AlicornAmuletItem(), "alicorn_amulet");

    public static final SpellbookItem spellbook = register(new SpellbookItem(), "spellbook");
    public static final Item staff_meadow_brook = register(new StaffItem(new Item.Settings().maxCount(1).maxDamage(2)), "staff_meadow_brook");
    public static final Item staff_remembrance = register(new EnchantedStaffItem(new Item.Settings(), new SpellScorch()), "staff_remembrance");

    public static final Item spear = register(new SpearItem(new Item.Settings().maxCount(1).maxDamage(500)), "spear");

    public static final MossItem moss = register(new MossItem(new Item.Settings()), "moss");

    public static final Item alfalfa_seeds = register(new AliasedBlockItem(UBlocks.alfalfa, new Item.Settings()
            .group(ItemGroup.MATERIALS)
            .food(new FoodComponent.Builder()
                    .hunger(1)
                    .saturationModifier(4)
                    .build())), "alfalfa_seeds");

    public static final Item enchanted_torch = register(new BlockItem(UBlocks.enchanted_torch, new Item.Settings().group(ItemGroup.DECORATIONS)), "enchanted_torch");

    public static final Item alfalfa_leaves = register(new Item(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(1)
                    .saturationModifier(3)
                    .build())), "alfalfa_leaves");

    public static final Item cereal = register(new SugaryItem(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(9)
                    .saturationModifier(0.8F)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 1), "cereal");
    public static final Item sugar_cereal = register(new SugaryItem(new Item.Settings()
            .group(ItemGroup.FOOD)
            .food(new FoodComponent.Builder()
                    .hunger(20)
                    .saturationModifier(-2)
                    .build())
            .maxCount(1)
            .recipeRemainder(Items.BOWL), 110), "sugar_cereal");

    public static final TomatoItem tomato = register(new TomatoItem(4, 34), "tomato");
    public static final RottenTomatoItem rotten_tomato = register(new RottenTomatoItem(4, 34), "rotten_tomato");

    public static final TomatoItem cloudsdale_tomato = register(new TomatoItem(16, 4), "cloudsdale_tomato");
    public static final RottenTomatoItem rotten_cloudsdale_tomato = register(new RottenTomatoItem(5, 34), "rotten_cloudsdale_tomato");

    public static final TomatoSeedsItem tomato_seeds = register(new TomatoSeedsItem(), "tomato_seeds");

    public static final Item apple_seeds = register(new BlockItem(UBlocks.apple_tree, new Item.Settings().group(ItemGroup.DECORATIONS)), "apple_seeds");
    public static final Item apple_leaves = register(new BlockItem(UBlocks.apple_leaves, new Item.Settings().group(ItemGroup.DECORATIONS)), "apple_leaves");

    public static final Item daffodil_daisy_sandwich = register(new DynamicToxicItem(new Item.Settings(), 3, 2, UseAction.EAT, Toxicity::fromStack), "daffodil_daisy_sandwich");
    public static final Item hay_burger = register(new DynamicToxicItem(new Item.Settings(), 3, 4, UseAction.EAT, Toxicity::fromStack), "hay_burger");
    public static final Item hay_fries = register(new ToxicItem(new Item.Settings(), 1, 5, UseAction.EAT, Toxicity.SAFE), "hay_fries");
    public static final Item salad = register(new DynamicToxicItem(new Item.Settings().recipeRemainder(Items.BOWL), 4, 2, UseAction.EAT, Toxicity::fromStack), "salad");

    public static final Item wheat_worms = register(new ToxicItem(new Item.Settings(), 1, 0, UseAction.EAT, Toxicity.SEVERE), "wheat_worms");
    public static final Item mug = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "mug");
    public static final Item apple_cider = register(new ToxicItem(new Item.Settings().recipeRemainder(mug), 4, 2, UseAction.DRINK, Toxicity.MILD), "apple_cider");
    public static final Item juice = register(new ToxicItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE), 2, 2, UseAction.DRINK, Toxicity.SAFE), "juice");
    public static final Item burned_juice = register(new ToxicItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE), 3, 1, UseAction.DRINK, Toxicity.FAIR), "burned_juice");

    private static <T extends Item> T register(T newItem, Item oldItem) {
        return Registry.ITEM.add(Registry.ITEM.getId(oldItem), newItem);
    }

    private static <T extends Item> T register(T item, String name) {
        return register(item, name);
    }

    public static void bootstrap() {

        if (ServerInteractionManager.isClientSide()) {
            //BuildInTexturesBakery.getBuiltInTextures().add(new Identifier(Unicopia.MODID, "items/empty_slot_gem"));

            ItemColors registry = null;
            registry.register((stack, tint) -> {
                if (MAGI.test(MinecraftClient.getInstance().player)) {
                    return SpellRegistry.instance().getSpellTintFromStack(stack);
                }

                return 0xffffff;
            }, spell, curse);
        }

        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
        // FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(cuccoon), new ItemStack(chitin_shell), 0.3F);
    }

    static class VanillaOverrides {
        public static final StickItem stick = register(new StickItem(), Items.STICK);
        public static final ExtendedShearsItem shears = register(new ExtendedShearsItem(), Items.SHEARS);

        public static final AppleItem red_apple = register(new AppleItem(FoodComponents.APPLE), Items.APPLE);

        public static final Item grass = register(new DynamicToxicBlockItem(Blocks.GRASS, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.NAUSEA), Items.GRASS);
        public static final Item fern = register(new DynamicToxicBlockItem(Blocks.FERN, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.STRENGTH), Items.FERN);
        public static final Item dead_bush = register(new DynamicToxicBlockItem(Blocks.DEAD_BUSH, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.NAUSEA), Items.DEAD_BUSH);

        public static final Item dandelion = register(new ToxicBlockItem(Blocks.DANDELION, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.DANDELION);
        public static final Item poppy = register(new ToxicBlockItem(Blocks.POPPY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE), Items.POPPY);
        public static final Item blue_orchid = register(new ToxicBlockItem(Blocks.BLUE_ORCHID, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.BLUE_ORCHID);
        public static final Item allium = register(new ToxicBlockItem(Blocks.ALLIUM, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.FAIR), Items.ALLIUM);
        public static final Item azure_bluet = register(new DynamicToxicBlockItem(Blocks.AZURE_BLUET, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.RADIOACTIVITY), Items.AZURE_BLUET);
        public static final Item red_tulip = register(new ToxicBlockItem(Blocks.RED_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.RED_TULIP);
        public static final Item orange_tulip = register(new ToxicBlockItem(Blocks.ORANGE_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.ORANGE_TULIP);
        public static final Item white_tulip = register(new ToxicBlockItem(Blocks.WHITE_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.FAIR), Items.WHITE_TULIP);
        public static final Item pink_tulip = register(new ToxicBlockItem(Blocks.PINK_TULIP, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.PINK_TULIP);
        public static final Item oxeye_daisy = register(new DynamicToxicBlockItem(Blocks.OXEYE_DAISY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.BLINDNESS), Items.OXEYE_DAISY);
        public static final Item cornflower = register(new ToxicBlockItem(Blocks.CORNFLOWER, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.CORNFLOWER);

        public static final Item rose_bush = register(new DynamicToxicBlockItem(Blocks.ROSE_BUSH, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE, Toxin.DAMAGE), Items.ROSE_BUSH);
        public static final Item peony = register(new ToxicBlockItem(Blocks.PEONY, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.PEONY);
        public static final Item tall_grass = register(new ToxicBlockItem(Blocks.TALL_GRASS, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SAFE), Items.TALL_GRASS);
        public static final Item large_fern = register(new DynamicToxicBlockItem(Blocks.LARGE_FERN, new Item.Settings().group(ItemGroup.DECORATIONS), 2, 1, UseAction.EAT, Toxicity.SEVERE, Toxin.DAMAGE), Items.LARGE_FERN);
    }
}
