package com.minelittlepony.unicopia.block.state;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.world.World;

interface StateMapping extends Predicate<BlockState>, BiFunction<World, BlockState, BlockState> {

    Random RNG = new Random();

    static StateMapping removeBlock(Predicate<BlockState> mapper) {
        return build(
                mapper,
                (w, s) -> Blocks.AIR.getDefaultState());
    }

    static StateMapping removeBlock(Block from) {
        return removeBlock(s -> s.isOf(from));
    }

    static StateMapping replaceBlock(Tag<Block> tag, Block to) {
        return build(
                s -> s.isIn(tag),
                (w, s) -> to.getDefaultState(),
                s -> build(
                        p -> p.isOf(to),
                        (w, p) -> tag.getRandom(w.random).getDefaultState()
                    )
                );
    }

    @SuppressWarnings("unchecked")
    static StateMapping replaceBlock(Block from, Block to) {
        return build(
                s -> s.getBlock() == from,
                (w, s) -> {
                    BlockState newState = to.getDefaultState();
                    for (@SuppressWarnings("rawtypes") Property i : s.getProperties()) {
                        if (newState.contains(i)) {
                            newState = newState.with(i, s.get(i));
                        }
                    }
                    return newState;
                },
                s -> replaceBlock(to, from));
    }

    static <T extends Comparable<T>> StateMapping replaceProperty(Block block, Property<T> property, T from, T to) {
        return build(
                s -> s.getBlock() == block && s.get(property) == from,
                (w, s) -> s.with(property, to),
                s -> replaceProperty(block, property, to, from));
    }

    static <T extends Comparable<T>> StateMapping setProperty(Block block, Property<T> property, T to) {
        return build(
                s -> s.getBlock() == block,
                (w, s) -> s.with(property, to));
    }

    static StateMapping build(Predicate<BlockState> predicate, BiFunction<World, BlockState, BlockState> converter) {
        return build(predicate, converter, s -> s);
    }

    static StateMapping build(Predicate<BlockState> predicate, BiFunction<World, BlockState, BlockState> converter, Function<StateMapping, StateMapping> inverter) {
        return new StateMapping() {
            private StateMapping inverse;

            @Override
            public boolean test(BlockState state) {
                return predicate.test(state);
            }

            @Override
            public BlockState apply(World world, BlockState state) {
                return converter.apply(world, state);
            }

            @Override
            public StateMapping inverse() {
                if (inverse == null) {
                    inverse = inverter.apply(this);
                }
                return inverse;
            }
        };
    }

    /**
     * Checks if this state can be converted by this mapping
     *
     * @param state    State to check
     *
     * @return    True if the state can be converted
     */
    @Override
    default boolean test(@Nonnull BlockState state) {
        return true;
    }

    /**
     * Converts the given state based on this mapping
     *
     * @param state    State to convert
     *
     * @return    The converted state
     */
    @Nonnull
    @Override
    default BlockState apply(World world, @Nonnull BlockState state) {
        return state;
    }

    /**
     * Gets the inverse of this mapping if one exists. Otherwise returns itself.
     */
    @Nonnull
    default StateMapping inverse() {
        return this;
    }
}
