package com.minelittlepony.unicopia.inventory.gui;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.inventory.InventorySpellBook;
import com.minelittlepony.unicopia.inventory.slot.SlotEnchanting;
import com.minelittlepony.unicopia.inventory.slot.SlotEnchantingResult;
import com.minelittlepony.unicopia.item.ItemSpell;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerSpellBook extends Container {

    private final World worldObj;

    private IInventory craftResult = new InventoryBasic("Spell Result", false, 1);

    private InventorySpellBook craftMatrix = new InventorySpellBook(craftResult, this, 5, 1);

    private IPageUnlockListener listener;

    private SlotEnchantingResult resultSlot = null;

    private final EntityPlayer player;

    public ContainerSpellBook(InventoryPlayer inventory, World world, BlockPos pos) {
        super();
        worldObj = world;
        player = inventory.player;

        initCraftingSlots();

        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(inventory, i, 121 + i * 18, 195));
        }

        onCraftMatrixChanged(craftMatrix);
    }

    public void setListener(IPageUnlockListener listener) {
        this.listener = listener;

        if (resultSlot != null) {
            resultSlot.setListener(listener);
        }
    }

    public void initCraftingSlots() {
        addSlotToContainer(new SlotEnchanting(craftMatrix, 0, 175, 50));
        addSlotToContainer(new SlotEnchanting(craftMatrix, 1, 149, 94));
        addSlotToContainer(new SlotEnchanting(craftMatrix, 2, 175, 134));
        addSlotToContainer(new SlotEnchanting(craftMatrix, 3, 226, 120));
        addSlotToContainer(new SlotEnchanting(craftMatrix, 4, 227, 65));
        addSlotToContainer(resultSlot = new SlotEnchantingResult(listener, PlayerSpeciesList.instance().getPlayer(player), craftMatrix, craftResult, 0, 191, 92));
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        ItemStack current = craftResult.getStackInSlot(0);

        if (!current.isEmpty()) {
            ItemStack crafted = Unicopia.getCraftingManager().findMatchingResult(craftMatrix, worldObj);

            if (!crafted.isEmpty()) {
                current = SpellRegistry.instance().enchantStack(current, crafted);

                resultSlot.setCrafted(true);
                player.playSound(SoundEvents.BLOCK_NOTE_CHIME, 1, 1);
            } else {
                current = SpellRegistry.instance().disenchantStack(current);

                resultSlot.setCrafted(false);
                player.playSound(SoundEvents.BLOCK_NOTE_BASS, 1, 1);
            }

            craftResult.setInventorySlotContents(0, current);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (index > 5) {
                if (stack.getItem() instanceof ItemSpell) {
                    if (!mergeItemStack(slotStack, 5, 6, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 0, 5, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!mergeItemStack(slotStack, 6, 15, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(slotStack, stack);
                onCraftMatrixChanged(craftMatrix);
            }

            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            if (craftMatrix.getStackInSlot(i) != null) {
                player.dropItem(craftMatrix.getStackInSlot(i), false);
                craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }

        if (craftResult.getStackInSlot(0) != null) {
            player.dropItem(craftResult.getStackInSlot(0), false);
            craftResult.setInventorySlotContents(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return Predicates.MAGI.test(player);
    }
}
