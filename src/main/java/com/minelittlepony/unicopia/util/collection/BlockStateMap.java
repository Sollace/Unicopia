package com.minelittlepony.unicopia.util.collection;

import java.util.ArrayList;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

/**
 * A collection of block-state mappings.
 *
 */
public class BlockStateMap extends ArrayList<StateMapping> {
    private static final long serialVersionUID = 2602772651960588745L;

    public void removeBlock(Predicate<BlockState> mapper) {
        add(StateMapping.removeBlock(mapper));
    }

    public void replaceBlock(Block from, Block to) {
        add(StateMapping.replaceBlock(from, to));
    }

    public <T extends Comparable<T>> void replaceProperty(Block block, Property<T> property, T from, T to) {
        add(StateMapping.replaceProperty(block, property, from, to));
    }

    public <T extends Comparable<T>> void setProperty(Block block, Property<T> property, T to) {
        add(StateMapping.setProperty(block, property, to));
    }

    /**
     * Checks if this collection contains a mapping capable of converting the given state.
     *
     * @param state        State to check
     *
     * @return    True if the state can be converted
     */
    public boolean canConvert(@Nullable BlockState state) {
        return state != null && stream().anyMatch(i -> i.test(state));
    }

    /**
     * Attempts to convert the given state based on the known mappings in this collection.
     *
     * @param state        State to convert
     *
     * @return    The converted state if there is one, otherwise null
     */
    @Nonnull
    public BlockState getConverted(@Nonnull BlockState state) {
        for (StateMapping i : this) {
            if (i.test(state)) {
                return i.apply(state);
            }
        }

        return state;
    }
}
