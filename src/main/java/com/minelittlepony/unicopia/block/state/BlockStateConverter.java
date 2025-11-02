package com.minelittlepony.unicopia.block.state;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockStateConverter {

    static Named of(Identifier id) {
        return new Named(id);
    }

    /**
     * Checks if this collection contains a mapping capable of converting the given state.
     *
     * @param state        State to check
     *
     * @return    True if the state can be converted
     */
    boolean canConvert(@Nullable BlockState state);

    /**
     * Attempts to convert the given state based on the known mappings in this collection.
     *
     * @param world        The world
     * @param state        State to convert
     *
     * @return    The converted state if there is one, otherwise the original state is returned
     */
    @NotNull
    Optional<BlockState> getConverted(World world, @NotNull BlockState state);

    /**
     * Attempts to convert a block state at a position.
     * Returns true if the block was changed.
     *
     */
    default boolean convert(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return canConvert(state) && getConverted(world, state).filter(newState -> {
            if (state.equals(newState)) {
                return false;
            }

            if (!newState.contains(Properties.DOUBLE_BLOCK_HALF)) {
                world.setBlockState(pos, newState, Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
                return true;
            }

            // for two-tall blocks (like doors) we have to update it's sibling
            boolean lower = newState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
            BlockPos other = lower ? pos.up() : pos.down();

            if (world.getBlockState(other).isOf(state.getBlock())) {
                world.setBlockState(other, newState.with(Properties.DOUBLE_BLOCK_HALF, lower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER), Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
                world.setBlockState(pos, newState, Block.FORCE_STATE | Block.NOTIFY_LISTENERS);

                return true;
            }

            return false;
        }).isPresent();
    }

    public static sealed class Named implements ReversableBlockStateConverter permits Named.Inverted {
        private final Identifier id;
        private final ReversableBlockStateConverter inverse;

        public Named(Identifier id) {
            this.id = id;
            this.inverse = new Inverted(this);
        }

        protected Named(Named inverse) {
            this.id = inverse.getId();
            this.inverse = inverse;
        }

        public Identifier getId() {
            return id;
        }

        @Override
        public boolean canConvert(@Nullable BlockState state) {
            return get().filter(map -> map.canConvert(state)).isPresent();
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(World world, @NotNull BlockState state) {
            return get().flatMap(map -> map.getConverted(world, state));
        }

        public Optional<ReversableBlockStateConverter> get() {
            return Optional.ofNullable(StateMapLoader.INSTANCE.converters.get(id));
        }

        @Override
        public ReversableBlockStateConverter getInverse() {
            return inverse;
        }

        private final class Inverted extends Named {
            private Inverted(Named inverse) {
                super(inverse);
            }

            @Override
            public Optional<ReversableBlockStateConverter> get() {
                return super.get().map(ReversableBlockStateConverter::getInverse);
            }
        }
    }

}
