package com.minelittlepony.unicopia.item.enchantment;

import net.minecraft.entity.EquipmentSlot;

public interface UEnchantmentValidSlots {
    EquipmentSlot[] ANY = EquipmentSlot.values();
    EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    EquipmentSlot[] HANDS = { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND };
}
