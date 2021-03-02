package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.Suppressable;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.PlayerDimensions;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundTag;

public class DisguiseSpell extends AbstractSpell implements Attached, Suppressable, FlightType.Provider, PlayerDimensions.Provider {

    private final Disguise disguise = new Disguise();

    private int suppressionCounter;

    protected DisguiseSpell(SpellType<?> type, Affinity affinity) {
        super(type, affinity);
    }

    @Override
    public boolean isVulnerable(Caster<?> otherSource, Spell other) {
        return suppressionCounter <= otherSource.getLevel().get();
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        caster.getEntity().calculateDimensions();
    }

    @Override
    public void onSuppressed(Caster<?> otherSource) {
        suppressionCounter = 100;
        setDirty(true);
    }

    @Override
    public boolean isSuppressed() {
        return suppressionCounter > 0;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public DisguiseSpell setDisguise(@Nullable Entity entity) {
        if (entity == disguise.getAppearance()) {
            entity = null;
        }

        disguise.setAppearance(entity);
        setDirty(true);
        return this;
    }

    @Override
    public boolean handleProjectileImpact(ProjectileEntity projectile) {
        return disguise.getAppearance() == projectile;
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {
        return update(source, true);
    }

    @SuppressWarnings("unchecked")
    public boolean update(Caster<?> source, boolean tick) {
        if (source.isClient()) {
            if (isSuppressed()) {
                source.spawnParticles(MagicParticleEffect.UNICORN, 5);
                source.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
            } else if (source.getWorld().random.nextInt(30) == 0) {
                source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
            }
        }

        LivingEntity owner = source.getMaster();

        Entity entity = disguise.getAppearance();

        if (isSuppressed()) {
            suppressionCounter--;

            owner.setInvisible(false);
            if (source instanceof Pony) {
                ((Pony)source).setInvisible(false);
            }

            if (entity != null) {
                entity.setInvisible(true);
                entity.setPos(entity.getX(), Integer.MIN_VALUE, entity.getY());
            }

            return true;
        }

        entity = disguise.getOrCreate(source);

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

        if (tick && !disguise.skipsUpdate()) {
            entity.tick();
        }

        behaviour.update(source, entity, this);

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            source.getMaster().setInvisible(true);
            player.setInvisible(true);

            if (entity instanceof Owned) {
                ((Owned<LivingEntity>)entity).setMaster(player.getMaster());
            }

            if (entity instanceof PlayerEntity) {
                entity.getDataTracker().set(PlayerAccess.getModelBitFlag(), owner.getDataTracker().get(PlayerAccess.getModelBitFlag()));
            }
        }

        return !source.getMaster().isDead();
    }

    @Override
    public void setDead() {
        super.setDead();
        disguise.remove();
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        compound.putInt("suppressionCounter", suppressionCounter);
        disguise.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        suppressionCounter = compound.getInt("suppressionCounter");
        disguise.fromNBT(compound);
    }

    @Override
    public FlightType getFlightType(Pony player) {
        if (isSuppressed() || !disguise.isPresent()) {
            return player.getSpecies().getFlightType();
        }
        return disguise.getFlightType();
    }

    @Override
    public float getTargetEyeHeight(Pony player) {
        return isSuppressed() ? -1 : disguise.getStandingEyeHeight();
    }

    @Override
    public Optional<EntityDimensions> getTargetDimensions(Pony player) {
        return isSuppressed() ? Optional.empty() : disguise.getDimensions();
    }

    static abstract class PlayerAccess extends PlayerEntity {
        public PlayerAccess() { super(null, null, 0, null); }
        static TrackedData<Byte> getModelBitFlag() {
            return PLAYER_MODEL_PARTS;
        }
    }
}
