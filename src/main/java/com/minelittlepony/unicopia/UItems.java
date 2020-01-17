package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.ItemAlicornAmulet;
import com.minelittlepony.unicopia.item.ItemAppleMultiType;
import com.minelittlepony.unicopia.item.ItemCereal;
import com.minelittlepony.unicopia.item.AppleItem;
import com.minelittlepony.unicopia.item.CloudPlacerItem;
import com.minelittlepony.unicopia.item.ExtendedShearsItem;
import com.minelittlepony.unicopia.item.CursedMagicGemItem;
import com.minelittlepony.unicopia.item.ItemMagicStaff;
import com.minelittlepony.unicopia.item.MossItem;
import com.minelittlepony.unicopia.item.ItemOfHolding;
import com.minelittlepony.unicopia.item.RottenAppleItem;
import com.minelittlepony.unicopia.item.RottenTomatoItem;
import com.minelittlepony.unicopia.item.ItemSpear;
import com.minelittlepony.unicopia.item.MagicGemItem;
import com.minelittlepony.unicopia.item.SpellbookItem;
import com.minelittlepony.unicopia.item.ItemStaff;
import com.minelittlepony.unicopia.item.TomatoItem;
import com.minelittlepony.unicopia.item.TomatoSeedsItem;
import com.minelittlepony.unicopia.item.ItemZapApple;
import com.minelittlepony.unicopia.item.PredicatedBlockItem;
import com.minelittlepony.unicopia.item.StickItem;
import com.minelittlepony.unicopia.item.URecord;
import com.minelittlepony.unicopia.item.consumables.BushToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.CookedToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.FlowerToxicityDeterminent;
import com.minelittlepony.unicopia.item.consumables.MultiItemEdible;
import com.minelittlepony.unicopia.item.consumables.Toxicity;
import com.minelittlepony.unicopia.item.consumables.UItemFoodDelegate;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.BlockItem;
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

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.ConstructionCloudEntity;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
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

    public static final ItemAppleMultiType zap_apple = new ItemZapApple(Unicopia.MODID, "zap_apple").setSubTypes("zap_apple", "red", "green", "sweet", "sour", "zap");

    public static final AppleItem rotten_apple = register(new RottenAppleItem(FoodComponents.APPLE), "rotten_apple");
    public static final AppleItem cooked_zap_apple = register(new AppleItem(FoodComponents.APPLE), "cooked_zap_apple");

    public static final Item cloud_matter = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "cloud_matter");
    public static final Item dew_drop = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "dew_drop");

    public static final CloudPlacerItem racing_cloud_spawner = register(new CloudPlacerItem(EntityRacingCloud::new), "racing_cloud_spawner");
    public static final CloudPlacerItem construction_cloud_spawner = register(new CloudPlacerItem(ConstructionCloudEntity::new), "construction_cloud_spawner");
    public static final CloudPlacerItem wild_cloud_spawner = register(new CloudPlacerItem(WildCloudEntity::new), "wild_cloud_spawner");

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

    public static final ItemOfHolding bag_of_holding = new ItemOfHolding(Unicopia.MODID, "bag_of_holding");
    public static final ItemAlicornAmulet alicorn_amulet = new ItemAlicornAmulet(Unicopia.MODID, "alicorn_amulet");

    public static final SpellbookItem spellbook = register(new SpellbookItem(), "spellbook");
    public static final Item staff_meadow_brook = new ItemStaff(Unicopia.MODID, "staff_meadow_brook").setMaxDamage(2);
    public static final Item staff_remembrance = new ItemMagicStaff(Unicopia.MODID, "staff_remembrance", new SpellScorch());

    public static final Item spear = new ItemSpear(Unicopia.MODID, "spear");

    public static final MossItem moss = new MossItem(Unicopia.MODID, "moss");

    public static final Item alfalfa_seeds = new ItemSeedFood(1, 4, UBlocks.alfalfa, Blocks.FARMLAND)
            .setTranslationKey("alfalfa_seeds")
            .setRegistryName(Unicopia.MODID, "alfalfa_seeds")
            .setCreativeTab(CreativeTabs.MATERIALS);

    public static final Item enchanted_torch = register(new BlockItem(UBlocks.enchanted_torch), "enchanted_torch");

    public static final Item alfalfa_leaves = new FoodItem(1, 3, false)
            .setTranslationKey("alfalfa_leaves")
            .setRegistryName(Unicopia.MODID, "alfalfa_leaves");

    public static final Item cereal = new ItemCereal(Unicopia.MODID, "cereal", 9, 0.8F).setSugarAmount(1);
    public static final Item sugar_cereal = new ItemCereal(Unicopia.MODID, "sugar_cereal", 20, -2).setSugarAmount(110).setAlwaysEdible();

    public static final TomatoItem tomato = new TomatoItem(Unicopia.MODID, "tomato", 4, 34);
    public static final RottenTomatoItem rotten_tomato = new RottenTomatoItem(Unicopia.MODID, "rotten_tomato", 4, 34);

    public static final TomatoItem cloudsdale_tomato = new TomatoItem(Unicopia.MODID, "cloudsdale_tomato", 16, 4);
    public static final RottenTomatoItem rotten_cloudsdale_tomato = new RottenTomatoItem(Unicopia.MODID, "rotten_cloudsdale_tomato", 4, 34);

    public static final TomatoSeedsItem tomato_seeds = new TomatoSeedsItem(Unicopia.MODID, "tomato_seeds");

    public static final Item apple_seeds = new UItemDecoration(UBlocks.apple_tree, Unicopia.MODID, "apple_seeds");

    public static final Item apple_leaves = new BlockItem(UBlocks.apple_leaves);

    public static final Item double_plant = new UItemFoodDelegate(Blocks.DOUBLE_PLANT, stack ->
                BlockDoublePlant.EnumPlantType.byMetadata(stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new MultiItemEdible(new BushToxicityDeterminent()))
            .setTranslationKey("doublePlant");

    public static final Item tall_grass = new UItemFoodDelegate(Blocks.TALLGRASS, stack -> {
        switch (stack.getMetadata()) {
            case 0: return "shrub";
            case 1: return "grass";
            case 2: return "fern";
            default: return "";
        }
    }).setFoodDelegate(new MultiItemEdible(stack -> {
        switch (stack.getMetadata()) {
            default:
            case 0: return Toxicity.SAFE;
            case 1: return Toxicity.SAFE;
            case 2: return Toxicity.SEVERE;
        }
    }));

    public static final Item yellow_flower = new UItemFoodDelegate(Blocks.YELLOW_FLOWER, stack ->
                BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.YELLOW, stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new MultiItemEdible(new FlowerToxicityDeterminent(BlockFlower.EnumFlowerColor.YELLOW)))
            .setTranslationKey("flower");

    public static final Item red_flower = new UItemFoodDelegate(Blocks.RED_FLOWER, stack ->
                BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, stack.getMetadata()).getTranslationKey()
            ).setFoodDelegate(new MultiItemEdible(new FlowerToxicityDeterminent(BlockFlower.EnumFlowerColor.RED)))
            .setTranslationKey("rose");

    public static final Item daffodil_daisy_sandwich = new MultiItemEdible(Unicopia.MODID, "daffodil_daisy_sandwich", 3, 2, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true);
    public static final Item hay_burger = new MultiItemEdible(Unicopia.MODID, "hay_burger", 3, 4, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true);
    public static final Item hay_fries = new MultiItemEdible(Unicopia.MODID, "hay_fries", 1, 5, stack -> Toxicity.SAFE);
    public static final Item salad = new MultiItemEdible(Unicopia.MODID, "salad", 4, 2, CookedToxicityDeterminent.instance)
            .setHasSubtypes(true)
            .setContainerItem(Items.BOWL);

    public static final Item wheat_worms = new MultiItemEdible(Unicopia.MODID, "wheat_worms", 1, 0, stack -> Toxicity.SEVERE);

    public static final Item mug = register(new Item(new Item.Settings().group(ItemGroup.MATERIALS)), "mug");
    public static final Item apple_cider = new MultiItemEdible(Unicopia.MODID, "apple_cider", 4, 2, stack -> Toxicity.MILD)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(mug)
            .setFull3D();
    public static final Item juice = new MultiItemEdible(Unicopia.MODID, "juice", 2, 2, stack -> Toxicity.SAFE)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);
    public static final Item burned_juice = new MultiItemEdible(Unicopia.MODID, "burned_juice", 3, 1, stack -> Toxicity.FAIR)
            .setUseAction(UseAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);


    static void init(IForgeRegistry<Item> registry) {
        RegistryLockSpinner.open(Item.REGISTRY, Items.class, r -> r
                .replace(Items.APPLE, red_apple)
                .replace(Item.getItemFromBlock(Blocks.TALLGRASS), tall_grass)
                .replace(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), double_plant)
                .replace(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), yellow_flower)
                .replace(Item.getItemFromBlock(Blocks.RED_FLOWER), red_flower));

        if (UClient.isClientSide()) {
            BuildInTexturesBakery.getBuiltInTextures().add(new Identifier(Unicopia.MODID, "items/empty_slot_gem"));
        }

        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(cuccoon), new ItemStack(chitin_shell), 0.3F);
    }

    static void fixRecipes() {
        new OreReplacer()
            .registerAll(stack -> stack.getItem().getRegistryName().equals(red_apple.getRegistryName()))
            .done();
    }

    private static <T extends Item> T register(T item, String namespace, String name) {
        return Registry.ITEM.add(new Identifier(namespace, name), item);
    }
    private static <T extends Item> T register(T item, String name) {
        return register(item, name);
    }

    static void registerColors(ItemColors registry) {
        registry.register((stack, tint) -> {
            if (MAGI.test(MinecraftClient.getInstance().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }

            return 0xffffff;
        }, spell, curse);
    }

    static void bootstrap() {}
}
