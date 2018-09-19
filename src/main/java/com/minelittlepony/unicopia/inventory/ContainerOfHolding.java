package com.minelittlepony.unicopia.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;

public class ContainerOfHolding extends Container implements IWorldNameable {

    private final InventoryOfHolding inventory;

    private ItemStack sourceStack;

    public ContainerOfHolding(EntityPlayer player) {
        sourceStack = player.getHeldItem(EnumHand.MAIN_HAND);
        inventory = InventoryOfHolding.getInventoryFromStack(sourceStack);

        inventory.openInventory(player);
        this.onContainerClosed(player);

        final int LEFT_MARGIN = 8;
        final int TOP_MARGIN = 18;

        final int inventoryRows = (int)Math.ceil(inventory.getSizeInventory() / 9);

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int slotX = i % 9;
            int slotY = (int)Math.floor(i / 9);

            addSlotToContainer(new SlotOfHolding(inventory, i, LEFT_MARGIN + slotX * 18, TOP_MARGIN + slotY * 18));
        }

        int hotbarY = TOP_MARGIN + (inventoryRows * 18) + 4;

        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(player.inventory, i, LEFT_MARGIN + i * 18, hotbarY));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        inventory.writeTostack(sourceStack);
        inventory.closeInventory(player);


        super.onContainerClosed(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }


    @Override
    public String getName() {
        return getDisplayName().getUnformattedText();
    }

    @Override
    public boolean hasCustomName() {
        return inventory.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return inventory.getDisplayName();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack resultingStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            resultingStack = originalStack.copy();

            if (index < inventory.getSizeInventory()) {
                if (!mergeItemStack(originalStack, inventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(originalStack, 0, inventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return resultingStack;
    }

    class SlotOfHolding extends Slot {
        public SlotOfHolding(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }
    }
}
