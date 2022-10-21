package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.container.*;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class InputSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;
    private final float weight;

    public InputSlot(SpellbookScreenHandler handler, Inventory inventory, int index, HexagonalCraftingGrid.Slot params) {
        super(inventory, index, params.left(), params.top());
        this.handler = handler;
        this.weight = params.weight();
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean isEnabled() {
       return handler.canShowSlots.test(SlotType.CRAFTING) && !handler.outputSlot.isEnabled();
    }
}