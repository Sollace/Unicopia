package com.minelittlepony.unicopia.projectile;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellInventory.Operation;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.mob.UEntities;
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
    private static final TrackedData<Integer> LEVEL = DataTracker.registerData(MagicBeamEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_LEVEL = DataTracker.registerData(MagicBeamEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CORRUPTION = DataTracker.registerData(MagicBeamEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_CORRUPTION = DataTracker.registerData(MagicBeamEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final SpellInventory spells = SpellSlots.ofSingle(this);
    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this);

    private final LevelStore level = Levelled.of(
            () -> dataTracker.get(LEVEL),
            l -> dataTracker.set(LEVEL, l),
            () -> dataTracker.get(MAX_LEVEL)
    );
    private final LevelStore corruption = Levelled.of(
            () -> dataTracker.get(CORRUPTION),
            l -> dataTracker.set(CORRUPTION, l),
            () -> dataTracker.get(MAX_CORRUPTION)
    );

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
        dataTracker.startTracking(HYDROPHOBIC, false);
        dataTracker.startTracking(LEVEL, 0);
        dataTracker.startTracking(CORRUPTION, 0);
        dataTracker.startTracking(MAX_LEVEL, 1);
        dataTracker.startTracking(MAX_CORRUPTION, 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (getOwner() != null) {
            spells.tick(Situation.PROJECTILE);
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
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        Caster.of(entity).ifPresent(caster -> {
            dataTracker.set(LEVEL, caster.getLevel().get());
            dataTracker.set(MAX_LEVEL, caster.getLevel().getMax());
            dataTracker.set(CORRUPTION, caster.getCorruption().get());
            dataTracker.set(MAX_CORRUPTION, caster.getCorruption().getMax());
        });
    }

    @Override
    public LevelStore getLevel() {
        return level;
    }

    @Override
    public LevelStore getCorruption() {
        return corruption;
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
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
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
        spells.tick(spell -> {
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
        spells.getSlots().fromNBT(compound);
        var level = Levelled.fromNbt(compound.getCompound("level"));
        dataTracker.set(MAX_LEVEL, level.getMax());
        dataTracker.set(LEVEL, level.get());
        var corruption = Levelled.fromNbt(compound.getCompound("corruption"));
        dataTracker.set(MAX_CORRUPTION, corruption.getMax());
        dataTracker.set(CORRUPTION, corruption.get());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.put("level", level.toNbt());
        compound.put("corruption", corruption.toNbt());
        compound.putBoolean("hydrophobic", getHydrophobic());
        physics.toNBT(compound);
        spells.getSlots().toNBT(compound);
    }
}
