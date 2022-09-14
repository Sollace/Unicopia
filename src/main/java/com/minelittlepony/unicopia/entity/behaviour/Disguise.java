package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
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

    default Disguise setDisguise(@Nullable Entity entity) {
        if (entity == getDisguise().getAppearance()) {
            entity = null;
        }

        getDisguise().setAppearance(entity);
        setDirty();
        return this;
    }

    @SuppressWarnings("unchecked")
    default boolean update(Caster<?> source, boolean tick) {

        LivingEntity owner = source.getMaster();

        Entity entity = getDisguise().getOrCreate(source);

        if (owner == null) {
            return true;
        }

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

        behaviour.update(source, entity, this);

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            source.getMaster().setInvisible(true);
            player.setInvisible(true);

            if (entity instanceof Owned) {
                ((Owned<LivingEntity>)entity).setMaster(player);
            }

            if (entity instanceof PlayerEntity) {
                entity.getDataTracker().set(PlayerAccess.getModelBitFlag(), owner.getDataTracker().get(PlayerAccess.getModelBitFlag()));
            }
        }

        return !isDead() && !source.getMaster().isDead();
    }

    static abstract class PlayerAccess extends PlayerEntity {
        public PlayerAccess() { super(null, null, 0, null); }
        static TrackedData<Byte> getModelBitFlag() {
            return PLAYER_MODEL_PARTS;
        }
    }
}
