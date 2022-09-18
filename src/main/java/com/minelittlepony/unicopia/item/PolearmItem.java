package com.minelittlepony.unicopia.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.UEntityAttributes;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.*;

public class PolearmItem extends SwordItem {
    protected static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.fromString("A7B3659C-AA74-469C-963A-09A391DCAA0F");

    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    private final int attackRange;

    public PolearmItem(ToolMaterial material, int damage, float speed, int range, Settings settings) {
        super(material, damage, speed, settings);
        this.attackRange = range;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(EquipmentSlot.MAINHAND));
        builder.put(UEntityAttributes.EXTENDED_REACH_DISTANCE, new EntityAttributeModifier(ATTACK_RANGE_MODIFIER_ID, "Weapon modifier", attackRange, EntityAttributeModifier.Operation.ADDITION));
        attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot);
    }
}
