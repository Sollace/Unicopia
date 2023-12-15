package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.URecipes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class ShapingBenchScreenHandler extends StonecutterScreenHandler {

    private final ScreenHandlerContext context;
    private final World world;

    private ItemStack inputStack = ItemStack.EMPTY;

    public ShapingBenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(syncId, playerInventory, context);
        this.context = context;
        this.world = playerInventory.player.getWorld();
    }

    public ShapingBenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
        this.context = ScreenHandlerContext.EMPTY;
        this.world = playerInventory.player.getWorld();
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return UScreenHandlers.SHAPING_BENCH;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, UBlocks.SHAPING_BENCH);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack stack = slots.get(0).getStack();
        if (!stack.isOf(inputStack.getItem())) {
            inputStack = stack.copy();
            getAvailableRecipes().clear();
            setProperty(0, -1);
            slots.get(1).setStackNoCallbacks(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                getAvailableRecipes().addAll(world.getRecipeManager().getAllMatches(URecipes.CLOUD_SHAPING, input, world));
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot srcSlot = slots.get(slot);
        if (srcSlot != null && srcSlot.hasStack()) {
            ItemStack movingStack = srcSlot.getStack();
            Item item = movingStack.getItem();
            originalStack = movingStack.copy();
            if (slot == 1) {
                item.onCraft(movingStack, player.getWorld(), player);
                if (!insertItem(movingStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                srcSlot.onQuickTransfer(movingStack, originalStack);
            } else if (slot == 0
                    ? !insertItem(movingStack, 2, 38, false)
                    : (world.getRecipeManager().getFirstMatch(URecipes.CLOUD_SHAPING, new SimpleInventory(movingStack), world).isPresent()
                            ? !insertItem(movingStack, 0, 1, false)
                            : (slot >= 2 && slot < 29
                                ? !insertItem(movingStack, 29, 38, false)
                                : slot >= 29 && slot < 38 && !insertItem(movingStack, 2, 29, false)))) {
                return ItemStack.EMPTY;
            }
            if (movingStack.isEmpty()) {
                srcSlot.setStack(ItemStack.EMPTY);
            }
            srcSlot.markDirty();
            if (movingStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }
            srcSlot.onTakeItem(player, movingStack);
            this.sendContentUpdates();
        }
        return originalStack;
    }
}
