package com.minelittlepony.unicopia.world.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BagOfHoldingContainer extends ScreenHandler {
    private final BagOfHoldingInventory inventory;

    private ItemStack sourceStack;

    public BagOfHoldingContainer(int sync, PlayerInventory inv) {
        super(UContainers.BAG_OF_HOLDING, sync);

        PlayerEntity player = inv.player;
        sourceStack = player.getStackInHand(Hand.MAIN_HAND);
        inventory = BagOfHoldingInventory.getInventoryFromStack(sourceStack);

        inventory.onOpen(player);
        close(player);

        final int LEFT_MARGIN = 8;
        final int TOP_MARGIN = 18;

        final int containerRows = (int)Math.ceil(inventory.size() / 9);
        final int inventoryRows = (int)Math.ceil((player.inventory.size() - 9) / 9);

        for (int i = 0; i < inventory.size(); i++) {
            int slotX = (i % 9) * 18;
            int slotY = (int)Math.floor(i / 9) * 18;

            addSlot(new SlotOfHolding(inventory, i, LEFT_MARGIN + slotX, TOP_MARGIN + slotY));
        }

        int inventoryY = (containerRows * 18) + 8;
        int hotbarY = inventoryY + TOP_MARGIN + (inventoryRows * 18) + 4;

        for (int i = 9; i < player.inventory.size() - 5; i++) {
            int slotX = (i % 9) * 18;
            int slotY = (int)Math.floor(i / 9) * 18;

            addSlot(new Slot(player.inventory, i, LEFT_MARGIN + slotX, inventoryY + slotY));
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(player.inventory, i, LEFT_MARGIN + i * 18, hotbarY));
        }
    }

    @Override
    public void close(PlayerEntity player) {
        inventory.writeTostack(sourceStack);
        inventory.onClose(player);

        super.close(player);
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerIn, int index) {
        ItemStack resultingStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            resultingStack = originalStack.copy();

            if (index < inventory.size()) {
                if (!insertItem(originalStack, inventory.size(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(originalStack, 0, inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        sendContentUpdates();

        return resultingStack;
    }

    class SlotOfHolding extends Slot {
        public SlotOfHolding(Inventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }
    }
}
