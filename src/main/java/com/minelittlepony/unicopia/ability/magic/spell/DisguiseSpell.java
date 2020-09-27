package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.FlightPredicate;
import com.minelittlepony.unicopia.ability.HeightPredicate;
import com.minelittlepony.unicopia.ability.magic.AttachableSpell;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.Suppressable;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.entity.behaviour.VirtualEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundTag;

public class DisguiseSpell extends AbstractSpell implements AttachableSpell, Suppressable, FlightPredicate, HeightPredicate {

    private final VirtualEntity disguise = new VirtualEntity();

    private int suppressionCounter;

    @Override
    public String getName() {
        return "disguise";
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    public int getTint() {
        return 0x19E48E;
    }

    @Override
    public boolean isVulnerable(Caster<?> otherSource, Spell other) {
        return suppressionCounter <= otherSource.getCurrentLevel();
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

    public VirtualEntity getDisguise() {
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
    public boolean updateOnPerson(Caster<?> caster) {
        return update(caster);
    }

    @Override
    public boolean update(Caster<?> source) {
        return update(source, true);
    }

    @SuppressWarnings("unchecked")
    public boolean update(Caster<?> source, boolean tick) {
        LivingEntity owner = source.getOwner();

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
            if (source instanceof Pony) {
                owner.setInvisible(false);
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

            player.setInvisible(true);
            source.getOwner().setInvisible(true);

            if (entity instanceof Owned) {
                ((Owned<LivingEntity>)entity).setOwner(player.getOwner());
            }

            if (entity instanceof PlayerEntity) {
                entity.getDataTracker().set(PlayerAccess.getModelBitFlag(), owner.getDataTracker().get(PlayerAccess.getModelBitFlag()));
            }

            if (player.isClientPlayer() && InteractionManager.instance().getViewMode() == 0) {
                entity.setInvisible(true);
                entity.setPos(entity.getX(), Integer.MIN_VALUE, entity.getY());
            }
        }

        return !source.getOwner().isDead();
    }

    @Override
    public void setDead() {
        super.setDead();
        disguise.remove();
    }

    @Override
    public void render(Caster<?> source) {
        if (isSuppressed()) {
            source.spawnParticles(MagicParticleEffect.UNICORN, 5);
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
        } else if (source.getWorld().random.nextInt(30) == 0) {
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
        }
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
    public boolean checkCanFly(Pony player) {
        return disguise.canFly() && player.getSpecies().canFly();
    }

    @Override
    public float getTargetEyeHeight(Pony player) {
        return isSuppressed() ? -1 : disguise.getStandingEyeHeight();
    }

    @Override
    public float getTargetBodyHeight(Pony player) {
        return isSuppressed() ? -1 : disguise.getHeight();
    }

    static abstract class PlayerAccess extends PlayerEntity {
        public PlayerAccess() { super(null, null, 0, null); }
        static TrackedData<Byte> getModelBitFlag() {
            return PLAYER_MODEL_PARTS;
        }
    }
}
