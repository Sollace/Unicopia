package com.minelittlepony.unicopia.magic;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Material;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;

public interface Dispensable {
    /**
     * Enables dispensing behaviours for this item.
     */
    static DispenserBehavior setDispenseable(Item item, Dispensable action) {
        ItemDispenserBehavior behaviour = new ItemDispenserBehavior() {
            private ActionResult result;
            @Override
            protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
                TypedActionResult<ItemStack> result = action.dispenseStack(source, stack);
                this.result = result.getResult();

                if (this.result != ActionResult.SUCCESS) {
                    return super.dispenseSilently(source, stack);
                }

                return result.getValue();
            }

            @Override
            protected void playSound(BlockPointer pointer) {
                if (result != ActionResult.PASS) {
                    super.playSound(pointer);
                }
            }

            @Override
            protected void spawnParticles(BlockPointer pointer, Direction side) {
                if (result != ActionResult.PASS) {
                    super.spawnParticles(pointer, side);
                }
            }
        };
        DispenserBlock.registerBehavior(item, behaviour);
        return behaviour;
    }

    /**
     * Called to dispense this stack.
     */
    TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack);

    static Optional<DispenserBehavior> getBehavior(ItemStack stack) {
        return Optional.ofNullable(DispenserAccess.INSTANCE.getBehaviorForItem(stack));
    }
}

final class DispenserAccess extends DispenserBlock {
    static final DispenserAccess INSTANCE = new DispenserAccess();
    private DispenserAccess() {
        super(Block.Settings.of(Material.AIR));
    }

    @Override
    public DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return super.getBehaviorForItem(stack);
    }
}