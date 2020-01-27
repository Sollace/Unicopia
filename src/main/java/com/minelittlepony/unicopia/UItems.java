package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.AlicornAmuletItem;
import com.minelittlepony.unicopia.item.SugaryItem;
import com.minelittlepony.unicopia.item.AppleItem;
import com.minelittlepony.unicopia.item.CloudPlacerItem;
import com.minelittlepony.unicopia.item.ExtendedShearsItem;
import com.minelittlepony.unicopia.item.CursedMagicGemItem;
import com.minelittlepony.unicopia.item.EnchantedStaffItem;
import com.minelittlepony.unicopia.item.MossItem;
import com.minelittlepony.unicopia.item.BagOfHoldingItem;
import com.minelittlepony.unicopia.item.RottenAppleItem;
import com.minelittlepony.unicopia.item.RottenTomatoItem;
import com.minelittlepony.unicopia.item.SpearItem;
import com.minelittlepony.unicopia.item.MagicGemItem;
import com.minelittlepony.unicopia.item.SpellbookItem;
import com.minelittlepony.unicopia.item.StaffItem;
import com.minelittlepony.unicopia.item.TomatoItem;
import com.minelittlepony.unicopia.item.TomatoSeedsItem;
import com.minelittlepony.unicopia.item.ZapAppleItem;
import com.minelittlepony.unicopia.item.PredicatedBlockItem;
import com.minelittlepony.unicopia.item.StickItem;
import com.minelittlepony.unicopia.item.URecord;
import com.minelittlepony.unicopia.item.consumables.BushToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.CookedToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.FlowerToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.DelegatedEdibleItem;
import com.minelittlepony.unicopia.item.consumables.Toxicity;
import com.minelittlepony.unicopia.item.consumables.DelegateFoodItem;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;
import com.minelittlepony.unicopia.magic.spells.SpellScorch;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TallBlockItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

import static com.minelittlepony.unicopia.EquinePredicates.*;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ServerInteractionManager;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.ConstructionCloudEntity;
import com.minelittlepony.unicopia.entity.RacingCloudEntity;
import com.minelittlepony.unicopia.entity.WildCloudEntity;
import com.minelittlepony.unicopia.forgebullshit.BuildInTexturesBakery;
import com.minelittlepony.unicopia.forgebullshit.OreReplacer;
import com.minelittlepony.unicopia.forgebullshit.UnFuckedItemSnow;

public class UItems {

    private static final StickItem stick = register(new StickItem(), "minecraft", "stick");
    private static final ExtendedShearsItem shears = register(new ExtendedShearsItem(), "minecraft", "shears");

    public static final AppleItem red_apple = register(new AppleItem(FoodComponents.APPLE), "minecraft", "apple");
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

    public static final Item alfalfa_seeds = new ItemSeedFood(1, 4, UBlocks.alfalfa, Blocks.FARMLAND)
            .setTranslationKey("alfalfa_seeds")
            .setRegistryName(Unicopia.MODID, "alfalfa_seeds")
            .setCreativeTab(CreativeTabs.MATERIALS);

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

    public static final TomatoSeedsItem tomato_seeds = new TomatoSeedsItem(Unicopia.MODID, "tomato_seeds");

    public static final Item apple_seeds = new UItemDecoration(UBlocks.apple_tree, Unicopia.MODID, "apple_seeds");

    public static final Item apple_leaves = new BlockItem(UBlocks.apple_leaves);

    public static final Item double_plant = new DelegateFoodItem(Blocks.TALL_GRASS, stack ->
                BlockDoublePlant.EnumPlantType.byMetadata(stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new DelegatedEdibleItem(new BushToxicityDeterminent()))
            .setTranslationKey("doublePlant");

    public static final Item tall_grass = new DelegateFoodItem(Blocks.GRASS, stack -> {
        switch (stack.getMetadata()) {
            case 0: return "shrub";
            case 1: return "grass";
            case 2: return "fern";
            default: return "";
        }
    }).setFoodDelegate(new DelegatedEdibleItem(stack -> {
        switch (stack.getMetadata()) {
            default:
            case 0: return Toxicity.SAFE;
            case 1: return Toxicity.SAFE;
            case 2: return Toxicity.SEVERE;
        }
    }));

    public static final Item yellow_flower = new DelegateFoodItem(Blocks.YELLOW_FLOWER, stack ->
                BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.YELLOW, stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new DelegatedEdibleItem(new FlowerToxicityDeterminent(BlockFlower.EnumFlowerColor.YELLOW)))
            .setTranslationKey("flower");

    public static final Item red_flower = new DelegateFoodItem(Blocks.RED_FLOWER, stack ->
                BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new DelegatedEdibleItem(new FlowerToxicityDeterminent(BlockFlower.EnumFlowerColor.RED)))
            .setTranslationKey("rose");

    public static final Item daffodil_daisy_sandwich = new DelegatedEdibleItem(Unicopia.MODID, "daffodil_daisy_sandwich", 3, 2, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true);
    public static final Item hay_burger = new DelegatedEdibleItem(Unicopia.MODID, "hay_burger", 3, 4, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true);
    public static final Item hay_fries = new DelegatedEdibleItem(Unicopia.MODID, "hay_fries", 1, 5, stack -> Toxicity.SAFE);
    public static final Item salad = new DelegatedEdibleItem(Unicopia.MODID, "salad", 4, 2, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true)
            .setContainerItem(Items.BOWL);

    public static final Item wheat_worms = new DelegatedEdibleItem(Unicopia.MODID, "wheat_worms", 1, 0, stack -> Toxicity.SEVERE);

    public static final Item mug = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "mug");
    public static final Item apple_cider = new DelegatedEdibleItem(Unicopia.MODID, "apple_cider", 4, 2, stack -> Toxicity.MILD)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(mug)
            .setFull3D();
    public static final Item juice = new DelegatedEdibleItem(Unicopia.MODID, "juice", 2, 2, stack -> Toxicity.SAFE)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);
    public static final Item burned_juice = new DelegatedEdibleItem(Unicopia.MODID, "burned_juice", 3, 1, stack -> Toxicity.FAIR)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);

    private static <T extends Item> T register(T item, String namespace, String name) {
        return Registry.ITEM.add(new Identifier(namespace, name), item);
    }
    private static <T extends Item> T register(T item, String name) {
        return register(item, name);
    }

    static void bootstrap() {
        RegistryLockSpinner.open(Item.REGISTRY, Items.class, r -> r
                .replace(Items.APPLE, red_apple)
                .replace(Item.getItemFromBlock(Blocks.TALLGRASS), tall_grass)
                .replace(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), double_plant)
                .replace(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), yellow_flower)
                .replace(Item.getItemFromBlock(Blocks.RED_FLOWER), red_flower));

        if (ServerInteractionManager.isClientSide()) {
            BuildInTexturesBakery.getBuiltInTextures().add(new Identifier(Unicopia.MODID, "items/empty_slot_gem"));

            ItemColors registry;
            registry.register((stack, tint) -> {
                if (MAGI.test(MinecraftClient.getInstance().player)) {
                    return SpellRegistry.instance().getSpellTintFromStack(stack);
                }

                return 0xffffff;
            }, spell, curse);
        }

        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(cuccoon), new ItemStack(chitin_shell), 0.3F);

        new OreReplacer()
            .registerAll(stack -> stack.getItem().getRegistryName().equals(red_apple.getRegistryName()))
            .done();
    }
}
