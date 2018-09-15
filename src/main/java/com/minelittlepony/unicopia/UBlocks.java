package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.block.BlockCloud;
import com.minelittlepony.unicopia.block.BlockCloudSlab;
import com.minelittlepony.unicopia.block.BlockCloudStairs;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

public class UBlocks {
    public static final BlockCloud cloud = new BlockCloud(UMaterials.cloud, Unicopia.MODID, "cloud_block");

    public static final BlockCloudStairs stairsCloud = new BlockCloudStairs(UBlocks.cloud.getDefaultState(), Unicopia.MODID, "cloud_stairs");

    public static final BlockCloudSlab cloud_double_slab = new BlockCloudSlab(true, UMaterials.cloud, Unicopia.MODID, "cloud_double_slab");
    public static final BlockCloudSlab cloud_slab = new BlockCloudSlab(false, UMaterials.cloud, Unicopia.MODID, "cloud_slab");

    static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.registerAll(cloud, stairsCloud, cloud_double_slab, cloud_slab);
    }
}
