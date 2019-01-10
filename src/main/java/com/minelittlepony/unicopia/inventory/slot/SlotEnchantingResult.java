package com.minelittlepony.unicopia.inventory.slot;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.enchanting.PagesList;
import com.minelittlepony.unicopia.inventory.InventorySpellBook;
import com.minelittlepony.unicopia.item.ItemSpell;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class SlotEnchantingResult extends SlotEnchanting {

	private final EntityPlayer thePlayer;
	private final InventorySpellBook craftMatrix;

	private IPageUnlockListener listener;

	public SlotEnchantingResult(IPageUnlockListener listener, EntityPlayer player, InventorySpellBook craftMatric, IInventory inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
		thePlayer = player;
		this.listener = listener;
		craftMatrix = craftMatric;
	}

	public void setListener(IPageUnlockListener listener) {
		this.listener = listener;
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        onCrafting(stack);

        ItemStack current = craftMatrix.getCraftResultMatrix().getStackInSlot(0);
        craftMatrix.getCraftResultMatrix().setInventorySlotContents(0, stack);

        NonNullList<ItemStack> remaining = Unicopia.getCraftingManager().getRemainingItems(craftMatrix, player.world);

        craftMatrix.getCraftResultMatrix().setInventorySlotContents(0, current);

        for (int i = 0; i < remaining.size(); ++i) {
            current = craftMatrix.getStackInSlot(i);
            ItemStack remainder = remaining.get(i);

            if (!current.isEmpty()) {
                if (current.getCount() < stack.getCount()) {
                    craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
                } else {
                    craftMatrix.decrStackSize(i, stack.getCount());
                }

        		if (!remainder.isEmpty()) {
        		    if (craftMatrix.getStackInSlot(i).isEmpty()) {
                        craftMatrix.setInventorySlotContents(i, remainder);
                    } else {
                        remainder.setCount(stack.getCount());
                        if (!player.inventory.addItemStackToInventory(remainder)) {
                            player.dropItem(remainder, true);
                        }
                    }
        		}
            }
        }

        return super.onTake(player, stack);
    }

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		if (PagesList.unlockPage(thePlayer, stack) && listener != null) {
			listener.onPageUnlocked();
		}

		if (listener != null) {
			listener.onPageUnlocked();
		}
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemSpell && !SpellRegistry.stackHasEnchantment(stack);
    }

	@Override
	public String getSlotTexture() {
        return "unicopia:items/empty_slot_gem";
    }
}