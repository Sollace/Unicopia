package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class SunBlindnessStatusEffect extends SimpleStatusEffect {
    public static final int MAX_DURATION = 250;

    SunBlindnessStatusEffect(int color) {
        super(StatusEffectCategory.NEUTRAL, color, false);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance state = entity.getStatusEffect(getEntry(entity));

        if (state == null || isSunImmune(entity)) {
            return false;
        }

        if (entity.age % 15 == 0) {
            if (!hasSunExposure(entity)) {
                entity.setStatusEffect(new StatusEffectInstance(getEntry(entity), (int)(state.getDuration() * 0.8F), Math.max(1, amplifier - 1), true, false), entity);
            } else {
                entity.damage(Living.living(entity).damageOf(amplifier == 2 ? UDamageTypes.SUN : UDamageTypes.SUNLIGHT), amplifier / 5F);
            }
        }

        return true;
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

        if (entity.isSubmergedInWater() || TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE, i -> i.isIn(UTags.Items.SHADES)).findAny().isPresent()) {
            return false;
        }

        return MeteorlogicalUtil.isPositionExposedToSun(entity.getWorld(), entity.getBlockPos());
    }
}
