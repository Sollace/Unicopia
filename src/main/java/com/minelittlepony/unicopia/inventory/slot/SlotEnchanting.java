package com.minelittlepony.unicopia.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotEnchanting extends Slot {
	
	public SlotEnchanting(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	public boolean canBeHovered() {
		return true;
	}
}
