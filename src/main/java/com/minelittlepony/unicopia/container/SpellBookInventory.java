package com.minelittlepony.unicopia.container;

import net.minecraft.container.Container;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;

public class SpellBookInventory extends CraftingInventory {

    private final Inventory craftResult;

    public SpellBookInventory(Inventory resultMatrix, Container eventHandler, int width, int height) {
        super(eventHandler, width, height);
        craftResult = resultMatrix;
    }

    public Inventory getCraftResultMatrix() {
        return craftResult;
    }
}
