package com.minelittlepony.unicopia.block;

import java.util.function.Function;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.TintedParticleLeavesBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface BlockConstructionUtils {
    static Function<AbstractBlock.Settings, ButtonBlock> woodenButton(BlockSetType setType) {
        return s -> new ButtonBlock(setType, 30, s.noCollision().strength(0.5f).pistonBehavior(PistonBehavior.DESTROY));
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static Function<AbstractBlock.Settings, StrippablePillarBlock> createMetallicLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return s -> new StrippablePillarBlock(s
                .mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
                .instrument(NoteBlockInstrument.BELL)
                .strength(3.0f)
                .sounds(BlockSoundGroup.METAL));
    }

    static Function<AbstractBlock.Settings, PillarBlock> createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return s -> new PillarBlock(s
                .mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
                .burnable());
    }

    static Function<AbstractBlock.Settings, PillarBlock> createWoodBlock(MapColor mapColor) {
        return s -> new PillarBlock(s
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
                .burnable());
    }

    static Function<AbstractBlock.Settings, StrippablePillarBlock> createMetallicWoodBlock(MapColor mapColor) {
        return s -> new StrippablePillarBlock(s
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BELL)
                .strength(3.0f)
                .sounds(BlockSoundGroup.METAL));
    }

    static Function<AbstractBlock.Settings, LeavesBlock> createLeavesBlock(BlockSoundGroup soundGroup) {
        return s -> new TintedParticleLeavesBlock(0.01F, s
                .mapColor(MapColor.DARK_GREEN)
                .strength(0.2f)
                .ticksRandomly()
                .sounds(soundGroup)
                .nonOpaque()
                .allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves)
                .suffocates(BlockConstructionUtils::never)
                .blockVision(BlockConstructionUtils::never)
                .burnable()
                .pistonBehavior(PistonBehavior.DESTROY)
                .solidBlock(BlockConstructionUtils::never));
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
