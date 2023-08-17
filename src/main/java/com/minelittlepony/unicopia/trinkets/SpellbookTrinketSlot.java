package com.minelittlepony.unicopia.trinkets;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.inventory.InventorySlot;
import com.mojang.datafixers.util.Pair;

import dev.emi.trinkets.SurvivalTrinketSlot;
import dev.emi.trinkets.api.SlotGroup;
import dev.emi.trinkets.api.TrinketInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class SpellbookTrinketSlot extends InventorySlot implements TrinketsDelegate.SlotWithForeground {
    private final SurvivalTrinketSlot slot;

    public SpellbookTrinketSlot(SpellbookScreenHandler handler, TrinketInventory inventory, int index, int x, int y, SlotGroup group) {
        super(handler, inventory, index, x, y);
        this.slot = new SurvivalTrinketSlot(inventory, index, x, y, group, inventory.getSlotType(), 0, true);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.slot.onTakeItem(player, stack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.slot.canInsert(stack);
    }

    @Override
    public ItemStack getStack() {
        return this.slot.getStack();
    }

    @Override
    public boolean hasStack() {
        return this.slot.hasStack();
    }

    @Override
    public void setStack(ItemStack stack) {
        this.slot.setStack(stack);
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        this.slot.setStackNoCallbacks(stack);
    }

    @Override
    public void markDirty() {
        this.slot.markDirty();
    }

    @Override
    public int getMaxItemCount() {
        return this.slot.getMaxItemCount();
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return this.slot.getMaxItemCount(stack);
    }

    @Override
    @Nullable
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return null;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return this.slot.takeStack(amount);
    }

    @Override
    public boolean isEnabled() {
        return this.slot.isEnabled();
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return this.slot.canTakeItems(playerEntity);
    }

    @Override
    public Identifier getForegroundIdentifier() {
        return slot.getBackgroundIdentifier();
    }
}
