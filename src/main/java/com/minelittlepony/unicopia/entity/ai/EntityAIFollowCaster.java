package com.minelittlepony.unicopia.entity.ai;

import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowCaster<T extends EntityLivingBase> extends EntityAIBase {

    protected final ICaster<?> casted;

    protected final EntityLiving entity;

    protected EntityLivingBase owner;

    protected final World world;

    public final double followSpeed;

    private final PathNavigate petPathfinder;

    private int timeout;

    public float maxDist;
    public float minDist;

    private float oldWaterCost;

    public EntityAIFollowCaster(ICaster<T> casted, double followSpeed, float minDist, float maxDist) {
        this.casted = casted;

        this.entity = (EntityLiving)casted.getEntity();
        this.world = casted.getWorld();

        this.followSpeed = followSpeed;
        this.petPathfinder = entity.getNavigator();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setMutexBits(3);

        if (!(petPathfinder instanceof PathNavigateGround || petPathfinder instanceof PathNavigateFlying)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        EntityLivingBase owner = casted.getOwner();

        if (owner == null
                || (owner instanceof EntityPlayer && ((EntityPlayer)owner).isSpectator())
                || entity.getDistanceSq(owner) < (minDist * minDist)) {
            return false;
        }

        this.owner = owner;

        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !petPathfinder.noPath()
                && entity.getDistanceSq(owner) > (maxDist * maxDist);
    }

    @Override
    public void startExecuting() {
        timeout = 0;
        oldWaterCost = entity.getPathPriority(PathNodeType.WATER);
        entity.setPathPriority(PathNodeType.WATER, 0);
    }

    @Override
    public void resetTask() {
        owner = null;
        petPathfinder.clearPath();
        entity.setPathPriority(PathNodeType.WATER, oldWaterCost);
    }

    @Override
    public void updateTask() {
        entity.getLookHelper().setLookPositionWithEntity(owner, 10, entity.getVerticalFaceSpeed());

        if (--timeout > 0) {
            return;
        }

        timeout = 10;

        if (petPathfinder.tryMoveToEntityLiving(owner, followSpeed)
                || entity.getLeashed()
                || entity.isRiding()
                || entity.getDistanceSq(owner) < 144) {
            return;
        }

        int x = MathHelper.floor(owner.posX) - 2;
        int y = MathHelper.floor(owner.getEntityBoundingBox().minY);
        int z = MathHelper.floor(owner.posZ) - 2;

        for (int offX = 0; offX <= 4; offX++) {
            for (int offZ = 0; offZ <= 4; offZ++) {
                if (canTeleport(x, y, z, offX, offZ)) {

                    entity.setLocationAndAngles((x + offX) + 0.5F, y, (z + offZ) + 0.5F, entity.rotationYaw, entity.rotationPitch);
                    petPathfinder.clearPath();

                    return;
                }
            }
        }
    }

    protected boolean canTeleport(int x, int y, int z, int xOffset, int zOffset) {
        if (xOffset < 1 || zOffset < 1 || xOffset > 3 || zOffset > 3) {
            return true;
        }

        BlockPos pos = new BlockPos(x + xOffset, y - 1, z + zOffset);
        IBlockState state = world.getBlockState(pos);

        return state.getBlockFaceShape(world, pos, EnumFacing.DOWN) == BlockFaceShape.SOLID
                && state.canEntitySpawn(entity)
                && world.isAirBlock(pos.up())
                && world.isAirBlock(pos.up(2));
    }
}