package com.minelittlepony.unicopia.projectile;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WeaklyOwned;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
 */
public class MagicProjectileEntity extends ThrownItemEntity implements WeaklyOwned.Mutable<LivingEntity> {
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public static final byte PROJECTILE_COLLISSION = 3;

    private final EntityReference<Entity> homingTarget = new EntityReference<>();
    private EntityReference<LivingEntity> owner;

    private int maxAge = 90;

    public MagicProjectileEntity(EntityType<? extends MagicProjectileEntity> type, World world) {
        super(type, world);
    }

    public MagicProjectileEntity(World world) {
        this(UEntities.THROWN_ITEM, world);
    }

    public MagicProjectileEntity(World world, LivingEntity thrower) {
        super(UEntities.THROWN_ITEM, thrower, world);
    }

    protected MagicProjectileEntity(EntityType<? extends MagicProjectileEntity> type, World world, LivingEntity thrower) {
        super(type, thrower, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DAMAGE, 0F);
    }

    @Override
    public World asWorld() {
        return getWorld();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    public final void setMaster(LivingEntity owner) {
        setOwner(owner);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity instanceof LivingEntity l) {
            WeaklyOwned.Mutable.super.setMaster(l);
        }
    }

    @Override
    @Nullable
    public final Entity getOwner() {
        return getMaster();
    }

    @Override
    public EntityReference<LivingEntity> getMasterReference() {
        if (owner == null) {
            owner = new EntityReference<>();
        }
        return owner;
    }

    public void setHomingTarget(@Nullable Entity target) {
        homingTarget.set(target);
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

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public void tick() {
        if (maxAge > 0 && !getWorld().isClient() && homingTarget.getOrEmpty(asWorld()).isEmpty() && (getVelocity().length() < 0.1 || age > maxAge)) {
            discard();
        }

        super.tick();

        homingTarget.ifPresent(getWorld(), e -> {
            setNoGravity(true);
            noClip = true;
            setVelocity(getVelocity().add(e.getPos().subtract(getPos()).normalize().multiply(0.2)).multiply(0.6, 0.6, 0.6));
        });
    }

    private ParticleEffect getParticleParameters() {
       ItemStack stack = getStack();

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
        homingTarget.fromNBT(compound.getCompound("homingTarget"), getRegistryManager());
        getMasterReference().fromNBT(compound.getCompound("owner"), getRegistryManager());
        if (compound.contains("maxAge", NbtElement.INT_TYPE)) {
            maxAge = compound.getInt("maxAge");
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.put("homingTarget", homingTarget.toNBT(getRegistryManager()));
        compound.put("owner", getMasterReference().toNBT(getRegistryManager()));
        compound.putInt("maxAge", maxAge);
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
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);

        forEachDelegates(effect -> effect.onImpact(this, hit), ProjectileDelegate.BlockHitListener.PREDICATE);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (!(entity instanceof FallingBlockEntity) && EquinePredicates.IS_MAGIC_IMMUNE.test(entity)) {
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

    public void knockback(LivingEntity target, DamageSource source, ItemStack weapon) {
        double d = weapon != null && getWorld() instanceof ServerWorld serverWorld ? EnchantmentHelper.modifyKnockback(serverWorld, weapon, target, source, 0) : 0;
        if (d > 0) {
            double e = Math.max(0, 1 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
            Vec3d vec3d = this.getVelocity().multiply(1, 0, 1).normalize().multiply(d * 0.6 * e);
            if (vec3d.lengthSquared() > 0) {
                target.addVelocity(vec3d.x, 0.1, vec3d.z);
            }
        }
    }

    protected <T extends ProjectileDelegate> void forEachDelegates(Consumer<T> consumer, Function<Object, T> predicate) {
        try {
            Optional.ofNullable(predicate.apply(getStack().getItem())).ifPresent(consumer);
        } catch (Throwable t) {
            Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", getMasterReference(), t);
        }
    }
}
