package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.ItemImpl.ClingyItem;

import net.minecraft.item.*;

public interface ItemDuck extends ItemConvertible, TickableItem, ClingyItem {
    static ItemDuck of(ItemStack stack) {
        return (ItemDuck)stack.getItem();
    }
}
