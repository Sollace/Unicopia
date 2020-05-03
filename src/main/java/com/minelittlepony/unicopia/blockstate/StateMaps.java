package com.minelittlepony.unicopia.blockstate;

import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Material;
import net.minecraft.block.OreBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.Util;

public class StateMaps {
    public static final BlockStateConverter ICE_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.WATER,
                s -> Blocks.ICE.getDefaultState()));
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.LAVA,
                s -> Blocks.OBSIDIAN.getDefaultState()));
        a.add(StateMapping.build(
                s -> s.getBlock() == Blocks.SNOW,
                s -> {
                    s = s.cycle(SnowBlock.LAYERS);
                    if (s.get(SnowBlock.LAYERS) >= 7) {
                        return Blocks.SNOW_BLOCK.getDefaultState();
                    }

                    return s;
                }));
        a.replaceBlock(Blocks.FIRE, Blocks.AIR);
        a.setProperty(Blocks.REDSTONE_WIRE, RedstoneWireBlock.POWER, 0);
    }), "ice");

    public static final ReversableBlockStateConverter MOSS_AFFECTED = register(Util.make(new ReversableBlockStateMap(), a -> {
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.COBBLESTONE_SLAB);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.COBBLESTONE_STAIRS);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.STONE_BRICK_SLAB);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS);
        a.replaceBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
    }), "moss");

    public static final BlockStateConverter FIRE_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.removeBlock(s -> s.getBlock() == Blocks.SNOW || s.getBlock() == Blocks.SNOW_BLOCK);
        a.removeBlock(s -> s.getBlock() instanceof PlantBlock);
        a.replaceBlock(Blocks.CLAY, Blocks.BROWN_CONCRETE);
        a.replaceBlock(Blocks.OBSIDIAN, Blocks.LAVA);
        a.replaceBlock(Blocks.GRASS, Blocks.DIRT);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS);
        a.replaceBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        a.replaceBlock(Blocks.PODZOL, Blocks.COARSE_DIRT);
        a.setProperty(Blocks.FARMLAND, FarmlandBlock.MOISTURE, 0);
        a.add(StateMapping.build(
                s -> s.getBlock() == Blocks.DIRT,
                s -> (Math.random() <= 0.15 ? Blocks.COARSE_DIRT.getDefaultState() : s)));
    }), "fire");

    public static final BlockStateConverter HELLFIRE_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.add(StateMapping.build(
                s -> s.getBlock() == Blocks.GRASS || s.getBlock() == Blocks.DIRT || s.getBlock() == Blocks.STONE,
                s -> Blocks.NETHERRACK.getDefaultState()));
        a.replaceBlock(Blocks.SAND, Blocks.SOUL_SAND);
        a.replaceBlock(Blocks.GRAVEL, Blocks.SOUL_SAND);
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.WATER,
                s -> Blocks.OBSIDIAN.getDefaultState()));
        a.add(StateMapping.build(
                s -> s.getBlock() instanceof PlantBlock,
                s -> Blocks.NETHER_WART.getDefaultState()));
        a.add(StateMapping.build(
                s -> (s.getBlock() != Blocks.NETHER_QUARTZ_ORE) && (s.getBlock() instanceof OreBlock),
                s -> Blocks.NETHER_QUARTZ_ORE.getDefaultState()));
    }), "hellfire");

    private static <T extends BlockStateConverter> T register(T value, String name) {
        return value;
    }
}
