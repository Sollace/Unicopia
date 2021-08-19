package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.UTags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.LightType;

public class SunBlindnessStatusEffect extends StatusEffect {
    public static final int MAX_DURATION = 50;

    SunBlindnessStatusEffect(int color) {
        super(StatusEffectType.NEUTRAL, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance state = entity.getStatusEffect(this);

        if (state == null) {
            return;
        }

        if (!hasSunExposure(entity)) {
            entity.setStatusEffect(new StatusEffectInstance(this, (int)(state.getDuration() * 0.8F), amplifier, true, false), entity);
        } else {
            if (entity.age % 15 == 0) {
                entity.damage(DamageSource.IN_FIRE, amplifier / 20F);
            }
        }
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyUpdateEffect(target, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration > 0;
    }

    public static boolean hasSunExposure(LivingEntity entity) {
        if (entity.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }

        if (EquinePredicates.PLAYER_BAT.test(entity) && entity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return true;
        }

        if (entity.getEquippedStack(EquipmentSlot.HEAD).isIn(UTags.SHADES)) {
            return false;
        }

        if (entity.world.isClient) {
            entity.world.calculateAmbientDarkness();
        }

        int light = entity.world.getLightLevel(LightType.SKY, entity.getBlockPos());

        return !(entity.isSubmergedInWater() || light < 12 || entity.world.isRaining() || entity.world.isThundering() || !entity.world.isDay());
    }
}
