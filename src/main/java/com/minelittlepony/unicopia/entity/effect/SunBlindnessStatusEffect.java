package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

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
                entity.damage(amplifier == 2 ? MagicalDamageSource.SUN : MagicalDamageSource.SUNLIGHT, amplifier / 5F);
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

        if (entity.getEquippedStack(EquipmentSlot.HEAD).isIn(UTags.SHADES)
            || TrinketsDelegate.getInstance().getEquipped(entity, TrinketsDelegate.FACE).anyMatch(i -> i.isIn(UTags.SHADES))
            || entity.isSubmergedInWater()) {
            return false;
        }

        return isPositionExposedToSun(entity.world, entity.getBlockPos());

    }

    public static boolean isPositionExposedToSun(World world, BlockPos pos) {
        if (world.isClient) {
            world.calculateAmbientDarkness();
        }

        return world.getDimension().hasSkyLight() && world.getLightLevel(LightType.SKY, pos) >= 12 && !world.isRaining() && !world.isThundering() && world.isDay();
    }
}
