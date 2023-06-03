package com.minelittlepony.unicopia.projectile;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellContainer.Operation;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.datasync.EffectSync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A generalised version of Mojang's projectile entity class with added support for a custom appearance and water phobia.
 *
 * Can also carry a spell if needed.
 */
public class MagicProjectileEntity extends ThrownItemEntity implements Caster<MagicProjectileEntity>, Owned.Mutable<LivingEntity> {
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    public static final byte PROJECTILE_COLLISSION = 3;

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this, GRAVITY, false);

    private final EntityReference<Entity> homingTarget = new EntityReference<>();

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world) {
        super(type, world);
    }

    public MagicProjectileEntity(World world) {
        this(UEntities.THROWN_ITEM, world);
    }

    public MagicProjectileEntity(World world, LivingEntity thrower) {
        super(UEntities.THROWN_ITEM, thrower, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(GRAVITY, 1F);
        getDataTracker().startTracking(DAMAGE, 0F);
        getDataTracker().startTracking(EFFECT, new NbtCompound());
        getDataTracker().startTracking(HYDROPHOBIC, false);
    }

    @Override
    protected Item getDefaultItem() {
        switch (getSpellSlot().get(false).map(Spell::getAffinity).orElse(Affinity.NEUTRAL)) {
            case GOOD: return Items.SNOWBALL;
            case BAD: return Items.MAGMA_CREAM;
            default: return Items.AIR;
        }
    }

    @Override
    public MagicProjectileEntity asEntity() {
        return this;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        setOwner(owner);
    }

    @Override
    @Nullable
    public LivingEntity getMaster() {
        return (LivingEntity)getOwner();
    }

    public void setHomingTarget(@Nullable Entity target) {
        homingTarget.set(target);
    }

    @Override
    public LevelStore getLevel() {
        return Caster.of(getMaster()).map(Caster::getLevel).orElse(Levelled.EMPTY);
    }

    @Override
    public LevelStore getCorruption() {
        return Caster.of(getMaster()).map(Caster::getCorruption).orElse(Levelled.EMPTY);
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Affinity getAffinity() {
        return getSpellSlot().get(true).map(Affine::getAffinity).orElse(Affinity.NEUTRAL);
    }

    @Override
    public SpellContainer getSpellSlot() {
        return effectDelegate;
    }

    @Override
    public boolean subtractEnergyCost(double amount) {
        return Caster.of(getMaster()).filter(c -> c.subtractEnergyCost(amount)).isPresent();
    }

    public void addThrowDamage(float damage) {
        setThrowDamage(getThrowDamage() + damage);
    }

    public void setThrowDamage(float damage) {
        getDataTracker().set(DAMAGE, Math.max(0, damage));
    }

    public float getThrowDamage() {
        return getDataTracker().get(DAMAGE);
    }

    public void setHydrophobic() {
        getDataTracker().set(HYDROPHOBIC, true);
    }

    public boolean getHydrophobic() {
        return getDataTracker().get(HYDROPHOBIC);
    }

    @Override
    public void tick() {
        if (!getWorld().isClient() && !homingTarget.isPresent(getWorld())) {
            if (getVelocity().length() < 0.1 || age > 90) {
                discard();
            }
        }

        super.tick();

        if (getOwner() == null) {
            return;
        }

        getSpellSlot().get(true).filter(spell -> spell.tick(this, Situation.PROJECTILE));

        if (getHydrophobic()) {
            if (getWorld().getBlockState(getBlockPos()).isLiquid()) {
                Vec3d vel = getVelocity();

                double velY = vel.y;

                velY *= -1;

                if (!hasNoGravity()) {
                    velY += 0.16;
                }

                setVelocity(new Vec3d(vel.x, velY, vel.z));
            }
        }

        homingTarget.ifPresent(getWorld(), e -> {
            setNoGravity(true);
            noClip = true;
            setVelocity(getVelocity().add(e.getPos().subtract(getPos()).normalize().multiply(0.2)).multiply(0.6, 0.6, 0.6));
        });
    }

    private ParticleEffect getParticleParameters() {
       ItemStack stack = getItem();

       if (stack.isEmpty()) {
           return ParticleTypes.ITEM_SNOWBALL;
       }

       if (stack.getItem() == UItems.FILLED_JAR) {
           stack = UItems.EMPTY_JAR.getDefaultStack();
       }

       return new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
    }

    @Override
    public void handleStatus(byte id) {
       if (id == PROJECTILE_COLLISSION) {
          ParticleEffect effect = getParticleParameters();

          for(int i = 0; i < 8; i++) {
             getWorld().addParticle(effect, getX(), getY(), getZ(), 0, 0, 0);
          }
       } else {
           super.handleStatus(id);
       }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        physics.fromNBT(compound);
        homingTarget.fromNBT(compound.getCompound("homingTarget"));
        if (compound.contains("effect")) {
            getSpellSlot().put(Spell.readNbt(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        physics.toNBT(compound);
        compound.put("homingTarget", homingTarget.toNBT());
        getSpellSlot().get(true).ifPresent(effect -> {
            compound.put("effect", Spell.writeNbt(effect));
        });
    }

    @Override
    protected void onCollision(HitResult result) {
        if (!isRemoved()) {
            super.onCollision(result);

            if (!getWorld().isClient()) {
                getWorld().sendEntityStatus(this, PROJECTILE_COLLISSION);
                discard();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getSpellSlot().clear();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);

        forEachDelegates(effect -> effect.onImpact(this, hit), ProjectileDelegate.BlockHitListener.PREDICATE);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (entity instanceof ProjectileEntity) {
            return;
        }

        if (entity != null) {
            float damage = getThrowDamage();

            if (damage > 0) {
                entity.damage(getDamageSources().thrown(this, getOwner()), getThrowDamage());
            }

            forEachDelegates(effect -> effect.onImpact(this, hit), ProjectileDelegate.EntityHitListener.PREDICATE);
        }
    }

    protected <T extends ProjectileDelegate> void forEachDelegates(Consumer<T> consumer, Function<Object, T> predicate) {
        getSpellSlot().forEach(spell -> {
            Optional.ofNullable(predicate.apply(spell)).ifPresent(consumer);
            return Operation.SKIP;
        }, getWorld().isClient);
        Optional.ofNullable(predicate.apply(getItem().getItem())).ifPresent(consumer);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }
}
