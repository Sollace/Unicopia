package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.TransientComponentMap;

import net.minecraft.component.ComponentHolder;
import net.minecraft.item.ItemStack;

public interface ItemStackDuck extends ComponentHolder, TransientComponentMap.Holder {
    default ItemDuck getItemDuck() {
        return (ItemDuck)((ItemStack)(Object)this).getItem();
    }

    static ItemStackDuck of(ItemStack stack) {
        return (ItemStackDuck)(Object)stack;
    }

    static boolean isItemStack(Object o) {
        return o instanceof ItemStackDuck;
    }
}
