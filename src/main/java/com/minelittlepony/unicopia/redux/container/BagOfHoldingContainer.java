package com.minelittlepony.unicopia.redux.container;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class BagOfHoldingContainer extends Container {
    private final BagOfHoldingInventory inventory;

    private ItemStack sourceStack;

    public BagOfHoldingContainer(int sync, Identifier id, PlayerEntity player, PacketByteBuf buf) {
        super(null, sync);

        sourceStack = player.getStackInHand(Hand.MAIN_HAND);
        inventory = BagOfHoldingInventory.getInventoryFromStack(sourceStack);

        inventory.onInvOpen(player);
        close(player);

        final int LEFT_MARGIN = 8;
        final int TOP_MARGIN = 18;

        final int containerRows = (int)Math.ceil(inventory.getInvSize() / 9);
        final int inventoryRows = (int)Math.ceil((player.inventory.getInvSize() - 9) / 9);

        for (int i = 0; i < inventory.getInvSize(); i++) {
            int slotX = (i % 9) * 18;
            int slotY = (int)Math.floor(i / 9) * 18;

            addSlot(new SlotOfHolding(inventory, i, LEFT_MARGIN + slotX, TOP_MARGIN + slotY));
        }

        int inventoryY = (containerRows * 18) + 8;
        int hotbarY = inventoryY + TOP_MARGIN + (inventoryRows * 18) + 4;

        for (int i = 9; i < player.inventory.getInvSize() - 5; i++) {
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
        inventory.onInvClose(player);

        super.close(player);
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerIn, int index) {
        ItemStack resultingStack = ItemStack.EMPTY;
        Slot slot = slotList.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            resultingStack = originalStack.copy();

            if (index < inventory.getInvSize()) {
                if (!insertItem(originalStack, inventory.getInvSize(), slotList.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(originalStack, 0, inventory.getInvSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return resultingStack;
    }

    class SlotOfHolding extends Slot {
        public SlotOfHolding(Inventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }
    }
}
