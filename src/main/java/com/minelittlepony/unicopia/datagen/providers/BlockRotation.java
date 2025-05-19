package com.minelittlepony.unicopia.datagen.providers;

import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantSettings.Rotation;
import net.minecraft.util.math.Direction;

import static net.minecraft.util.math.Direction.*;

public class BlockRotation {
    private static final Rotation[] ROTATIONS = Rotation.values();
    public static final Direction[] DIRECTIONS = { EAST, SOUTH, WEST, NORTH };

    public static VariantSettings.Rotation cycle(Rotation rotation, int steps) {
        int index = rotation.ordinal() + steps;
        while (index < 0) {
            index += ROTATIONS.length;
        }
        return ROTATIONS[index % ROTATIONS.length];
    }

    public static Rotation next(Rotation rotation) {
        return cycle(rotation, 1);
    }

    public static Rotation previous(Rotation rotation) {
        return cycle(rotation, -1);
    }
}
