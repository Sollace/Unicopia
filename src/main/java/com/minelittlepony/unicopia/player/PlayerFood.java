package com.minelittlepony.unicopia.player;

import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

class PlayerFood implements IFood {

    private final IPlayer player;

    private ItemStack eatingStack = ItemStack.EMPTY;

    public PlayerFood(IPlayer player) {
        this.player = player;
    }

    @Override
    public void begin(ItemStack stack) {
        eatingStack = ItemStack.EMPTY;

        if (!stack.isEmpty()) {
            EnumAction action = stack.getItemUseAction();

            if (action == EnumAction.EAT && stack.getItem() instanceof ItemFood) {
                eatingStack = stack.copy();
            }
        }
    }

    @Override
    public void end() {
        eatingStack = ItemStack.EMPTY;
    }

    @Override
    public void finish() {
        if (!eatingStack.isEmpty()) {
            player.onEat(eatingStack, (ItemFood)eatingStack.getItem());
        }
    }

}
