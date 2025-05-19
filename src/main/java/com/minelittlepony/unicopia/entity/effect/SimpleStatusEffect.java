package com.minelittlepony.unicopia.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class SimpleStatusEffect extends StatusEffect {

    private final boolean instant;

    public SimpleStatusEffect(StatusEffectCategory category, int color, boolean instant) {
        super(category, color);
        this.instant = instant;
    }

    protected RegistryEntry<StatusEffect> getEntry(Entity entity) {
        return entity.getRegistryManager().get(RegistryKeys.STATUS_EFFECT).getEntry(this);
    }

    @Override
    public final boolean isInstant() {
        return instant;
    }
}
