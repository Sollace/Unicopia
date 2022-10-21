package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.container.*;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class InventorySlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;

    public InventorySlot(SpellbookScreenHandler handler, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
    }

    @Override
    public boolean isEnabled() {
       return handler.canShowSlots.test(SlotType.INVENTORY);
    }
}