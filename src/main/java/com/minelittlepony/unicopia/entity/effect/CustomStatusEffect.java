package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CustomStatusEffect extends StatusEffect {
    private final boolean instant;
    private final int rate;
    private final Direct direct;
    private final Indirect indirect;

    public CustomStatusEffect(StatusEffectType type, int color, int rate, boolean instant, Direct direct, Indirect indirect) {
        super(type, color);
        this.direct = direct;
        this.rate = rate;
        this.indirect = indirect;
        this.instant = instant;
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
        return instant;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        if (isInstant()) {
            return duration > 0;
        }

        int i = rate >> amplifier;
        return i <= 0 || duration % i == 0;
    }

    public static class Builder {
        private final Identifier id;
        private final StatusEffectType type;
        private final int color;

        private boolean instant;
        private int rate = 40;

        @NotNull
        private Direct direct = Direct.NONE;

        @NotNull
        private Indirect indirect = Indirect.NONE;

        public Builder(Identifier id, StatusEffectType type, int color) {
            this.id = id;
            this.type = type;
            this.color = color;
        }

        public Builder instant() {
            instant = true;
            return this;
        }

        public Builder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public Builder direct(@NotNull Direct applicator) {
            this.direct = applicator;
            return this;
        }

        public Builder indirect(@NotNull Indirect applicator) {
            this.indirect = applicator;
            return this;
        }

        public StatusEffect build() {
            return Registry.register(Registry.STATUS_EFFECT, id, new CustomStatusEffect(type, color, rate, instant, direct, indirect));
        }
    }

    @FunctionalInterface
    public interface Direct {
        Direct NONE = (p, e, i) -> {};

        void perform(StatusEffect effect, LivingEntity target, int amplifier);
    }

    @FunctionalInterface
    public interface Indirect {
        Indirect NONE = (p, s, a, t, A, d) -> {};

        void perform(StatusEffect effect, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double distance);
    }
}
