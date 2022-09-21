package com.minelittlepony.unicopia.mixin.trinkets;

import org.spongepowered.asm.mixin.*;

import com.minelittlepony.unicopia.trinkets.TrinketsDelegateImpl;
import dev.emi.trinkets.*;
import dev.emi.trinkets.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Mixin(SurvivalTrinketSlot.class)
abstract class MixinTrinketSurvivalSlot extends Slot implements TrinketSlot {
    @Shadow(remap = false)
    private @Final int slotOffset;
    @Shadow(remap = false)
    private @Final TrinketInventory trinketInventory;

    MixinTrinketSurvivalSlot() { super(null, 0, 0, 0); }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return TrinketsDelegateImpl.getMaxCount(stack, new SlotReference(trinketInventory, slotOffset), super.getMaxItemCount(stack));
    }
}

@Mixin(CreativeTrinketSlot.class)
abstract class MixinTrinketCreativeSlot extends Slot implements TrinketSlot {
    @Shadow(remap = false)
    private @Final SurvivalTrinketSlot original;

    MixinTrinketCreativeSlot() { super(null, 0, 0, 0); }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return original.getMaxItemCount(stack);
    }
}
