package com.minelittlepony.unicopia.block.state;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.SnowBlock;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.world.World;

interface StateMapping extends Predicate<BlockState>, BiFunction<World, BlockState, BlockState> {

    static StateMapping incrementSnow() {
        return build(
                isOf(Blocks.SNOW),
                (w, s) -> {
                    s = s.cycle(SnowBlock.LAYERS);
                    if (s.get(SnowBlock.LAYERS) >= 7) {
                        return Blocks.SNOW_BLOCK.getDefaultState();
                    }

                    return s;
                });
    }
    static Predicate<BlockState> isOf(Block block) {
        return s -> s.isOf(block);
    }

    static StateMapping replaceMaterial(Material mat, Block block) {
        return build(isOf(mat), block);
    }
    static Predicate<BlockState> isOf(Material mat) {
        return s -> s.getMaterial() == mat;
    }

    static StateMapping removeBlock(Predicate<BlockState> mapper) {
        return build(
                mapper,
                (w, s) -> Blocks.AIR.getDefaultState());
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
                s -> s.isOf(from),
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
                s -> s.isOf(block) && s.get(property) == from,
                (w, s) -> s.with(property, to),
                s -> replaceProperty(block, property, to, from));
    }

    static StateMapping build(Predicate<BlockState> predicate, Block result) {
        return build(predicate, (w, s) -> result.getDefaultState());
    }

    static StateMapping build(Predicate<BlockState> predicate, BiFunction<World, BlockState, BlockState> converter) {
        return build(predicate, converter, s -> s);
    }

    static StateMapping build(Predicate<BlockState> predicate, Block result, Function<StateMapping, StateMapping> inverter) {
        return build(predicate, (w, s) -> result.getDefaultState(), inverter);
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
    boolean test(@NotNull BlockState state);

    /**
     * Converts the given state based on this mapping
     *
     * @param state    State to convert
     *
     * @return    The converted state
     */
    @NotNull
    @Override
    BlockState apply(World world, @NotNull BlockState state);

    /**
     * Gets the inverse of this mapping if one exists. Otherwise returns itself.
     */
    @NotNull
    StateMapping inverse();
}
