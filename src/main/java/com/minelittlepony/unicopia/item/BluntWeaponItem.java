package com.minelittlepony.unicopia.item;

import java.util.UUID;

import com.google.common.collect.Multimap;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;

public class BluntWeaponItem extends Item {
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public static final UUID KNOCKBACK_MODIFIER_ID = UUID.fromString("7b16994b-1edb-4381-be62-94317f39ec8f");
    public static final UUID LUCK_MODIFIER_ID = UUID.fromString("7b16994b-1edb-8431-be62-7f39ec94318f");

    public BluntWeaponItem(Settings settings, Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        super(settings);
        this.modifiers = modifiers;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND) {
            return modifiers;
        }
        return super.getAttributeModifiers(slot);
    }
}
