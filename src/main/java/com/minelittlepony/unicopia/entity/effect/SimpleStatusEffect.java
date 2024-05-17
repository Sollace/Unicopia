package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class SimpleStatusEffect extends StatusEffect {

    private final boolean instant;

    public SimpleStatusEffect(StatusEffectCategory category, int color, boolean instant) {
        super(category, color);
        this.instant = instant;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {

    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {

    }

    @Override
    public final boolean isInstant() {
        return instant;
    }
}
