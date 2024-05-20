package com.minelittlepony.unicopia.projectile;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellContainer.Operation;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.network.datasync.EffectSync;
import com.minelittlepony.unicopia.network.track.Trackable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MagicBeamEntity extends MagicProjectileEntity implements Caster<MagicBeamEntity>, MagicImmune {
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(MagicBeamEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final EffectSync effectDelegate = new EffectSync(this, Trackable.of(this).getDataTrackers().getPrimaryTracker());
    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this, Trackable.of(this).getDataTrackers().getPrimaryTracker());

    public MagicBeamEntity(EntityType<MagicBeamEntity> type, World world) {
        super(type, world);
    }

    public MagicBeamEntity(World world, Entity owner, float divergance, Spell spell) {
        super(UEntities.MAGIC_BEAM, world);
        setPosition(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        setOwner(owner);
        setVelocity(owner, owner.getPitch(), owner.getYaw(), 0, 1.5F, divergance);
        setNoGravity(true);
        spell.apply(this);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(HYDROPHOBIC, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (getOwner() != null) {
            effectDelegate.tick(Situation.PROJECTILE);
        }


        if (getHydrophobic()) {
            if (StatePredicate.isFluid(getWorld().getBlockState(getBlockPos()))) {
                Vec3d vel = getVelocity();

                double velY = vel.y;

                velY *= -1;

                if (!hasNoGravity()) {
                    velY += 0.16;
                }

                setVelocity(new Vec3d(vel.x, velY, vel.z));
            }
        }
    }

    public void setHydrophobic() {
        getDataTracker().set(HYDROPHOBIC, true);
    }

    public boolean getHydrophobic() {
        return getDataTracker().get(HYDROPHOBIC);
    }

    @Override
    public MagicBeamEntity asEntity() {
        return this;
    }

    @Override
    public LevelStore getLevel() {
        return getMasterReference().getTarget().map(target -> target.level()).orElse(Levelled.EMPTY);
    }

    @Override
    public LevelStore getCorruption() {
        return getMasterReference().getTarget().map(target -> target.corruption()).orElse(Levelled.EMPTY);
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Affinity getAffinity() {
        return getSpellSlot().get().map(Affine::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public SpellContainer getSpellSlot() {
        return effectDelegate;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        return Caster.of(getMaster()).filter(c -> c.subtractEnergyCost(amount)).isPresent();
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        InteractionManager.getInstance().playLoopingSound(this, InteractionManager.SOUND_MAGIC_BEAM, getId());
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getSpellSlot().clear();
    }

    @Override
    protected <T extends ProjectileDelegate> void forEachDelegates(Consumer<T> consumer, Function<Object, T> predicate) {
        effectDelegate.tick(spell -> {
            Optional.ofNullable(predicate.apply(spell)).ifPresent(consumer);
            return Operation.SKIP;
        });
        super.forEachDelegates(consumer, predicate);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        getDataTracker().set(HYDROPHOBIC, compound.getBoolean("hydrophobic"));
        physics.fromNBT(compound);
        if (compound.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("hydrophobic", getHydrophobic());
        physics.toNBT(compound);
        getSpellSlot().get().ifPresent(effect -> {
            compound.put("effect", Spell.writeNbt(effect));
        });
    }
}
