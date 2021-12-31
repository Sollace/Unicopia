package com.minelittlepony.unicopia.block.state;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Material;
import net.minecraft.block.OreBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StateMaps {
    private static final Registry<BlockStateConverter> REGISTRY = Registries.createSimple(new Identifier("unicopia", "state_map"));

    public static final BlockStateConverter SNOW_PILED = register("snow_piled", new BlockStateMap.Builder()
            .add(StateMapping.cycleProperty(Blocks.SNOW, SnowBlock.LAYERS, 7)));

    public static final BlockStateConverter ICE_AFFECTED = register("ice", new BlockStateMap.Builder()
            .replaceMaterial(Material.WATER, Blocks.FROSTED_ICE)
            .replaceMaterial(Material.LAVA, UBlocks.FROSTED_OBSIDIAN)
            .add(StateMapping.cycleProperty(Blocks.SNOW, SnowBlock.LAYERS, 7))
            .replaceBlock(Blocks.FIRE, Blocks.AIR)
            .setProperty(Blocks.REDSTONE_WIRE, RedstoneWireBlock.POWER, 0));

    public static final BlockStateConverter SILVERFISH_AFFECTED = register("infestation", new BlockStateMap.Builder()
            .replaceBlock(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS)
            .replaceBlock(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE)
            .replaceBlock(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS)
            .replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS)
            .replaceBlock(Blocks.STONE, Blocks.INFESTED_STONE)
            .replaceBlock(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS));

    public static final BlockStateConverter FIRE_AFFECTED = register("fire", new BlockStateMap.Builder()
            .removeBlock(Blocks.SNOW)
            .removeBlock(Blocks.SNOW_BLOCK)
            .removeBlock(StateMaps::isPlant)
            .replaceBlock(BlockTags.ICE, Blocks.WATER)
            .replaceBlock(Blocks.CLAY, Blocks.BROWN_CONCRETE)
            .replaceBlock(Blocks.OBSIDIAN, Blocks.LAVA)
            .replaceBlock(UBlocks.FROSTED_OBSIDIAN, Blocks.LAVA)
            .replaceBlock(Blocks.GRASS_BLOCK, Blocks.DIRT)
            .replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE)
            .replaceBlock(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL)
            .replaceBlock(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS)
            .replaceBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS)
            .replaceBlock(Blocks.PODZOL, Blocks.COARSE_DIRT)
            .setProperty(Blocks.FARMLAND, FarmlandBlock.MOISTURE, 0)
            .add(StateMapping.build(isOf(Blocks.DIRT), (w, s) -> (w.random.nextFloat() <= 0.15 ? Blocks.COARSE_DIRT.getDefaultState() : s))));

    public static final ReversableBlockStateConverter HELLFIRE_AFFECTED = register("hellfire", new ReversableBlockStateMap.Builder()
            .replaceBlock(Blocks.GRASS_BLOCK, Blocks.WARPED_NYLIUM)
            .replaceBlock(Blocks.STONE, Blocks.NETHERRACK)
            .replaceBlock(Blocks.SAND, Blocks.SOUL_SAND)
            .replaceBlock(Blocks.GRAVEL, Blocks.SOUL_SAND)
            .replaceBlock(Blocks.DIRT, Blocks.SOUL_SOIL)
            .replaceBlock(Blocks.COARSE_DIRT, Blocks.SOUL_SOIL)
            .replaceBlock(Blocks.TORCH, Blocks.SOUL_TORCH)
            .replaceBlock(Blocks.WALL_TORCH, Blocks.SOUL_WALL_TORCH)
            .replaceBlock(Blocks.OAK_LOG, Blocks.WARPED_STEM)
            .replaceBlock(Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_WARPED_STEM)
            .replaceBlock(Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_WARPED_HYPHAE)
            .replaceBlock(Blocks.OAK_PLANKS, Blocks.WARPED_PLANKS)
            .replaceBlock(Blocks.OAK_DOOR, Blocks.WARPED_DOOR)
            .replaceBlock(Blocks.OAK_STAIRS, Blocks.WARPED_STAIRS)
            .replaceBlock(Blocks.OAK_TRAPDOOR, Blocks.WARPED_TRAPDOOR)
            .replaceBlock(Blocks.OAK_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE)
            .replaceBlock(Blocks.OAK_BUTTON, Blocks.WARPED_BUTTON)
            .replaceBlock(Blocks.OAK_FENCE, Blocks.WARPED_FENCE)
            .replaceBlock(Blocks.OAK_FENCE_GATE, Blocks.WARPED_FENCE_GATE)
            .replaceBlock(BlockTags.LEAVES, Blocks.WARPED_HYPHAE)
            .replaceMaterial(Material.WATER, Blocks.OBSIDIAN)
            .add(StateMapping.build(StateMaps::isPlant, Blocks.NETHER_WART, s -> StateMapping.replaceBlock(Blocks.NETHER_WART, Blocks.GRASS)))
            .add(StateMapping.build(s -> !s.isOf(Blocks.NETHER_QUARTZ_ORE) && isOre(s), Blocks.NETHER_QUARTZ_ORE, s -> StateMapping.replaceBlock(Blocks.NETHER_QUARTZ_ORE, Blocks.COAL_ORE))));

    private static <T extends BlockStateConverter> T register(String name, BlockStateMap.Builder value) {
        return Registry.register(REGISTRY, new Identifier("unicopia", name), value.build());
    }

    static Predicate<BlockState> isOf(Block block) {
        return s -> s.isOf(block);
    }
    static boolean isPlant(BlockState s) {
        return s.getBlock() instanceof PlantBlock;
    }
    static boolean isOre(BlockState s) {
        return s.getBlock() instanceof OreBlock;
    }
}
