package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.WorldEvents;

public class CorruptInfluenceStatusEffect extends StatusEffect {
    CorruptInfluenceStatusEffect(int color) {
        super(StatusEffectCategory.NEUTRAL, color);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "6D706448-6A60-4F59-BE8A-C23A6DD2C7A9", 15, EntityAttributeModifier.Operation.ADDITION);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "6D706448-6A60-4F59-BE8A-C23A6DD2C7A9", 10, EntityAttributeModifier.Operation.ADDITION);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        if (entity.world.isClient) {
            return;
        }

        if (entity instanceof HostileEntity) {

            int nearby = 0;

            for (Entity i : entity.world.getOtherEntities(entity, entity.getBoundingBox().expand(40))) {
                if (i.getType() == entity.getType()) {
                    nearby++;
                }
            }

            if (nearby > 1) {
                if (Equine.of(entity).filter(eq -> eq instanceof Owned<?> o && o.hasMaster()).isPresent()) {
                    return;
                }

                if (entity.world.random.nextInt(2000) != 0) {
                    return;
                }
            } else if (entity.world.random.nextInt(200) != 0) {
                return;
            }

            HostileEntity mob = (HostileEntity)entity;

            HostileEntity clone = (HostileEntity)mob.getType().create(mob.world);
            clone.copyPositionAndRotation(entity);

            Equine.of(clone).ifPresent(eq -> {
                if (eq instanceof Owned) {
                    ((Owned<Entity>)eq).setMaster(mob);
                }
            });
            mob.world.spawnEntity(clone);

            if (!mob.isSilent()) {
                mob.world.syncWorldEvent((PlayerEntity)null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, mob.getBlockPos(), 0);
            }
        } else if (entity.age % 2000 == 0) {
            entity.damage(MagicalDamageSource.ALICORN_AMULET, 2);
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
}
