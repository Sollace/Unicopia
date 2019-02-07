package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.block.BlockAlfalfa;
import com.minelittlepony.unicopia.block.BlockFruitLeaves;
import com.minelittlepony.unicopia.block.BlockCloud;
import com.minelittlepony.unicopia.block.BlockCloudAnvil;
import com.minelittlepony.unicopia.block.BlockCloudBanister;
import com.minelittlepony.unicopia.block.BlockCloudSlab;
import com.minelittlepony.unicopia.block.BlockCloudStairs;
import com.minelittlepony.unicopia.block.BlockSugar;
import com.minelittlepony.unicopia.block.BlockTomatoPlant;
import com.minelittlepony.unicopia.block.IColourful;
import com.minelittlepony.unicopia.block.USapling;
import com.minelittlepony.unicopia.block.BlockCloudDoor;
import com.minelittlepony.unicopia.block.BlockCloudFarm;
import com.minelittlepony.unicopia.block.BlockCloudFence;

import net.minecraft.block.Block;
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
    public static final BlockCloud cloud = new BlockCloud(UMaterials.cloud, Unicopia.MODID, "cloud_block");

    public static final BlockCloudStairs cloud_stairs = new BlockCloudStairs(UBlocks.cloud.getDefaultState(), Unicopia.MODID, "cloud_stairs");

    public static final BlockCloudSlab cloud_double_slab = new BlockCloudSlab(true, UMaterials.cloud, Unicopia.MODID, "cloud_double_slab");
    public static final BlockCloudSlab cloud_slab = new BlockCloudSlab(false, UMaterials.cloud, Unicopia.MODID, "cloud_slab");

    public static final BlockCloudDoor mist_door = new BlockCloudDoor(UMaterials.cloud, Unicopia.MODID, "mist_door");

    public static final BlockCloudAnvil anvil = new BlockCloudAnvil(Unicopia.MODID, "anvil");

    public static final BlockCloudFence cloud_fence = new BlockCloudFence(Unicopia.MODID, "cloud_fence");
    public static final BlockCloudBanister cloud_banister = new BlockCloudBanister(Unicopia.MODID, "cloud_banister");

    public static final BlockAlfalfa alfalfa = new BlockAlfalfa(Unicopia.MODID, "alfalfa");

    public static final BlockTomatoPlant tomato_plant = new BlockTomatoPlant(Unicopia.MODID, "tomato_plant");

    public static final BlockCloudFarm cloud_farmland = new BlockCloudFarm(Unicopia.MODID, "cloud_farmland");

    public static final Block sugar_block = new BlockSugar(Unicopia.MODID, "sugar_block");

    public static final USapling apple_tree = new USapling(Unicopia.MODID, "apple_sapling")
            .setTreeGen((w, s, m) -> new WorldGenTrees(true, 5, Blocks.LOG.getDefaultState(), UBlocks.apple_leaves.getDefaultState(), false));
    public static final Block apple_leaves = new BlockFruitLeaves(Unicopia.MODID, "apple_leaves", apple_tree)
            .setBaseGrowthChance(1200)
            .setTint(0xFFEE81)
            .setHarvestFruit(w -> UItems.apple.getRandomApple())
            .setUnharvestFruit(w -> new ItemStack(UItems.rotten_apple));

    static void init(IForgeRegistry<Block> registry) {
        registry.registerAll(cloud, cloud_stairs, cloud_double_slab, cloud_slab, cloud_fence, cloud_banister,
                             mist_door, anvil, cloud_farmland,
                             sugar_block,
                             alfalfa,
                             tomato_plant,
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
}
