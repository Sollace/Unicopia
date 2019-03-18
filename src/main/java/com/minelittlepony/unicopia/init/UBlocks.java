package com.minelittlepony.unicopia.init;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.BlockAlfalfa;
import com.minelittlepony.unicopia.block.BlockChiselledChitin;
import com.minelittlepony.unicopia.block.BlockChitin;
import com.minelittlepony.unicopia.block.BlockFruitLeaves;
import com.minelittlepony.unicopia.block.BlockGlowingGem;
import com.minelittlepony.unicopia.block.BlockGrowingCuccoon;
import com.minelittlepony.unicopia.block.BlockHiveWall;
import com.minelittlepony.unicopia.block.BlockSlimeLayer;
import com.minelittlepony.unicopia.block.BlockStick;
import com.minelittlepony.unicopia.block.BlockCloudAnvil;
import com.minelittlepony.unicopia.block.BlockCloudBanister;
import com.minelittlepony.unicopia.block.BlockCloudSlab;
import com.minelittlepony.unicopia.block.BlockCloudStairs;
import com.minelittlepony.unicopia.block.BlockDutchDoor;
import com.minelittlepony.unicopia.block.BlockSugar;
import com.minelittlepony.unicopia.block.BlockTomatoPlant;
import com.minelittlepony.unicopia.block.IColourful;
import com.minelittlepony.unicopia.block.UPot;
import com.minelittlepony.unicopia.block.USapling;
import com.minelittlepony.unicopia.item.ItemApple;
import com.minelittlepony.unicopia.block.BlockCloudDoor;
import com.minelittlepony.unicopia.block.BlockDiamondDoor;
import com.minelittlepony.unicopia.block.BlockCloudFarm;
import com.minelittlepony.unicopia.block.BlockCloudFence;
import com.minelittlepony.unicopia.block.BlockCloud;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class UBlocks {
    public static final BlockCloud normal_cloud = new BlockCloud(UMaterials.cloud, CloudType.NORMAL, Unicopia.MODID, "cloud_block");
    public static final BlockCloud enchanted_cloud = new BlockCloud(UMaterials.cloud, CloudType.ENCHANTED, Unicopia.MODID, "enchanted_cloud_block");
    public static final BlockCloud packed_cloud = new BlockCloud(UMaterials.cloud, CloudType.PACKED, Unicopia.MODID, "packed_cloud_block");

    public static final BlockCloudStairs cloud_stairs = new BlockCloudStairs(normal_cloud.getDefaultState(), Unicopia.MODID, "cloud_stairs");

    public static final BlockCloudSlab.Single<?> cloud_slab = new BlockCloudSlab.Single<>(normal_cloud, UMaterials.cloud, Unicopia.MODID, "cloud_slab");
    public static final BlockCloudSlab.Single<?> enchanted_cloud_slab = new BlockCloudSlab.Single<>(enchanted_cloud, UMaterials.cloud, Unicopia.MODID, "enchanted_cloud_slab");
    public static final BlockCloudSlab.Single<?> packed_cloud_slab = new BlockCloudSlab.Single<>(enchanted_cloud, UMaterials.cloud, Unicopia.MODID, "packed_cloud_slab");

    public static final BlockCloudDoor mist_door = new BlockCloudDoor(UMaterials.cloud, Unicopia.MODID, "mist_door", () -> UItems.mist_door);
    public static final Block library_door = new BlockDutchDoor(Material.WOOD, Unicopia.MODID, "library_door", () -> UItems.library_door)
            .setSoundType(SoundType.WOOD)
            .setHardness(3);
    public static final Block bakery_door = new BlockDutchDoor(Material.WOOD, Unicopia.MODID, "bakery_door", () -> UItems.bakery_door)
            .setSoundType(SoundType.WOOD)
            .setHardness(3);
    public static final Block diamond_door = new BlockDiamondDoor(Unicopia.MODID, "diamond_door", () -> UItems.diamond_door);

    public static final BlockGlowingGem enchanted_torch = new BlockGlowingGem(Unicopia.MODID, "enchanted_torch");

    public static final BlockCloudAnvil anvil = new BlockCloudAnvil(Unicopia.MODID, "anvil");

    public static final BlockCloudFence cloud_fence = new BlockCloudFence(UMaterials.cloud, CloudType.NORMAL, Unicopia.MODID, "cloud_fence");
    public static final BlockCloudBanister cloud_banister = new BlockCloudBanister(UMaterials.cloud, Unicopia.MODID, "cloud_banister");

    public static final BlockAlfalfa alfalfa = new BlockAlfalfa(Unicopia.MODID, "alfalfa");

    public static final BlockStick stick = new BlockStick(Unicopia.MODID, "stick");
    public static final BlockTomatoPlant tomato_plant = new BlockTomatoPlant(Unicopia.MODID, "tomato_plant");

    public static final BlockCloudFarm cloud_farmland = new BlockCloudFarm(Unicopia.MODID, "cloud_farmland");

    public static final BlockHiveWall hive = new BlockHiveWall(Unicopia.MODID, "hive");
    public static final BlockChitin chitin = new BlockChitin(Unicopia.MODID, "chitin_block");
    public static final Block chissled_chitin = new BlockChiselledChitin(Unicopia.MODID, "chissled_chitin");

    public static final BlockGrowingCuccoon cuccoon = new BlockGrowingCuccoon(Unicopia.MODID, "cuccoon");
    public static final BlockSlimeLayer slime_layer = new BlockSlimeLayer(Unicopia.MODID, "slime_layer");

    public static final Block sugar_block = new BlockSugar(Unicopia.MODID, "sugar_block");
    public static final UPot flower_pot = new UPot(Unicopia.MODID, "flower_pot");

    public static final USapling apple_tree = new USapling(Unicopia.MODID, "apple_sapling")
            .setTreeGen((w, s, m) -> new WorldGenTrees(true, 5, Blocks.LOG.getDefaultState(), UBlocks.apple_leaves.getDefaultState(), false));
    public static final Block apple_leaves = new BlockFruitLeaves(Unicopia.MODID, "apple_leaves", apple_tree)
            .setBaseGrowthChance(1200)
            .setTint(0xFFEE81)
            .setHarvestFruit(ItemApple::getRandomItemStack)
            .setUnharvestFruit(w -> new ItemStack(UItems.rotten_apple));

    static void init(IForgeRegistry<Block> registry) {
        registry.registerAll(normal_cloud, enchanted_cloud, packed_cloud,
                             cloud_stairs,
                             cloud_slab, cloud_slab.doubleSlab,
                             enchanted_cloud_slab, enchanted_cloud_slab.doubleSlab,
                             packed_cloud_slab, packed_cloud_slab.doubleSlab,
                             cloud_fence, cloud_banister,
                             mist_door, library_door, bakery_door, diamond_door,
                             hive, chitin, chissled_chitin, cuccoon, slime_layer,
                             anvil, cloud_farmland,
                             sugar_block, flower_pot,
                             alfalfa,
                             stick, tomato_plant,
                             enchanted_torch,
                             apple_tree, apple_leaves);
    }

    @SideOnly(Side.CLIENT)
    static void registerColors(ItemColors items, BlockColors blocks) {
        items.registerItemColorHandler((stack, tint) -> {
            @SuppressWarnings("deprecation")
            IBlockState state = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());

            return blocks.colorMultiplier(state, null, null, tint);
        }, apple_leaves);
        blocks.registerBlockColorHandler((state, world, pos, tint) -> {
            Block block = state.getBlock();

            if (block instanceof IColourful) {
                return ((IColourful)block).getCustomTint(state, tint);
            }

            if (world != null && pos != null) {
                return BiomeColorHelper.getFoliageColorAtPos(world, pos);
            }

            return ColorizerFoliage.getFoliageColorBasic();
        }, apple_leaves);
    }

    public static class Shills {
        @Nullable
        public static Block getShill(Block blockIn) {
            if (blockIn == Blocks.FLOWER_POT) {
                return flower_pot;
            }

            return null;
        }
    }
}
