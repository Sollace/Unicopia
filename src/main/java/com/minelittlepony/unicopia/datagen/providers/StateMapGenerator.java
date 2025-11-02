package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.state.ReversableBlockStateConverter;
import com.minelittlepony.unicopia.block.state.ReversableStateChange;
import com.minelittlepony.unicopia.block.state.StateMapLoader;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.block.state.StatePredicate.PropertyOp.Comparison;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;

public class StateMapGenerator {

    public void generate(BiConsumer<StateMapLoader.Indirect, ReversableBlockStateConverter.Builder> exporter) {
        exporter.accept(StateMaps.SNOW_PILED, ReversableBlockStateConverter.builder().apply(this::appendSnowLayering));
        exporter.accept(StateMaps.ICE_AFFECTED, ReversableBlockStateConverter.builder()
                .apply(this::appendSnowLayering)
                .add(fluid(FluidTags.WATER), setState(Blocks.FROSTED_ICE))
                .add(fluid(FluidTags.LAVA), setState(UBlocks.FROSTED_OBSIDIAN))
                .add(tag(BlockTags.FIRE), setState(Blocks.AIR))
                .add(state(Blocks.REDSTONE_WIRE), setProperty(RedstoneWireBlock.POWER, 0))
        );
        exporter.accept(StateMaps.SILVERFISH_AFFECTED, ReversableBlockStateConverter.builder()
                .add(state(Blocks.CHISELED_STONE_BRICKS), setState(Blocks.INFESTED_CHISELED_STONE_BRICKS))
                .add(state(Blocks.COBBLESTONE), setState(Blocks.INFESTED_COBBLESTONE))
                .add(state(Blocks.CRACKED_STONE_BRICKS), setState(Blocks.INFESTED_CRACKED_STONE_BRICKS))
                .add(state(Blocks.MOSSY_STONE_BRICKS), setState(Blocks.INFESTED_MOSSY_STONE_BRICKS))
                .add(state(Blocks.STONE), setState(Blocks.INFESTED_STONE))
                .add(state(Blocks.STONE_BRICKS), setState(Blocks.INFESTED_STONE_BRICKS))
        );
        exporter.accept(StateMaps.FIRE_AFFECTED, ReversableBlockStateConverter.builder()
                .apply(this::appendFireAffected));
        exporter.accept(StateMaps.BURNABLE, ReversableBlockStateConverter.builder()
                .apply(this::appendFireAffected)
                .add(tag(BlockTags.LOGS_THAT_BURN), chance(setState(Blocks.COAL_BLOCK), 0.15F))
        );
        exporter.accept(StateMaps.HELLFIRE_AFFECTED, ReversableBlockStateConverter.builder()
                .apply(this::appendCracking)
                .apply(this::appendSmelting)
                .add(state(Blocks.GRASS_BLOCK), setState(Blocks.WARPED_NYLIUM))
                .add(state(Blocks.STONE), setState(Blocks.NETHERRACK))
                .add(tag(BlockTags.SAND), setState(Blocks.SOUL_SAND))
                .add(tag(BlockTags.DIRT), setState(Blocks.SOUL_SOIL))
                .add(state(Blocks.TORCH), setState(Blocks.SOUL_TORCH))
                .add(state(Blocks.WALL_TORCH), setState(Blocks.SOUL_WALL_TORCH))
                .add(state(Blocks.OAK_LOG), setState(Blocks.WARPED_STEM))
                .add(state(Blocks.STRIPPED_OAK_LOG), setState(Blocks.STRIPPED_WARPED_STEM))
                .add(state(Blocks.OAK_WOOD), setState(Blocks.WARPED_HYPHAE))
                .add(state(Blocks.STRIPPED_OAK_WOOD), setState(Blocks.STRIPPED_WARPED_HYPHAE))
                .add(tag(BlockTags.PLANKS), setState(Blocks.WARPED_PLANKS))
                .add(tag(BlockTags.WOODEN_DOORS), setState(Blocks.WARPED_DOOR))
                .add(tag(BlockTags.WOODEN_STAIRS), setState(Blocks.WARPED_STAIRS))
                .add(tag(BlockTags.WOODEN_TRAPDOORS), setState(Blocks.WARPED_TRAPDOOR))
                .add(tag(BlockTags.WOODEN_PRESSURE_PLATES), setState(Blocks.WARPED_PRESSURE_PLATE))
                .add(tag(BlockTags.WOODEN_FENCES), setState(Blocks.WARPED_FENCE))
                .add(tag(BlockTags.WOODEN_SLABS), setState(Blocks.WARPED_SLAB))
                .add(tag(BlockTags.WOODEN_BUTTONS), setState(Blocks.WARPED_BUTTON))
                .add(tag(BlockTags.FENCE_GATES), setState(Blocks.WARPED_FENCE_GATE))
                .add(tag(BlockTags.LEAVES), setState(Blocks.WARPED_HYPHAE))
                .add(state(Blocks.WATER), setState(Blocks.OBSIDIAN))
                .add(StatePredicate.Plants.INSTANCE, setState(Blocks.NETHER_WART), state(Blocks.NETHER_WART), setState(Blocks.SHORT_GRASS))
                .add(tag(ConventionalBlockTags.ORES), setState(Blocks.NETHER_QUARTZ_ORE))
        );
    }

    private ReversableBlockStateConverter.Builder appendSnowLayering(ReversableBlockStateConverter.Builder builder) {
        return builder.add(state(Blocks.SNOW, property(SnowBlock.LAYERS, 7, Comparison.LESS)), cycleProperty(SnowBlock.LAYERS));
    }

    private ReversableBlockStateConverter.Builder appendFireAffected(ReversableBlockStateConverter.Builder builder) {
        return builder
            .add(anyOf(state(Blocks.SNOW), state(Blocks.SNOW_BLOCK), StatePredicate.Plants.INSTANCE), setState(Blocks.AIR))
            .add(anyOf(state(Blocks.MUD), state(Blocks.GRASS_BLOCK)), setState(Blocks.DIRT))
            .add(anyOf(state(Blocks.OBSIDIAN), state(Blocks.CRYING_OBSIDIAN), state(UBlocks.FROSTED_OBSIDIAN)), setState(Blocks.LAVA))
            .add(tag(BlockTags.ICE), setState(Blocks.WATER))
            .add(state(Blocks.CLAY), setState(Blocks.BROWN_CONCRETE))
            .add(anyOf(state(Blocks.MOSSY_COBBLESTONE), state(Blocks.INFESTED_COBBLESTONE)), setState(Blocks.COBBLESTONE))
            .add(state(Blocks.MOSSY_COBBLESTONE_SLAB), setState(Blocks.COBBLESTONE_SLAB))
            .add(state(Blocks.MOSSY_COBBLESTONE_STAIRS), setState(Blocks.COBBLESTONE_STAIRS))
            .add(state(Blocks.MOSSY_COBBLESTONE_WALL), setState(Blocks.COBBLESTONE_WALL))
            .add(state(Blocks.MOSSY_STONE_BRICK_SLAB), setState(Blocks.STONE_BRICK_SLAB))
            .add(state(Blocks.MOSSY_STONE_BRICK_STAIRS), setState(Blocks.STONE_BRICK_STAIRS))
            .add(state(Blocks.MOSSY_STONE_BRICK_WALL), setState(Blocks.STONE_BRICK_WALL))
            .add(anyOf(state(Blocks.MOSSY_STONE_BRICKS), state(Blocks.INFESTED_MOSSY_STONE_BRICKS), state(Blocks.INFESTED_STONE_BRICKS)), setState(Blocks.STONE_BRICKS))
            .add(state(Blocks.INFESTED_CHISELED_STONE_BRICKS), setState(Blocks.CHISELED_STONE_BRICKS))
            .add(state(Blocks.INFESTED_CRACKED_STONE_BRICKS), setState(Blocks.CRACKED_STONE_BRICKS))
            .add(state(Blocks.INFESTED_STONE), setState(Blocks.STONE))
            .add(state(Blocks.PODZOL), setState(Blocks.COARSE_DIRT))
            .add(state(Blocks.DIRT), chance(setState(Blocks.COARSE_DIRT), 0.15F));
    }

    private ReversableBlockStateConverter.Builder appendCracking(ReversableBlockStateConverter.Builder builder) {
        return builder
            .add(state(Blocks.STONE_BRICKS), chance(setState(Blocks.CRACKED_STONE_BRICKS), 0.15F))
            .add(state(Blocks.DEEPSLATE_BRICKS), chance(setState(Blocks.CRACKED_DEEPSLATE_BRICKS), 0.15F))
            .add(state(Blocks.NETHER_BRICKS), chance(setState(Blocks.CRACKED_NETHER_BRICKS), 0.15F))
            .add(state(Blocks.POLISHED_BLACKSTONE_BRICKS), chance(setState(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS), 0.15F))
            .add(state(Blocks.INFESTED_STONE_BRICKS), chance(setState(Blocks.INFESTED_CRACKED_STONE_BRICKS), 0.15F))
            .add(state(Blocks.DEEPSLATE_TILES), chance(setState(Blocks.CRACKED_DEEPSLATE_TILES), 0.15F));
    }

    private ReversableBlockStateConverter.Builder appendSmelting(ReversableBlockStateConverter.Builder builder) {
        return builder
            .add(state(Blocks.RAW_IRON_BLOCK), chance(setState(Blocks.IRON_BLOCK), 0.05F))
            .add(state(Blocks.RAW_COPPER_BLOCK), chance(setState(Blocks.COPPER_BLOCK), 0.05F))
            .add(state(Blocks.RAW_GOLD_BLOCK), chance(setState(Blocks.GOLD_BLOCK), 0.15F));
    }

    static ReversableStateChange chance(ReversableStateChange change, float chance) {
        return new ReversableStateChange.Chance(change, chance);
    }

    static <T extends Comparable<T>> ReversableStateChange setProperty(Property<T> property, T value) {
        return new ReversableStateChange.SetProperty(property.getName(), property.name(value));
    }

    static <T extends Comparable<T>> ReversableStateChange setState(Block block, StatePredicate.PropertyOp...properties) {
        return new ReversableStateChange.SetState(state(block, properties));
    }

    static <T extends Comparable<T>> ReversableStateChange cycleProperty(Property<T> property) {
        return new ReversableStateChange.CycleProperty(property.getName());
    }

    static <T extends Comparable<T>> StatePredicate.PropertyOp property(Property<T> property, T value, Comparison comparison) {
        return new StatePredicate.PropertyOp(property.getName(), property.name(value), comparison);
    }

    static StatePredicate.State state(Block block, StatePredicate.PropertyOp...properties) {
        return new StatePredicate.State(Registries.BLOCK.getId(block), List.of(properties));
    }

    static StatePredicate.FluidTag fluid(TagKey<Fluid> tag) {
        return new StatePredicate.FluidTag(tag);
    }

    static StatePredicate.Tag tag(TagKey<Block> tag) {
        return new StatePredicate.Tag(tag);
    }

    static StatePredicate anyOf(StatePredicate...predicates) {
        return new StatePredicate.Union(List.of(predicates), StatePredicate.Union.Combiner.OR);
    }
}
