package com.minelittlepony.unicopia.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

interface BlockConstructionUtils {
    static ButtonBlock woodenButton() {
        return woodenButton(BlockSoundGroup.WOOD, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON);
    }

    static ButtonBlock woodenButton(BlockSoundGroup soundGroup, SoundEvent clickOffSound, SoundEvent clickOnSound) {
        return new ButtonBlock(AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5f).sounds(soundGroup), 30, true, clickOffSound, clickOnSound);
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return new PillarBlock(AbstractBlock.Settings.of(Material.WOOD, state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).strength(2).sounds(BlockSoundGroup.WOOD));
    }

    static PillarBlock createWoodBlock(MapColor mapColor) {
        return new PillarBlock(AbstractBlock.Settings.of(Material.WOOD, mapColor).strength(2).sounds(BlockSoundGroup.WOOD));
    }

    static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup) {
        return new LeavesBlock(AbstractBlock.Settings.of(Material.LEAVES).strength(0.2F).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves).suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never));
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
