package com.minelittlepony.unicopia.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface BlockConstructionUtils {
    static ButtonBlock woodenButton() {
        return woodenButton(BlockSoundGroup.WOOD, BlockSetType.OAK);
    }

    static ButtonBlock woodenButton(BlockSoundGroup soundGroup, BlockSetType setType) {
        return new ButtonBlock(AbstractBlock.Settings.create().noCollision().strength(0.5f).pistonBehavior(PistonBehavior.DESTROY).sounds(soundGroup), setType, 30, true);
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return new PillarBlock(AbstractBlock.Settings.create().mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable());
    }

    static PillarBlock createWoodBlock(MapColor mapColor) {
        return new PillarBlock(AbstractBlock.Settings.create().mapColor(mapColor).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable());
    }

    static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup) {
        return new LeavesBlock(AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).strength(0.2f).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves).suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never).burnable().pistonBehavior(PistonBehavior.DESTROY).solidBlock(BlockConstructionUtils::never));
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
