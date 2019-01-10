package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.block.BlockAlfalfa;
import com.minelittlepony.unicopia.block.BlockCloud;
import com.minelittlepony.unicopia.block.BlockCloudAnvil;
import com.minelittlepony.unicopia.block.BlockCloudSlab;
import com.minelittlepony.unicopia.block.BlockCloudStairs;
import com.minelittlepony.unicopia.block.BlockSugar;
import com.minelittlepony.unicopia.block.BlockTomatoPlant;
import com.minelittlepony.unicopia.block.BlockCloudDoor;
import com.minelittlepony.unicopia.block.BlockCloudFarm;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

public class UBlocks {
    public static final BlockCloud cloud = new BlockCloud(UMaterials.cloud, Unicopia.MODID, "cloud_block");

    public static final BlockCloudStairs cloud_stairs = new BlockCloudStairs(UBlocks.cloud.getDefaultState(), Unicopia.MODID, "cloud_stairs");

    public static final BlockCloudSlab double_cloud_slab = new BlockCloudSlab(true, UMaterials.cloud, Unicopia.MODID, "cloud_double_slab");
    public static final BlockCloudSlab cloud_slab = new BlockCloudSlab(false, UMaterials.cloud, Unicopia.MODID, "cloud_slab");

    public static final BlockCloudDoor mist_door = new BlockCloudDoor(UMaterials.cloud, Unicopia.MODID, "mist_door");

    public static final BlockCloudAnvil anvil = new BlockCloudAnvil(Unicopia.MODID, "anvil");

    public static final BlockAlfalfa alfalfa = new BlockAlfalfa(Unicopia.MODID, "alfalfa");

    public static final BlockTomatoPlant tomato_plant = new BlockTomatoPlant(Unicopia.MODID, "tomato_plant");

    public static final BlockCloudFarm cloud_farmland = new BlockCloudFarm(Unicopia.MODID, "cloud_farmland");

    public static final Block sugar_block = new BlockSugar(Unicopia.MODID, "sugar_block");

    static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.registerAll(cloud, cloud_stairs, double_cloud_slab, cloud_slab, mist_door, anvil, cloud_farmland,
                             sugar_block,
                             alfalfa,
                             tomato_plant);
    }
}
