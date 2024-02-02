package com.minelittlepony.unicopia.block.state;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

public interface StateUtil {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static BlockState copyState(BlockState from, @Nullable BlockState to) {
        if (to == null) {
            return to;
        }
        for (var property : from.getProperties()) {
            to = to.withIfExists((Property)property, from.get(property));
        }
        return to;
    }
}
