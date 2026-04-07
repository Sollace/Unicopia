package com.minelittlepony.unicopia.datagen.providers;

import net.minecraft.util.math.Direction;

import static net.minecraft.util.math.Direction.*;

import net.minecraft.util.math.AxisRotation;

public class BlockRotation {
    private static final AxisRotation[] ROTATIONS = AxisRotation.values();
    public static final Direction[] DIRECTIONS = { EAST, SOUTH, WEST, NORTH };

    public static AxisRotation cycle(AxisRotation rotation, int steps) {
        int index = rotation.ordinal() + steps;
        while (index < 0) {
            index += ROTATIONS.length;
        }
        return ROTATIONS[index % ROTATIONS.length];
    }

    public static AxisRotation next(AxisRotation rotation) {
        return cycle(rotation, 1);
    }

    public static AxisRotation previous(AxisRotation rotation) {
        return cycle(rotation, -1);
    }
}
