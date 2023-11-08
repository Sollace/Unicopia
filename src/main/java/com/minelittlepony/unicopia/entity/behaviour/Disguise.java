package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.player.PlayerDimensions;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface Disguise extends FlightType.Provider, PlayerDimensions.Provider {

    EntityAppearance getDisguise();

    void setDirty();

    boolean isDead();

    default Optional<EntityAppearance> getAppearance() {
        return Optional.ofNullable(getDisguise());
    }

    @Override
    default FlightType getFlightType() {
        return getAppearance().map(EntityAppearance::getFlightType).orElse(FlightType.UNSET);
    }

    @Override
    default Optional<Float> getTargetEyeHeight(Pony player) {
        return getAppearance().flatMap(d -> d.getTargetEyeHeight(player));
    }

    @Override
    default Optional<EntityDimensions> getTargetDimensions(Pony player) {
        return getAppearance().flatMap(d -> d.getTargetDimensions(player));
    }

    default boolean isOf(@Nullable Entity entity) {
        return getDisguise().isOf(entity);
    }

    default Disguise setDisguise(@Nullable Entity entity) {
        if (entity == getDisguise().getAppearance()) {
            entity = null;
        }

        getDisguise().setAppearance(entity);
        setDirty();
        return this;
    }

    @SuppressWarnings("unchecked")
    default boolean update(Caster<?> caster, boolean tick) {

        if (!(caster instanceof Living)) {
            return false;
        }

        Living<?> source = (Living<?>)caster;
        LivingEntity owner = source.asEntity();

        if (owner == null) {
            return true;
        }

        Entity entity = getDisguise().getOrCreate(source);

        if (entity == null) {
            owner.setInvisible(false);
            if (source instanceof Pony) {
                ((Pony) source).setInvisible(false);
            }

            owner.calculateDimensions();
            return false;
        }

        entity.noClip = true;

        if (entity instanceof MobEntity) {
            ((MobEntity)entity).setAiDisabled(true);
        }

        entity.setInvisible(false);
        entity.setNoGravity(true);

        EntityBehaviour<Entity> behaviour = EntityBehaviour.forEntity(entity);

        behaviour.copyBaseAttributes(owner, entity);

        if (tick && !getDisguise().skipsUpdate()) {
            entity.tick();
        }

        if (!(owner instanceof PlayerEntity) && !((LivingEntityDuck)owner).isJumping()) {
            owner.addVelocity(0, -0.09, 0);
        }

        behaviour.update(source, entity, this);

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            source.asEntity().setInvisible(true);
            player.setInvisible(true);

            if (entity instanceof Owned.Mutable) {
                ((Owned.Mutable<LivingEntity>)entity).setMaster(player);
            }

            if (entity instanceof PlayerEntity) {
                entity.getDataTracker().set(PlayerAccess.getModelBitFlag(), owner.getDataTracker().get(PlayerAccess.getModelBitFlag()));
            }
        }

        return !isDead() && !source.asEntity().isDead();
    }

    public static abstract class PlayerAccess extends PlayerEntity {
        public PlayerAccess() { super(null, null, 0, null); }
        public static TrackedData<Byte> getModelBitFlag() {
            return PLAYER_MODEL_PARTS;
        }
    }
}
