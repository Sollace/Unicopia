package com.minelittlepony.unicopia.block.state;

import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Material;
import net.minecraft.block.OreBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Util;

public class StateMaps {
    public static final BlockStateConverter ICE_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.WATER,
                (w, s) -> Blocks.ICE.getDefaultState()));
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.LAVA,
                (w, s) -> Blocks.OBSIDIAN.getDefaultState()));
        a.add(StateMapping.build(
                s -> s.getBlock() == Blocks.SNOW,
                (w, s) -> {
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

    public static final BlockStateConverter SILVERFISH_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.replaceBlock(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        a.replaceBlock(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        a.replaceBlock(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        a.replaceBlock(Blocks.STONE, Blocks.INFESTED_STONE);
        a.replaceBlock(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
    }), "infestation");

    public static final BlockStateConverter FIRE_AFFECTED = register(Util.make(new BlockStateMap(), a -> {
        a.removeBlock(Blocks.SNOW);
        a.removeBlock(Blocks.SNOW_BLOCK);
        a.removeBlock(s -> s.getBlock() instanceof PlantBlock);
        a.replaceBlock(Blocks.ICE, Blocks.WATER);
        a.replaceBlock(Blocks.PACKED_ICE, Blocks.WATER);
        a.replaceBlock(Blocks.CLAY, Blocks.BROWN_CONCRETE);
        a.replaceBlock(Blocks.OBSIDIAN, Blocks.LAVA);
        a.replaceBlock(Blocks.GRASS_BLOCK, Blocks.DIRT);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
        a.replaceBlock(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL);
        a.replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS);
        a.replaceBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        a.replaceBlock(Blocks.PODZOL, Blocks.COARSE_DIRT);
        a.setProperty(Blocks.FARMLAND, FarmlandBlock.MOISTURE, 0);
        a.add(StateMapping.build(
                s -> s.getBlock() == Blocks.DIRT,
                (w, s) -> (w.random.nextFloat() <= 0.15 ? Blocks.COARSE_DIRT.getDefaultState() : s)));
    }), "fire");

    public static final ReversableBlockStateConverter HELLFIRE_AFFECTED = register(Util.make(new ReversableBlockStateMap(), a -> {
        a.replaceBlock(Blocks.GRASS_BLOCK, Blocks.WARPED_NYLIUM);
        a.replaceBlock(Blocks.STONE, Blocks.NETHERRACK);
        a.replaceBlock(Blocks.SAND, Blocks.SOUL_SAND);
        a.replaceBlock(Blocks.GRAVEL, Blocks.SOUL_SAND);
        a.replaceBlock(Blocks.DIRT, Blocks.SOUL_SOIL);
        a.replaceBlock(Blocks.COARSE_DIRT, Blocks.SOUL_SOIL);
        a.replaceBlock(Blocks.TORCH, Blocks.SOUL_TORCH);
        a.replaceBlock(Blocks.WALL_TORCH, Blocks.SOUL_WALL_TORCH);
        a.replaceBlock(Blocks.OAK_LOG, Blocks.WARPED_STEM);
        a.replaceBlock(Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_WARPED_STEM);
        a.replaceBlock(Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_WARPED_HYPHAE);
        a.replaceBlock(Blocks.OAK_PLANKS, Blocks.WARPED_PLANKS);
        a.replaceBlock(Blocks.OAK_DOOR, Blocks.WARPED_DOOR);
        a.replaceBlock(Blocks.OAK_STAIRS, Blocks.WARPED_STAIRS);
        a.replaceBlock(Blocks.OAK_TRAPDOOR, Blocks.WARPED_TRAPDOOR);
        a.replaceBlock(Blocks.OAK_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE);
        a.replaceBlock(Blocks.OAK_BUTTON, Blocks.WARPED_BUTTON);
        a.replaceBlock(Blocks.OAK_FENCE, Blocks.WARPED_FENCE);
        a.replaceBlock(Blocks.OAK_FENCE_GATE, Blocks.WARPED_FENCE_GATE);
        a.replaceBlock(BlockTags.LEAVES, Blocks.WARPED_HYPHAE);
        a.add(StateMapping.build(
                s -> s.getMaterial() == Material.WATER,
                (w, s) -> Blocks.OBSIDIAN.getDefaultState(),
                s -> StateMapping.replaceBlock(Blocks.OBSIDIAN, Blocks.WATER)));
        a.add(StateMapping.build(
                s -> s.getBlock() instanceof PlantBlock,
                (w, s) -> Blocks.NETHER_WART.getDefaultState(),
                s -> StateMapping.replaceBlock(Blocks.NETHER_WART, Blocks.GRASS)));
        a.add(StateMapping.build(
                s -> (s.getBlock() != Blocks.NETHER_QUARTZ_ORE) && (s.getBlock() instanceof OreBlock),
                (w, s) -> Blocks.NETHER_QUARTZ_ORE.getDefaultState(),
                s -> StateMapping.replaceBlock(Blocks.NETHER_QUARTZ_ORE, Blocks.COAL_ORE)));
    }), "hellfire");

    private static <T extends BlockStateConverter> T register(T value, String name) {
        return value;
    }
}
