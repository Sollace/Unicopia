package com.minelittlepony.unicopia.projectile;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellContainer.Operation;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
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
public class MagicProjectileEntity extends ThrownItemEntity implements Caster<LivingEntity> {
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    private static final LevelStore LEVELS = Levelled.fixed(1);

    public static final byte PROJECTILE_COLLISSION = 3;

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this, GRAVITY, false);

    private final EntityReference<Entity> homingTarget = new EntityReference<>();

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world) {
        super(type, world);
    }

    public MagicProjectileEntity(World world, @Nullable LivingEntity thrower) {
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
    public Entity getEntity() {
        return this;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        setOwner(owner);
    }

    public void setHomingTarget(@Nullable Entity target) {
        homingTarget.set(target);
    }

    @Override
    @Nullable
    public LivingEntity getMaster() {
        return (LivingEntity)getOwner();
    }

    @Override
    public LevelStore getLevel() {
        return LEVELS;
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
        if (!world.isClient() && !homingTarget.isPresent(world)) {
            if (getVelocity().length() < 0.01) {
                discard();
            }
        }

        super.tick();

        if (getOwner() == null) {
            return;
        }

        getSpellSlot().get(true).filter(spell -> spell.tick(this, Situation.PROJECTILE));

        if (getHydrophobic()) {
            if (world.getBlockState(getBlockPos()).getMaterial().isLiquid()) {
                Vec3d vel = getVelocity();

                double velY = vel.y;

                velY *= -1;

                if (!hasNoGravity()) {
                    velY += 0.16;
                }

                setVelocity(new Vec3d(vel.x, velY, vel.z));
            }
        }

        homingTarget.ifPresent(world, e -> {
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
             world.addParticle(effect, getX(), getY(), getZ(), 0, 0, 0);
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
            discard();
            super.onCollision(result);

            if (!world.isClient()) {
                world.sendEntityStatus(this, PROJECTILE_COLLISSION);
                discard();
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);

        forEachDelegates(effect -> effect.onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos())));
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
                entity.damage(DamageSource.thrownProjectile(this, getOwner()), getThrowDamage());
            }

            forEachDelegates(effect -> effect.onImpact(this, entity));
        }
    }

    protected void forEachDelegates(Consumer<ProjectileDelegate> consumer) {
        getSpellSlot().forEach(spell -> {
            if (SpellPredicate.HAS_PROJECTILE_EVENTS.test(spell)) {
                consumer.accept((ProjectileDelegate)spell);
            }
            return Operation.SKIP;
        }, true);
        if (getItem().getItem() instanceof ProjectileDelegate) {
            consumer.accept(((ProjectileDelegate)getItem().getItem()));
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }
}
