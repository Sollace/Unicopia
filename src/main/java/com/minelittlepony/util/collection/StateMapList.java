package com.minelittlepony.util.collection;

import java.util.ArrayList;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 * A collection of block-state mappings.
 *
 */
public class StateMapList extends ArrayList<IStateMapping> {
    private static final long serialVersionUID = 2602772651960588745L;

    public void removeBlock(Predicate<IBlockState> mapper) {
        add(IStateMapping.removeBlock(mapper));
    }

    public void replaceBlock(Block from, Block to) {
        add(IStateMapping.replaceBlock(from, to));
    }

    public <T extends Comparable<T>> void replaceProperty(Block block, IProperty<T> property, T from, T to) {
        add(IStateMapping.replaceProperty(block, property, from, to));
    }

    public <T extends Comparable<T>> void setProperty(Block block, IProperty<T> property, T to) {
        add(IStateMapping.setProperty(block, property, to));
    }

    /**
     * Checks if this collection contains a mapping capable of converting the given state.
     *
     * @param state        State to check
     *
     * @return    True if the state can be converted
     */
    public boolean canConvert(@Nullable IBlockState state) {
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
    public IBlockState getConverted(@Nonnull IBlockState state) {
        for (IStateMapping i : this) {
            if (i.test(state)) {
                return i.apply(state);
            }
        }

        return state;
    }
}
