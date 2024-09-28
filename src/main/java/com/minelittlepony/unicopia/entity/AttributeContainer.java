package com.minelittlepony.unicopia.entity;

import java.util.Map;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public interface AttributeContainer {
    @Nullable
    EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute);

    default void updateAttributeModifier(Identifier id, RegistryEntry<EntityAttribute> attribute, float desiredValue, Float2ObjectFunction<EntityAttributeModifier> modifierSupplier, boolean permanent) {
        @Nullable
        EntityAttributeInstance instance = getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }

        @Nullable
        EntityAttributeModifier modifier = instance.getModifier(id);

        if (!MathHelper.approximatelyEquals(desiredValue, modifier == null ? 0 : modifier.value())) {
            instance.removeModifier(id);

            if (desiredValue != 0) {
                if (permanent) {
                    instance.addPersistentModifier(modifierSupplier.get(desiredValue));
                } else {
                    instance.addTemporaryModifier(modifierSupplier.get(desiredValue));
                }
            }
        }
    }

    default void applyAttributeModifiers(Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers, boolean permanent, boolean apply) {
        modifiers.forEach((attribute, modifier) -> {
            applyAttributeModifier(attribute, modifier, permanent, apply);
        });
    }

    default void applyAttributeModifier(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, boolean permanent, boolean apply) {
        @Nullable
        EntityAttributeInstance instance = getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }

        @Nullable
        boolean present = instance.hasModifier(modifier.id());

        if (present != apply) {
            if (apply) {
                if (permanent) {
                    instance.addPersistentModifier(modifier);
                } else {
                    instance.addTemporaryModifier(modifier);
                }
            } else {
                instance.removeModifier(modifier.id());
            }
        }
    }
}
