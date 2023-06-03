package com.minelittlepony.unicopia.entity.behaviour;

import java.util.*;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
import com.minelittlepony.unicopia.util.Swap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

public interface EntitySwap {
    Swap<Entity> POSITION = Swap.of(Entity::getPos, (entity, pos) -> {
        entity.teleport(pos.getX(), pos.getY(), pos.getZ());
        if (entity instanceof PathAwareEntity pae && !(entity instanceof PlayerEntity)) {
            pae.getNavigation().stop();
        }
    });
    Swap<Entity> VELOCITY = Swap.of(Entity::getVelocity, (entity, vel) -> {
        entity.setVelocity(vel);
        Living.updateVelocity(entity);
    });
    Swap<Entity> PITCH = Swap.of(Entity::getPitch, Entity::setPitch);
    Swap<Entity> YAW = Swap.of(Entity::getYaw, Entity::setYaw);
    Swap<Entity> HEAD_YAW = Swap.of(Entity::getHeadYaw, (entity, headYaw) -> {
        entity.setHeadYaw(headYaw);
        if (entity.getWorld() instanceof ServerWorld sw) {
            sw.getChunkManager().sendToNearbyPlayers(entity, new EntitySetHeadYawS2CPacket(entity, (byte)MathHelper.floor(entity.getHeadYaw() * 256F / 360F)));
        }
    });
    Swap<Entity> BODY_YAW = Swap.of(Entity::getBodyYaw, Entity::setBodyYaw);
    Swap<Entity> FIRE_TICKS = Swap.of(Entity::getFireTicks, Entity::setFireTicks);
    Swap<Entity> PASSENGERS = Swap.of(entity -> new ArrayList<>(entity.getPassengerList()), (entity, passengers) -> {
       entity.removeAllPassengers();
       passengers.forEach(passenger -> passenger.startRiding(entity));
       Living.transmitPassengers(entity);
    });
    Swap<LivingEntity> STATUS_EFFECTS = Swap.of(
            entity -> Set.copyOf(entity.getActiveStatusEffects().values()),
            (entity, effects) -> {
            entity.clearStatusEffects();
            effects.forEach(entity::addStatusEffect);
        }
    );
    Swap<LivingEntity> MAX_HEALTH = Swap.of(LivingEntity::getMaxHealth, (e, newMax) -> {
        float oldHealthPercentage = e.getHealth() / e.getMaxHealth();
        e.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).removeModifier(PlayerAttributes.HEALTH_SWAPPING_MODIFIER_ID);

        float change = newMax - e.getMaxHealth();
        if (!MathHelper.approximatelyEquals(change, 0)) {
            e.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(PlayerAttributes.healthChange(change));
        }
        e.setHealth(oldHealthPercentage * newMax);
    });
    Swap<LivingEntity> HEALTH = Swap.of(LivingEntity::getHealth, LivingEntity::getMaxHealth, LivingEntity::setHealth, Number::floatValue);
    Swap<LivingEntity> AIR = Swap.of(LivingEntity::getAir, LivingEntity::getMaxAir, LivingEntity::setAir, Number::intValue);

    List<Swap<Entity>> REGISTRY = new ArrayList<>(List.of(
            Swap.union(POSITION, VELOCITY, PITCH, YAW, HEAD_YAW, BODY_YAW, FIRE_TICKS, PASSENGERS),
            Swap.union(STATUS_EFFECTS, MAX_HEALTH, HEALTH, AIR).upcast(e -> e instanceof LivingEntity)
    ));
    Swap<Entity> ALL = Swap.union(REGISTRY);
}
