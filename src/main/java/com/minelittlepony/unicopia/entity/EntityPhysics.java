package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
import com.minelittlepony.unicopia.util.Copieable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityPhysics<T extends Entity> implements Physics, Copieable<EntityPhysics<T>>, Tickable {

    private final TrackedData<Float> gravity;

    protected final T entity;

    private float lastGravity = 1;

    public EntityPhysics(T entity, TrackedData<Float> gravity) {
        this(entity, gravity, true);
    }

    public EntityPhysics(T entity, TrackedData<Float> gravity, boolean register) {
        this.entity = entity;
        this.gravity = gravity;

        if (register) {
            this.entity.getDataTracker().startTracking(gravity, 1F);
        }
    }

    @Override
    public void tick() {
        if (isGravityNegative()) {
            if (entity.getY() > entity.world.getHeight() + 64) {
                entity.damage(DamageSource.OUT_OF_WORLD, 4.0F);
            }

            entity.setOnGround(entity.verticalCollision && entity.getVelocity().getY() > 0);
        }

        float gravity = this.getGravityModifier();
        if (gravity != lastGravity) {
            lastGravity = gravity;

            onGravitychanged();
        }
    }

    protected void onGravitychanged() {
        entity.calculateDimensions();

        if (!entity.world.isClient && entity instanceof MobEntity) {
            ((MobEntity)entity).getNavigation().stop();
            ((MobEntity)entity).setTarget(null);
        }
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public Vec3d getMotionAngle() {
        return new Vec3d(entity.getPitch(1), entity.getYaw(1), 0);
    }

    @Override
    public double calcGravity(double worldConstant) {
        return worldConstant * getGravityModifier();
    }

    @Override
    public BlockPos getHeadPosition() {

        entity.setOnGround(false);

        BlockPos pos = new BlockPos(
                MathHelper.floor(entity.getX()),
                MathHelper.floor(entity.getY() + entity.getHeight() + 0.20000000298023224D),
                MathHelper.floor(entity.getZ())
        );

        if (entity.world.getBlockState(pos).isAir()) {
            BlockPos below = pos.down();
            BlockState block = entity.world.getBlockState(below);
            if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || block.getBlock() instanceof FenceGateBlock) {
                entity.setOnGround(true);
                return below;
            }
        } else {
            entity.setOnGround(true);
        }

        return pos;
    }

    @Override
    public void spawnSprintingParticles() {
        BlockState state = entity.world.getBlockState(getHeadPosition());
        if (state.getRenderType() != BlockRenderType.INVISIBLE) {
            Vec3d vel = entity.getVelocity();
            entity.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    entity.getX() + (entity.world.random.nextFloat() - 0.5D) * entity.getWidth(),
                    entity.getY() + entity.getHeight() - 0.1D,
                    entity.getZ() + (entity.world.random.nextFloat() - 0.5D) * entity.getWidth(),
                    vel.x * -4, -1.5D, vel.z * -4);
        }
    }

    @Override
    public void setBaseGravityModifier(float constant) {
        entity.getDataTracker().set(gravity, constant);
    }

    @Override
    public float getBaseGravityModifier() {
        return entity.getDataTracker().get(gravity);
    }

    @Override
    public float getGravityModifier() {

        if (entity instanceof LivingEntity) {
            if (((LivingEntity)entity).getAttributes() == null) {
                // may be null due to order of execution in the constructor.
                // Will have the default (1) here in any case, so it's safe to ignore the attribute at this point.
                return getBaseGravityModifier();
            }

            if (((LivingEntity)entity).isSleeping()) {
                return 1;
            }

            return getBaseGravityModifier() * (float)((LivingEntity)entity).getAttributeValue(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);
        }

        return getBaseGravityModifier();
    }

    @Override
    public void copyFrom(EntityPhysics<T> other) {
        setBaseGravityModifier(other.getBaseGravityModifier());
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putFloat("gravity", getBaseGravityModifier());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        setBaseGravityModifier(compound.getFloat("gravity"));
    }

}
