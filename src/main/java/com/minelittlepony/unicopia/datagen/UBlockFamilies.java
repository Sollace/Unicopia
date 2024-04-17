package com.minelittlepony.unicopia.datagen;

import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.data.family.BlockFamily;

public interface UBlockFamilies {
    BlockFamily PALM = new BlockFamily.Builder(UBlocks.PALM_PLANKS)
            .slab(UBlocks.PALM_SLAB).stairs(UBlocks.PALM_STAIRS).fence(UBlocks.PALM_FENCE).fenceGate(UBlocks.PALM_FENCE_GATE)
            .button(UBlocks.PALM_BUTTON).pressurePlate(UBlocks.PALM_PRESSURE_PLATE).sign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN)
            .door(UBlocks.PALM_DOOR).trapdoor(UBlocks.PALM_TRAPDOOR)
            .group("wooden").unlockCriterionName("has_planks")
            .build();
    BlockFamily ZAP = new BlockFamily.Builder(UBlocks.ZAP_PLANKS)
            .slab(UBlocks.ZAP_SLAB).stairs(UBlocks.ZAP_STAIRS).fence(UBlocks.ZAP_FENCE).fenceGate(UBlocks.ZAP_FENCE_GATE)
            .group("wooden").unlockCriterionName("has_planks")
            .build();
    BlockFamily WAXED_ZAP = new BlockFamily.Builder(UBlocks.WAXED_ZAP_PLANKS)
            .slab(UBlocks.WAXED_ZAP_SLAB).stairs(UBlocks.WAXED_ZAP_STAIRS).fence(UBlocks.WAXED_ZAP_FENCE).fenceGate(UBlocks.WAXED_ZAP_FENCE_GATE)
            .group("wooden").unlockCriterionName("has_planks")
            .build();
    BlockFamily CHISELED_CHITIN = new BlockFamily.Builder(UBlocks.CHISELLED_CHITIN)
            .slab(UBlocks.CHISELLED_CHITIN_SLAB).stairs(UBlocks.CHISELLED_CHITIN_STAIRS)
            .group("chitin").unlockCriterionName("has_chiselled_chitin")
            .build();
    BlockFamily CLOUD = new BlockFamily.Builder(UBlocks.CLOUD)
            .slab(UBlocks.CLOUD_SLAB).stairs(UBlocks.CLOUD_STAIRS)
            .group("cloud").unlockCriterionName("has_cloud_lump")
            .build();
    BlockFamily ETCHED_CLOUD = new BlockFamily.Builder(UBlocks.ETCHED_CLOUD)
            .slab(UBlocks.ETCHED_CLOUD_SLAB).stairs(UBlocks.ETCHED_CLOUD_STAIRS)
            .group("etched_cloud").unlockCriterionName("has_cloud_lump")
            .build();
    BlockFamily CLOUD_PLANKS = new BlockFamily.Builder(UBlocks.CLOUD_PLANKS)
            .slab(UBlocks.CLOUD_PLANK_SLAB).stairs(UBlocks.CLOUD_PLANK_STAIRS)
            .group("cloud").unlockCriterionName("has_cloud")
            .build();
    BlockFamily CLOUD_BRICKS = new BlockFamily.Builder(UBlocks.CLOUD_BRICKS)
            .slab(UBlocks.CLOUD_BRICK_SLAB).stairs(UBlocks.CLOUD_BRICK_STAIRS)
            .group("cloud").unlockCriterionName("has_cloud_bricks")
            .build();
    BlockFamily DENSE_CLOUD = new BlockFamily.Builder(UBlocks.DENSE_CLOUD)
            .slab(UBlocks.DENSE_CLOUD_SLAB).stairs(UBlocks.DENSE_CLOUD_STAIRS)
            .group("cloud").unlockCriterionName("has_dense_cloud")
            .build();
}
