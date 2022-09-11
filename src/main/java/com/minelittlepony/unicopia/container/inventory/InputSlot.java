package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.container.*;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class InputSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;
    private final int ring;

    public InputSlot(SpellbookScreenHandler handler, Inventory inventory, int index, int[] params) {
        super(inventory, index, params[0], params[1]);
        this.handler = handler;
        this.ring = params[2];
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public int getRing() {
        return ring;
    }

    @Override
    public boolean isEnabled() {
       return handler.canShowSlots.test(SlotType.CRAFTING) && !handler.outputSlot.isEnabled();
    }
}