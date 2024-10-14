package com.minelittlepony.unicopia.util;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;

public interface Dispensable {
    default DispenserBehavior createDispenserBehaviour() {
        return new ItemDispenserBehavior() {
            private ActionResult result;
            @Override
            protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
                result = dispenseStack(source, stack);

                if (!result.isAccepted()) {
                    return super.dispenseSilently(source, stack);
                }

                return result instanceof ActionResult.Success success ? success.getNewHandStack() : stack.split(1);
            }

            @Override
            protected void playSound(BlockPointer pointer) {
                if (!result.isAccepted()) {
                    super.playSound(pointer);
                }
            }

            @Override
            protected void spawnParticles(BlockPointer pointer, Direction side) {
                if (!result.isAccepted()) {
                    super.spawnParticles(pointer, side);
                }
            }
        };
    }

    /**
     * Called to dispense this stack.
     */
    ActionResult dispenseStack(BlockPointer source, ItemStack stack);
}
