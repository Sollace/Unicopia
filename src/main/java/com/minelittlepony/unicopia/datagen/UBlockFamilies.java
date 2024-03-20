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
}
