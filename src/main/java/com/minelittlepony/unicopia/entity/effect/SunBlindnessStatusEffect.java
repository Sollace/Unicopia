package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;

public class SunBlindnessStatusEffect extends StatusEffect {
    public static final int MAX_DURATION = 50;

    public static final SunBlindnessStatusEffect INSTANCE = new SunBlindnessStatusEffect(0x886F0F);

    private SunBlindnessStatusEffect(int color) {
        super(StatusEffectType.NEUTRAL, color);

        Registry.register(Registry.STATUS_EFFECT, new Identifier("unicopia", "sun_blindness"), this);
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
        if (entity.world.isClient) {
            entity.world.calculateAmbientDarkness();
        }

        int light = entity.world.getLightLevel(LightType.SKY, entity.getBlockPos());

        return !(entity.isSubmergedInWater() || light < 12 || entity.world.isRaining() || entity.world.isThundering() || !entity.world.isDay());
    }
}
