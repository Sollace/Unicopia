package com.minelittlepony.unicopia.item.enchantment;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

// TODO: Replaced with EnchantmentEffectComponentTypes.ATTRIBUTES
@Deprecated
public class AttributedEnchantment extends SimpleEnchantment {

    private final Map<RegistryEntry<EntityAttribute>, ModifierFactory> modifiers = new HashMap<>();

    protected AttributedEnchantment(Options options) {
        super(options);
    }

    public AttributedEnchantment addModifier(RegistryEntry<EntityAttribute> attribute, ModifierFactory modifierSupplier) {
        modifiers.put(attribute, modifierSupplier);
        return this;
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (shouldChangeModifiers(user, level)) {
            modifiers.forEach((attr, modifierSupplier) -> {
                EntityAttributeInstance instance = user.asEntity().getAttributeInstance(attr);

                EntityAttributeModifier modifier = modifierSupplier.get(user, level);

                instance.removeModifier(modifier.id());
                instance.addPersistentModifier(modifier);
            });
        }
    }

    @Override
    public void onUnequipped(Living<?> user) {
        modifiers.forEach((attr, modifierSupplier) -> {
            EntityAttributeInstance instance = user.asEntity().getAttributeInstance(attr);

            instance.removeModifier(modifierSupplier.get(user, 1).getId());
        });
        user.getEnchants().remove(this);
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
