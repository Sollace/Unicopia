package com.minelittlepony.unicopia.blockstate;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;

interface StateMapping extends Predicate<BlockState>, Function<BlockState, BlockState> {

    static StateMapping removeBlock(Predicate<BlockState> mapper) {
        return build(
                mapper,
                s -> Blocks.AIR.getDefaultState());
    }

    static StateMapping replaceBlock(Block from, Block to) {
        return build(
                s -> s.getBlock() == from,
                s -> to.getDefaultState(),
                s -> replaceBlock(to, from));
    }

    static <T extends Comparable<T>> StateMapping replaceProperty(Block block, Property<T> property, T from, T to) {
        return build(
                s -> s.getBlock() == block && s.get(property) == from,
                s -> s.with(property, to),
                s -> replaceProperty(block, property, to, from));
    }

    static <T extends Comparable<T>> StateMapping setProperty(Block block, Property<T> property, T to) {
        return build(
                s -> s.getBlock() == block,
                s -> s.with(property, to));
    }

    static StateMapping build(Predicate<BlockState> predicate, Function<BlockState, BlockState> converter) {
        return build(predicate, converter, s -> s);
    }

    static StateMapping build(Predicate<BlockState> predicate, Function<BlockState, BlockState> converter, Function<StateMapping, StateMapping> inverter) {
        return new StateMapping() {
            private StateMapping inverse;

            @Override
            public boolean test(BlockState state) {
                return predicate.test(state);
            }

            @Override
            public BlockState apply(BlockState state) {
                return converter.apply(state);
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
    default BlockState apply(@Nonnull BlockState state) {
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
