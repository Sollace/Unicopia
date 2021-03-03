package com.minelittlepony.unicopia.block.state;

import java.util.ArrayList;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.world.World;

/**
 * A collection of block-state mappings.
 *
 */
class BlockStateMap extends ArrayList<StateMapping> implements BlockStateConverter {
    private static final long serialVersionUID = 2602772651960588745L;

    public void removeBlock(Predicate<BlockState> mapper) {
        add(StateMapping.removeBlock(mapper));
    }

    public void removeBlock(Block from) {
        add(StateMapping.removeBlock(from));
    }


    public void replaceBlock(Block from, Block to) {
        add(StateMapping.replaceBlock(from, to));
    }

    public void replaceBlock(Tag<Block> from, Block to) {
        add(StateMapping.replaceBlock(from, to));
    }

    public <T extends Comparable<T>> void replaceProperty(Block block, Property<T> property, T from, T to) {
        add(StateMapping.replaceProperty(block, property, from, to));
    }

    public <T extends Comparable<T>> void setProperty(Block block, Property<T> property, T to) {
        add(StateMapping.setProperty(block, property, to));
    }

    @Override
    public boolean canConvert(@Nullable BlockState state) {
        return state != null && stream().anyMatch(i -> i.test(state));
    }

    @Override
    @Nonnull
    public BlockState getConverted(World world, @Nonnull BlockState state) {
        for (StateMapping i : this) {
            if (i.test(state)) {
                return i.apply(world, state);
            }
        }

        return state;
    }
}
