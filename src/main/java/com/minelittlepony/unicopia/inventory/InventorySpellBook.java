package com.minelittlepony.unicopia.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;

public class InventorySpellBook extends InventoryCrafting {
	
	private final IInventory craftResult;
	
	public InventorySpellBook(IInventory resultMatrix, Container eventHandler, int width, int height) {
		super(eventHandler, width, height);
		craftResult = resultMatrix;
	}
	
	public IInventory getCraftResultMatrix() {
		return craftResult;
	}
}
