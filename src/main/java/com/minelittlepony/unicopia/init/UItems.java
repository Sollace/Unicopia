package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.item.ItemAlicornAmulet;
import com.minelittlepony.unicopia.item.ItemApple;
import com.minelittlepony.unicopia.item.ItemAppleMultiType;
import com.minelittlepony.unicopia.item.ItemCereal;
import com.minelittlepony.unicopia.item.ItemCloud;
import com.minelittlepony.unicopia.item.ItemCurse;
import com.minelittlepony.unicopia.item.ItemFruitLeaves;
import com.minelittlepony.unicopia.item.ItemMoss;
import com.minelittlepony.unicopia.item.ItemOfHolding;
import com.minelittlepony.unicopia.item.ItemRottenApple;
import com.minelittlepony.unicopia.item.ItemSpell;
import com.minelittlepony.unicopia.item.ItemSpellbook;
import com.minelittlepony.unicopia.item.ItemStaff;
import com.minelittlepony.unicopia.item.ItemStick;
import com.minelittlepony.unicopia.item.ItemTomato;
import com.minelittlepony.unicopia.item.ItemTomatoSeeds;
import com.minelittlepony.unicopia.item.ItemZapApple;
import com.minelittlepony.unicopia.item.UItemBlock;
import com.minelittlepony.unicopia.item.UItemDecoration;
import com.minelittlepony.unicopia.item.UItemSlab;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import static com.minelittlepony.unicopia.Predicates.*;

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.edibles.BushToxicityDeterminent;
import com.minelittlepony.unicopia.edibles.CookedToxicityDeterminent;
import com.minelittlepony.unicopia.edibles.FlowerToxicityDeterminent;
import com.minelittlepony.unicopia.edibles.MultiItemEdible;
import com.minelittlepony.unicopia.edibles.Toxicity;
import com.minelittlepony.unicopia.edibles.UItemFoodDelegate;
import com.minelittlepony.unicopia.extern.Baubles;
import com.minelittlepony.unicopia.forgebullshit.BuildInTexturesBakery;
import com.minelittlepony.unicopia.forgebullshit.ItemModels;
import com.minelittlepony.unicopia.forgebullshit.OreReplacer;
import com.minelittlepony.unicopia.forgebullshit.RegistryLockSpinner;

public class UItems {
    public static final ItemApple red_apple = new ItemApple("minecraft", "apple");
    public static final ItemApple green_apple = new ItemApple(Unicopia.MODID, "apple_green");
    public static final ItemApple sweet_apple = new ItemApple(Unicopia.MODID, "apple_sweet");
    public static final ItemApple sour_apple = new ItemApple(Unicopia.MODID, "apple_sour");

    public static final ItemAppleMultiType zap_apple = new ItemZapApple(Unicopia.MODID, "zap_apple")
            .setSubTypes("zap_apple", "red", "green", "sweet", "sour", "zap");

    public static final ItemApple rotten_apple = new ItemRottenApple(Unicopia.MODID, "rotten_apple");
    public static final ItemApple cooked_zap_apple = new ItemApple(Unicopia.MODID, "cooked_zap_apple");

    public static final Item cloud_matter = new Item()
            .setCreativeTab(CreativeTabs.MATERIALS)
            .setTranslationKey("cloud_matter")
            .setRegistryName(Unicopia.MODID, "cloud_matter");

    public static final Item dew_drop = new Item()
            .setCreativeTab(CreativeTabs.MATERIALS)
            .setTranslationKey("dew_drop")
            .setRegistryName(Unicopia.MODID, "dew_drop");

    public static final ItemCloud cloud_spawner = new ItemCloud(Unicopia.MODID, "cloud");

    public static final Item cloud_block = new UItemBlock(UBlocks.normal_cloud, INTERACT_WITH_CLOUDS);
    public static final Item enchanted_cloud = new UItemBlock(UBlocks.enchanted_cloud, INTERACT_WITH_CLOUDS);
    public static final Item packed_cloud = new UItemBlock(UBlocks.packed_cloud, INTERACT_WITH_CLOUDS);

    public static final Item cloud_stairs = new UItemBlock(UBlocks.cloud_stairs, INTERACT_WITH_CLOUDS);

    public static final Item cloud_farmland = new UItemBlock(UBlocks.cloud_farmland, INTERACT_WITH_CLOUDS);

    public static final Item cloud_fence = new UItemBlock(UBlocks.cloud_fence, INTERACT_WITH_CLOUDS);
    public static final Item cloud_banister = new UItemBlock(UBlocks.cloud_banister, INTERACT_WITH_CLOUDS);

    public static final Item anvil = new UItemBlock(UBlocks.anvil, INTERACT_WITH_CLOUDS).setTranslationKey("cloud_anvil");

    public static final Item hive = new ItemBlock(UBlocks.hive).setRegistryName(Unicopia.MODID, "hive");

    public static final Item mist_door = new ItemDoor(UBlocks.mist_door)
            .setTranslationKey("mist_door")
            .setRegistryName(Unicopia.MODID, "mist_door");
    public static final Item library_door = new ItemDoor(UBlocks.library_door)
            .setTranslationKey("library_door")
            .setRegistryName(Unicopia.MODID, "library_door");
    public static final Item bakery_door = new ItemDoor(UBlocks.bakery_door)
            .setTranslationKey("bakery_door")
            .setRegistryName(Unicopia.MODID, "bakery_door");

    public static final Item sugar_block = new UItemDecoration(UBlocks.sugar_block);

    public static final Item cloud_slab = new UItemSlab(UBlocks.cloud_slab, UBlocks.cloud_slab.doubleSlab, INTERACT_WITH_CLOUDS);
    public static final Item enchanted_cloud_slab = new UItemSlab(UBlocks.enchanted_cloud_slab, UBlocks.enchanted_cloud_slab.doubleSlab, INTERACT_WITH_CLOUDS);
    public static final Item packed_cloud_slab = new UItemSlab(UBlocks.packed_cloud_slab, UBlocks.packed_cloud_slab.doubleSlab, INTERACT_WITH_CLOUDS);

    public static final ItemSpell spell = new ItemSpell(Unicopia.MODID, "gem");
    public static final ItemSpell curse = new ItemCurse(Unicopia.MODID, "corrupted_gem");

    public static final ItemOfHolding bag_of_holding = new ItemOfHolding(Unicopia.MODID, "bag_of_holding");
    public static final ItemAlicornAmulet alicorn_amulet = Baubles.alicornAmulet(Unicopia.MODID, "alicorn_amulet");

    public static final ItemSpellbook spellbook = new ItemSpellbook(Unicopia.MODID, "spellbook");
    public static final Item staff_meadow_brook = new ItemStaff(Unicopia.MODID, "staff_meadow_brook").setMaxDamage(2);

    public static final ItemMoss moss = new ItemMoss(Unicopia.MODID, "moss");

    public static final Item alfalfa_seeds = new ItemSeedFood(1, 4, UBlocks.alfalfa, Blocks.FARMLAND)
            .setTranslationKey("alfalfa_seeds")
            .setRegistryName(Unicopia.MODID, "alfalfa_seeds")
            .setCreativeTab(CreativeTabs.MATERIALS);

    public static final ItemStick stick = new ItemStick();
    public static final Item enchanted_torch = new ItemBlock(UBlocks.enchanted_torch)
            .setRegistryName(Unicopia.MODID, "enchanted_torch");

    public static final Item alfalfa_leaves = new ItemFood(1, 3, false)
            .setTranslationKey("alfalfa_leaves")
            .setRegistryName(Unicopia.MODID, "alfalfa_leaves");

    public static final Item cereal = new ItemCereal(Unicopia.MODID, "cereal", 9, 0.8F).setSugarAmount(1);
    public static final Item sugar_cereal = new ItemCereal(Unicopia.MODID, "sugar_cereal", 20, -2).setSugarAmount(110).setAlwaysEdible();

    public static final ItemTomato tomato = new ItemTomato(Unicopia.MODID, "tomato", 4, 34);
    public static final ItemTomato cloudsdale_tomato = new ItemTomato(Unicopia.MODID, "cloudsdale_tomato", 16, 4);
    public static final ItemTomatoSeeds tomato_seeds = new ItemTomatoSeeds(Unicopia.MODID, "tomato_seeds");

    public static final Item apple_seeds = new UItemDecoration(UBlocks.apple_tree, Unicopia.MODID, "apple_seeds");

    public static final Item apple_leaves = new ItemFruitLeaves(UBlocks.apple_leaves);

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

    public static final Item mug = new Item()
            .setTranslationKey("mug")
            .setRegistryName(Unicopia.MODID, "mug")
            .setCreativeTab(CreativeTabs.MATERIALS)
            .setFull3D();
    public static final Item apple_cider = new MultiItemEdible(Unicopia.MODID, "apple_cider", 4, 2, stack -> Toxicity.MILD)
            .setUseAction(EnumAction.DRINK)
            .setContainerItem(mug)
            .setFull3D();
    public static final Item juice = new MultiItemEdible(Unicopia.MODID, "juice", 2, 2, stack -> Toxicity.SAFE)
            .setUseAction(EnumAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);
    public static final Item burned_juice = new MultiItemEdible(Unicopia.MODID, "burned_juice", 3, 1, stack -> Toxicity.FAIR)
            .setUseAction(EnumAction.DRINK)
            .setContainerItem(Items.GLASS_BOTTLE);

    static void init(IForgeRegistry<Item> registry) {
        RegistryLockSpinner.open(Item.REGISTRY, Items.class, r -> r
                .replace(Items.APPLE, red_apple)
                .replace(Items.STICK, stick)
                .replace(Item.getItemFromBlock(Blocks.TALLGRASS), tall_grass)
                .replace(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), double_plant)
                .replace(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), yellow_flower)
                .replace(Item.getItemFromBlock(Blocks.RED_FLOWER), red_flower));

        registry.registerAll(
                green_apple, sweet_apple, sour_apple,
                cloud_spawner, dew_drop, cloud_matter, cloud_block, enchanted_cloud, packed_cloud,
                cloud_stairs,
                cloud_slab, enchanted_cloud_slab, packed_cloud_slab,
                cloud_fence, cloud_banister,
                cloud_farmland, mist_door, library_door, bakery_door, anvil,

                bag_of_holding, spell, curse, spellbook, mug, enchanted_torch,
                staff_meadow_brook, alicorn_amulet,

                alfalfa_seeds, alfalfa_leaves,
                cereal, sugar_cereal, sugar_block,
                rotten_apple, zap_apple, cooked_zap_apple,

                cloudsdale_tomato, tomato_seeds, tomato, moss,

                hive,

                apple_seeds, apple_leaves,

                daffodil_daisy_sandwich, hay_burger, hay_fries, salad, wheat_worms,
                apple_cider, juice, burned_juice);

        if (UClient.isClientSide()) {
            ItemModels.registerAll(
                    cloud_spawner,

                    green_apple, sweet_apple, sour_apple,

                    zap_apple,
                    rotten_apple, cooked_zap_apple, dew_drop,

                    tomato, cloudsdale_tomato, moss,

                    cloud_spawner, cloud_matter, cloud_block, enchanted_cloud, packed_cloud,
                    cloud_stairs,
                    cloud_slab, enchanted_cloud_slab, packed_cloud_slab,
                    cloud_fence, cloud_banister,
                    cloud_farmland, mist_door, library_door, bakery_door, anvil,

                    bag_of_holding, spell, curse, spellbook, mug, enchanted_torch,
                    staff_meadow_brook, alicorn_amulet,

                    alfalfa_seeds, alfalfa_leaves,
                    cereal, sugar_cereal, sugar_block,
                    tomato_seeds,

                    hive,

                    apple_seeds, apple_leaves,

                    daffodil_daisy_sandwich, hay_burger, hay_fries, salad, wheat_worms,

                    apple_cider, juice, burned_juice);

            BuildInTexturesBakery.getBuiltInTextures().add(new ResourceLocation(Unicopia.MODID, "items/empty_slot_gem"));
        }

        registerFuels();
    }

    static void registerFuels() {
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(zap_apple), new ItemStack(cooked_zap_apple), 0.1F);
        FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(juice), new ItemStack(burned_juice), 0);
    }

    public static void fixRecipes() {
        new OreReplacer()
            .registerAll(stack -> stack.getItem().getRegistryName().equals(red_apple.getRegistryName()))
            .done();
    }

    @SideOnly(Side.CLIENT)
    static void registerColors(ItemColors registry) {
        registry.registerItemColorHandler((stack, tint) -> {
            if (MAGI.test(Minecraft.getMinecraft().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }

            return 0xffffff;
        }, spell, curse);
    }
}
