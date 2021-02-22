package com.minelittlepony.unicopia.item.enchantment;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public class AttributedEnchantment extends SimpleEnchantment {

    private final Map<EntityAttribute, ModifierFactory> modifiers = new HashMap<>();

    protected AttributedEnchantment(Rarity rarity, EnchantmentTarget target, boolean cursed, int maxLevel, EquipmentSlot... slots) {
        super(rarity, target, cursed, maxLevel, slots);
    }

    protected AttributedEnchantment(Rarity rarity, boolean cursed, int maxLevel, EquipmentSlot... slots) {
        super(rarity, cursed, maxLevel, slots);
    }

    public AttributedEnchantment addModifier(EntityAttribute attribute, ModifierFactory modifierSupplier) {
        modifiers.put(attribute, modifierSupplier);
        return this;
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (shouldChangeModifiers(user, level)) {
            LivingEntity entity = user.getMaster();
            modifiers.forEach((attr, modifierSupplier) -> {
                EntityAttributeInstance instance = entity.getAttributeInstance(attr);

                EntityAttributeModifier modifier = modifierSupplier.get(user, level);

                instance.removeModifier(modifier.getId());
                instance.addPersistentModifier(modifier);
            });
            entity.calculateDimensions();
        }
    }

    @Override
    public void onUnequipped(Living<?> user) {
        LivingEntity entity = user.getMaster();
        modifiers.forEach((attr, modifierSupplier) -> {
            EntityAttributeInstance instance = entity.getAttributeInstance(attr);

            instance.tryRemoveModifier(modifierSupplier.get(user, 1).getId());
        });
        user.getEnchants().remove(this);
        AwaitTickQueue.scheduleTask(entity.world, w -> {
            entity.calculateDimensions();
       }, 1);
    }

    public void getModifiers(Living<?> user, int level, Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> output) {
        modifiers.forEach((attr, modifierSupplier) -> {
            EntityAttributeModifier modif = modifierSupplier.get(user, level);

            for (EquipmentSlot slot : getSlots()) {
                output.computeIfAbsent(slot, s -> HashMultimap.create()).put(attr, modif);
            }
        });
    }

    protected boolean shouldChangeModifiers(Living<?> user, int level) {
        return user.getEnchants().computeIfAbsent(this, Data::new).update(level);
    }

    interface ModifierFactory {
        EntityAttributeModifier get(Living<?> user, int level);
    }
}
