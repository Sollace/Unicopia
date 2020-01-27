package com.minelittlepony.unicopia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CustomStatusEffect extends StatusEffect {

    private int tickDelay = 40;

    @Nonnull
    private DirectEffect direct = DirectEffect.NONE;

    @Nonnull
    private IndirectEffect indirect = IndirectEffect.NONE;

    public CustomStatusEffect(Identifier id, StatusEffectType type, int color) {
        super(type, color);

        Registry.register(Registry.STATUS_EFFECT, id, this);
    }

    public CustomStatusEffect setSilent() {
        return this;
    }

    public CustomStatusEffect direct(@Nonnull DirectEffect applicator) {
        this.direct = applicator;

        return this;
    }

    public CustomStatusEffect indirect(@Nonnull IndirectEffect applicator) {
        this.indirect = applicator;

        return this;
    }


    public CustomStatusEffect setTickDelay(int delay) {
        tickDelay = delay;

        return this;
    }

    public CustomStatusEffect setEffectiveness(double effectiveness) {

        return this;
    }

    @Override
    public void applyUpdateEffect(LivingEntity target, int amplifier) {
        direct.perform(this, target, amplifier);
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double distance) {
        indirect.perform(this, source, attacker, target, amplifier, distance);
    }

    @Override
    public boolean isInstant() {
        return tickDelay > 0;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        if (!isInstant()) {
            int i = tickDelay >> amplifier;

            if (i > 0) {
                return duration % i == 0;
            }
        }

        return duration > 0;
    }

    @FunctionalInterface
    public interface DirectEffect {
        DirectEffect NONE = (p, e, i) -> {};

        void perform(StatusEffect effect, LivingEntity target, int amplifier);
    }

    @FunctionalInterface
    public interface IndirectEffect {
        IndirectEffect NONE = (p, s, a, t, A, d) -> {};

        void perform(StatusEffect effect, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double distance);
    }
}
