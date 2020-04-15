package com.minelittlepony.unicopia.entity;

import java.util.UUID;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.ITossedEffect;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.util.projectile.IAdvancedProjectile;
import com.minelittlepony.unicopia.util.projectile.ITossable;
import com.minelittlepony.unicopia.util.projectile.ITossableItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
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
public class ProjectileEntity extends ThrownItemEntity implements IMagicals, IAdvancedProjectile, ICaster<LivingEntity> {

    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(ProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(ProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(ProjectileEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private UUID ownerUuid;

    public ProjectileEntity(EntityType<ProjectileEntity> type, World world) {
        super(type, world);
    }

    public ProjectileEntity(EntityType<ProjectileEntity> type, World world, LivingEntity thrower) {
        this(type, world, thrower.x, thrower.y + thrower.getStandingEyeHeight(), thrower.z);
        setOwner(thrower);
    }

    public ProjectileEntity(EntityType<ProjectileEntity> type, World world, double x, double y, double z) {
        super(type, world);

        setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(DAMAGE, (float)0);
        getDataTracker().startTracking(EFFECT, new CompoundTag());
        getDataTracker().startTracking(HYDROPHOBIC, false);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
     }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void setOwner(LivingEntity owner) {
        ownerUuid = owner == null ? null : owner.getUuid();
    }

    @Override
    public LivingEntity getOwner() {
        if (ownerUuid == null || !(world instanceof ServerWorld)) {
            return null;
        }

        return (LivingEntity) ((ServerWorld)world).getEntity(ownerUuid);
    }

    @Override
    public int getCurrentLevel() {
        return 1;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

    @Override
    public Affinity getAffinity() {
        return hasEffect() ? Affinity.NEUTRAL : getEffect().getAffinity();
    }

    @Override
    public void setGravity(boolean gravity) {
        setNoGravity(gravity);
    }

    @Override
    public void setEffect(ITossedEffect effect) {
        setEffect((IMagicEffect)effect);
    }

    @Override
    public void setEffect(IMagicEffect effect) {
        effectDelegate.set(effect);

        if (effect != null) {
            effect.onPlaced(this);
        }
    }

    @Override
    public <T extends IMagicEffect> T getEffect(Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Override
    public void setThrowDamage(float damage) {
        getDataTracker().set(DAMAGE, Math.max(0, damage));
    }

    @Override
    public float getThrowDamage() {
        return getDataTracker().get(DAMAGE);
    }

    @Override
    public void setHydrophobic() {
        getDataTracker().set(HYDROPHOBIC, true);
    }

    @Override
    public boolean getHydrophobic() {
        return getDataTracker().get(HYDROPHOBIC);
    }

    @Override
    public void tick() {

        if (!world.isClient()) {
            if (Math.abs(getVelocity().x) < 0.01 && Math.abs(getVelocity().x) < 0.01 && Math.abs(getVelocity().y) < 0.01) {
                remove();
            }
        }

        super.tick();

        if (age % 1000 == 0) {
            setNoGravity(false);
        }

        if (hasEffect()) {
            if (getEffect().isDead()) {
                remove();
            } else {
                getEffect().update(this);
            }

            if (world.isClient()) {
                getEffect().render(this);
            }
        }

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
    }

    @Override
    public void launch(Entity entityThrower, float pitch, float yaw, float wobble, float velocity, float inaccuracy) {
        setVelocity(pitch, yaw, wobble, velocity, inaccuracy);
    }

    private ParticleEffect getParticleParameters() {
       ItemStack stack = getItem();

       if (stack.isEmpty()) {
           return ParticleTypes.ITEM_SNOWBALL;
       }

       return new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
    }

    @Override
    public void handleStatus(byte byte_1) {
       if (byte_1 == 3) {
          ParticleEffect effect = getParticleParameters();

          for(int i = 0; i < 8; i++) {
             world.addParticle(effect, x, y, z, 0, 0, 0);
          }
       }

    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);

        if (compound.containsKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);

        if (hasEffect()) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(getEffect()));
        }
    }

    @Override
    protected void onCollision(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult)result);
        } else if (result.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult)result);
        }
    }

    protected void onHitBlock(BlockHitResult hit) {
        Item item = getItem().getItem();

        if (item instanceof ITossableItem) {
            ((ITossableItem)item).onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos()));
        }

        if (hasEffect()) {
            IMagicEffect effect = getEffect();

            if (effect instanceof ITossable) {
                ((ITossable<?>)effect).onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos()));
            }
        }
    }

    protected void onHitEntity(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (entity instanceof IAdvancedProjectile) {
            return;
        }

        if (entity != null) {
            entity.damage(DamageSource.thrownProjectile(this, getOwner()), getThrowDamage());
        }

        if (!world.isClient()) {
            world.sendEntityStatus(this, (byte)3);
            remove();
        }
    }
}
