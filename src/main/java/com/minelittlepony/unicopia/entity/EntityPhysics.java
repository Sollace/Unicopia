package com.minelittlepony.unicopia.entity;

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

public class EntityPhysics<T extends Ponylike<?> & Owned<? extends Entity>> implements Physics {

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
    public double calcGravity(double worldConstant) {
        return worldConstant * getGravityModifier();
    }

    @Override
    public BlockPos getHeadPosition() {

        Entity entity = pony.getOwner();

        entity.onGround = false;

        BlockPos pos = new BlockPos(
                MathHelper.floor(entity.getX()),
                MathHelper.floor(entity.getY() + entity.getHeight() + 0.20000000298023224D),
                MathHelper.floor(entity.getZ())
        );

        if (entity.world.getBlockState(pos).isAir()) {
            BlockPos below = pos.down();
            Block block = entity.world.getBlockState(below).getBlock();
            if (block.matches(BlockTags.FENCES) || block.matches(BlockTags.WALLS) || block instanceof FenceGateBlock) {
                entity.onGround = true;
                return below;
            }
        } else {
            pony.getOwner().onGround = true;
        }

        return pos;
    }

    @Override
    public void spawnSprintingParticles() {
        Entity entity = pony.getOwner();
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
    public void toNBT(CompoundTag compound) {
        if (gravity != 0) {
            compound.putFloat("gravity", gravity);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains("gravity")) {
            gravity = compound.getFloat("gravity");
        } else {
            gravity = 0;
        }
    }
}
