package com.minelittlepony.unicopia.projectile;

import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Magical;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.ThrowableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A generalised version of Mojang's projectile entity class with added support for a custom appearance and water phobia.
 *
 * Can also carry a spell if needed.
 */
public class MagicProjectileEntity extends ThrownItemEntity implements Magical, Projectile, Caster<LivingEntity> {

    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private UUID ownerUuid;

    private BlockPos lastBlockPos;

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world) {
        super(type, world);
    }

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world, LivingEntity thrower) {
        this(type, world, thrower.getX(), thrower.getY() + thrower.getStandingEyeHeight(), thrower.getZ());
        setOwner(thrower);
    }

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world, double x, double y, double z) {
        super(type, world);

        setPos(x, y, z);
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
        return hasSpell() ? Affinity.NEUTRAL : getSpell().getAffinity();
    }

    @Override
    public void setGravity(boolean gravity) {
        setNoGravity(gravity);
    }

    @Override
    public void setEffect(ThrowableSpell effect) {
        setSpell(effect);
    }

    @Override
    public EffectSync getPrimarySpellSlot() {
        return effectDelegate;
    }

    @Override
    public void setSpell(Spell effect) {
        Caster.super.setSpell(effect);

        if (effect != null) {
            effect.onPlaced(this);
        }
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

        if (hasSpell()) {
            if (lastBlockPos == null || !lastBlockPos.equals(getBlockPos())) {
                lastBlockPos = getBlockPos();
            }

            if (getSpell().isDead()) {
                remove();
            } else {
                getSpell().update(this);
            }

            if (world.isClient()) {
                getSpell().render(this);
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
        setProperties(entityThrower, pitch, yaw, wobble, velocity, inaccuracy);
    }

    private ParticleEffect getParticleParameters() {
       ItemStack stack = getItem();

       if (stack.isEmpty()) {
           return ParticleTypes.ITEM_SNOWBALL;
       }

       return new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
    }

    @Override
    public void handleStatus(byte id) {
       if (id == 3) {
          ParticleEffect effect = getParticleParameters();

          for(int i = 0; i < 8; i++) {
             world.addParticle(effect, getX(), getY(), getZ(), 0, 0, 0);
          }
       }

    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);

        if (compound.contains("effect")) {
            setSpell(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);

        if (hasSpell()) {
            compound.put("effect", SpellRegistry.toNBT(getSpell()));
        }
    }

    @Override
    protected void onCollision(HitResult result) {
        if (!removed) {
            remove();

            if (result.getType() == HitResult.Type.BLOCK) {
                onHitBlock((BlockHitResult)result);
            } else if (result.getType() == HitResult.Type.ENTITY) {
                onHitEntity((EntityHitResult)result);
            }
        }
    }

    protected void onHitBlock(BlockHitResult hit) {
        if (hasSpell()) {
            Spell effect = getSpell();

            if (effect instanceof ThrowableSpell) {
                ((ThrowableSpell)effect).onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos()));
            }
        }
    }

    protected void onHitEntity(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (entity instanceof Projectile) {
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

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }
}
