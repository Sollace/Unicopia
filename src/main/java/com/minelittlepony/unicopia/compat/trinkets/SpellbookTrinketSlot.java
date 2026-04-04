package com.minelittlepony.unicopia.compat.trinkets;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.inventory.InventorySlot;
import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class SpellbookTrinketSlot extends InventorySlot {
    private final AccessoriesBasedSlot slot;

    SpellbookTrinketSlot(SpellbookScreenHandler handler, AccessoriesBasedSlot accessoriesSlot) {
        super(handler, accessoriesSlot.inventory, accessoriesSlot.id, accessoriesSlot.x, accessoriesSlot.y);
        slot = accessoriesSlot;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        slot.onTakeItem(player, stack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return slot.canInsert(stack);
    }

    @Override
    public ItemStack getStack() {
        return slot.getStack();
    }

    @Override
    public boolean hasStack() {
        return slot.hasStack();
    }

    @Override
    public void setStack(ItemStack stack) {
        slot.setStack(stack);
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        slot.setStackNoCallbacks(stack);
    }

    @Override
    public void markDirty() {
        slot.markDirty();
    }

    @Deprecated
    @Override
    public int getMaxItemCount() {
        return slot.getMaxItemCount();
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return slot.getMaxItemCount(stack);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return slot.takeStack(amount);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && slot.isEnabled();
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return slot.canTakeItems(playerEntity);
    }

    @Override
    public Identifier getBackgroundSprite() {
        return slot.getBackgroundSprite();
    }

    @Override
    public Identifier getForegroundIdentifier() {
        return slot.getBackgroundSprite();
    }

    @Override
    public boolean isTrinket() {
        return true;
    }
}
