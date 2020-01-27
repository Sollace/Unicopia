package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.magic.spells.CastResult;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;

/**
 * Represents an object with an action to perform when dispensed from a dispenser.
 */
public interface IDispenceable extends IMagicEffect {

    /**
     * Called when dispensed.
     *
     * @param pos        Block position in front of the dispenser
     * @param facing    Direction of the dispenser
     * @param source    The dispenser currently dispensing
     * @param affinity  The affinity of the casting artifact
     * @return    an ActionResult for the type of action to perform.
     */
    CastResult onDispenced(BlockPos pos, Direction facing, Position source, Affinity affinity);
}
