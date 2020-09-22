package com.minelittlepony.unicopia.block.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;

public interface BlockStateConverter {
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
     * @param state        State to convert
     *
     * @return    The converted state if there is one, otherwise null
     */
    @Nonnull
    BlockState getConverted(@Nonnull BlockState state);
}
