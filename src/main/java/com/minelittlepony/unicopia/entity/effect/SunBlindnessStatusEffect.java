package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class SunBlindnessStatusEffect extends StatusEffect {
    public static final int MAX_DURATION = 250;

    SunBlindnessStatusEffect(int color) {
        super(StatusEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance state = entity.getStatusEffect(this);

        if (state == null || isSunImmune(entity)) {
            return;
        }

        if (entity.age % 15 == 0) {
            if (!hasSunExposure(entity)) {
                entity.setStatusEffect(new StatusEffectInstance(this, (int)(state.getDuration() * 0.8F), Math.max(1, amplifier - 1), true, false), entity);
            } else {
                entity.damage(Living.living(entity).damageOf(amplifier == 2 ? UDamageTypes.SUN : UDamageTypes.SUNLIGHT), amplifier / 5F);
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

    public static boolean isSunImmune(LivingEntity entity) {
        return entity.hasStatusEffect(StatusEffects.BLINDNESS)
                || entity.hasPortalCooldown()
                || Pony.of(entity).filter(Pony::isSunImmune).isPresent();
    }

    public static boolean hasSunExposure(LivingEntity entity) {

        if (isSunImmune(entity)) {
            return false;
        }

        if (entity.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }

        if (EquinePredicates.PLAYER_BAT.test(entity) && entity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return true;
        }

        if (entity.getEquippedStack(EquipmentSlot.HEAD).isIn(UTags.Items.SHADES)
            || TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE).anyMatch(i -> i.isIn(UTags.Items.SHADES))
            || entity.isSubmergedInWater()) {
            return false;
        }

        return MeteorlogicalUtil.isPositionExposedToSun(entity.getWorld(), entity.getBlockPos());
    }
}
