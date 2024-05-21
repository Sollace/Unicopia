package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityPhysics<T extends Entity> implements Physics, Copyable<EntityPhysics<T>>, Tickable {

    protected final T entity;

    private float lastGravity = 1;

    private final DataTracker tracker;
    protected final DataTracker.Entry<Float> gravity;

    public EntityPhysics(T entity) {
        this.entity = entity;
        this.tracker = Trackable.of(entity).getDataTrackers().getPrimaryTracker();
        gravity = tracker.startTracking(TrackableDataType.of(PacketCodec.FLOAT), 1F);
    }

    @Override
    public void tick() {
        if (isGravityNegative()) {
            if (entity.getY() > entity.getWorld().getHeight() + 64) {
                entity.damage(entity.getDamageSources().outOfWorld(), 4.0F);
            }
        }

        float gravity = getGravityModifier();
        if (gravity != lastGravity) {
            lastGravity = gravity;

            onGravitychanged();
        }
    }

    protected void onGravitychanged() {
        entity.calculateDimensions();

        if (!entity.getWorld().isClient && entity instanceof MobEntity) {
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

        BlockPos pos = new BlockPos(
                MathHelper.floor(entity.getX()),
                MathHelper.floor(entity.getY() + entity.getHeight() + 0.20000000298023224D),
                MathHelper.floor(entity.getZ())
        );

        if (entity.getWorld().getBlockState(pos).isAir()) {
            BlockPos below = pos.down();
            BlockState block = entity.getWorld().getBlockState(below);
            if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || block.getBlock() instanceof FenceGateBlock) {
                return below;
            }
        }

        return pos;
    }

    @Override
    public void setBaseGravityModifier(float constant) {
        tracker.set(gravity, constant);
    }

    @Override
    public float getBaseGravityModifier() {
        return tracker.get(gravity);
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

            return getBaseGravityModifier() * (float)((LivingEntity)entity).getAttributeValue(UEntityAttributes.ENTITY_GRAVITY_MODIFIER);
        }

        return getBaseGravityModifier();
    }

    @Override
    public void copyFrom(EntityPhysics<T> other, boolean alive) {
        if (alive) {
            setBaseGravityModifier(other.getBaseGravityModifier());
        }
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
