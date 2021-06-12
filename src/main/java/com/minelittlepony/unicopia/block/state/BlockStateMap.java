package com.minelittlepony.unicopia.block.state;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.world.World;

/**
 * A collection of block-state mappings.
 *
 */
class BlockStateMap implements BlockStateConverter {
    private final List<StateMapping> mappings;

    BlockStateMap(List<StateMapping> mappings) {
        this.mappings = new ArrayList<>(mappings);
    }

    @Override
    public boolean canConvert(@Nullable BlockState state) {
        return state != null && mappings.stream().anyMatch(i -> i.test(state));
    }

    @Override
    @Nonnull
    public BlockState getConverted(World world, @Nonnull BlockState state) {
        for (StateMapping i : mappings) {
            if (i.test(state)) {
                return i.apply(world, state);
            }
        }

        return state;
    }

    public static class Builder {
        protected final ArrayList<StateMapping> items = new ArrayList<>();

        public Builder add(StateMapping mapping) {
            items.add(mapping);
            return this;
        }

        public Builder removeBlock(Predicate<BlockState> mapper) {
            return add(StateMapping.removeBlock(mapper));
        }

        public Builder removeBlock(Block from) {
            return add(StateMapping.removeBlock(s -> s.isOf(from)));
        }

        public Builder replaceMaterial(Material from, Block to) {
            return add(StateMapping.replaceMaterial(from, to));
        }

        public Builder replaceBlock(Block from, Block to) {
            return add(StateMapping.replaceBlock(from, to));
        }

        public Builder replaceBlock(Tag<Block> from, Block to) {
            return add(StateMapping.replaceBlock(from, to));
        }

        public <T extends Comparable<T>> Builder replaceProperty(Block block, Property<T> property, T from, T to) {
            return add(StateMapping.replaceProperty(block, property, from, to));
        }

        public <T extends Comparable<T>> Builder setProperty(Block block, Property<T> property, T to) {
            return add(StateMapping.build(
                    s -> s.isOf(block),
                    (w, s) -> s.with(property, to)));
        }

        @SuppressWarnings("unchecked")
        public <T extends BlockStateConverter> T build() {
            return (T)new BlockStateMap(items);
        }
    }
}
