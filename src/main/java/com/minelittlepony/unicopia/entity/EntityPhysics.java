package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.util.Copieable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityPhysics<T extends Owned<? extends Entity>> implements Physics, Copieable<EntityPhysics<T>> {

    private float gravity = 1;

    protected final T pony;

    public EntityPhysics(T pony) {
        this.pony = pony;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public Vec3d getMotionAngle() {
        return new Vec3d(pony.getMaster().getPitch(1), pony.getMaster().getYaw(1), 0);
    }

    @Override
    public double calcGravity(double worldConstant) {
        return worldConstant * getGravityModifier();
    }

    @Override
    public BlockPos getHeadPosition() {

        Entity entity = pony.getMaster();

        entity.setOnGround(false);

        BlockPos pos = new BlockPos(
                MathHelper.floor(entity.getX()),
                MathHelper.floor(entity.getY() + entity.getHeight() + 0.20000000298023224D),
                MathHelper.floor(entity.getZ())
        );

        if (entity.world.getBlockState(pos).isAir()) {
            BlockPos below = pos.down();
            Block block = entity.world.getBlockState(below).getBlock();
            if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || block instanceof FenceGateBlock) {
                entity.setOnGround(true);
                return below;
            }
        } else {
            pony.getMaster().setOnGround(true);
        }

        return pos;
    }

    @Override
    public void spawnSprintingParticles() {
        Entity entity = pony.getMaster();
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
        gravity = constant;
    }

    @Override
    public float getGravityModifier() {
        return gravity;
    }

    @Override
    public void copyFrom(EntityPhysics<T> other) {
        gravity = other.gravity;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putFloat("gravity", gravity);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        gravity = compound.getFloat("gravity");
    }
}
