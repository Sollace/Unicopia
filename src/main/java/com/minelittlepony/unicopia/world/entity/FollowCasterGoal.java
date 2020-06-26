package com.minelittlepony.unicopia.world.entity;

import java.util.EnumSet;

import com.minelittlepony.unicopia.magic.Caster;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;

@Deprecated
public class FollowCasterGoal<T extends MobEntity> extends Goal {

    protected final Caster<?> caster;

    protected final MobEntity entity;

    protected LivingEntity owner;

    protected final WorldView world;

    public final double followSpeed;

    private final EntityNavigation navigation;

    private int timeout;

    public float maxDistance;
    public float minDistance;

    private float oldWaterCost;

    public FollowCasterGoal(Caster<T> caster, double followSpeed, float minDist, float maxDist) {
        this.caster = caster;

        this.entity = (MobEntity)caster.getEntity();
        this.world = caster.getWorld();

        this.followSpeed = followSpeed;
        navigation = entity.getNavigation();
        minDistance = minDist;
        maxDistance = maxDist;
        setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));

        if (!(navigation instanceof MobNavigation || navigation instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowCasterGoal");
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity owner = caster.getOwner();

        if (owner == null
                || (owner instanceof PlayerEntity && ((PlayerEntity)owner).isSpectator())
                || entity.squaredDistanceTo(owner) < (minDistance * minDistance)) {
            return false;
        }

        this.owner = owner;

        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !navigation.isIdle()
                && entity.squaredDistanceTo(owner) > (maxDistance * maxDistance);
    }

    @Override
    public void start() {
        timeout = 0;
        oldWaterCost = entity.getPathfindingPenalty(PathNodeType.WATER);
        entity.setPathfindingPenalty(PathNodeType.WATER, 0);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        entity.setPathfindingPenalty(PathNodeType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        entity.getLookControl().lookAt(owner, 10, entity.getLookPitchSpeed());

        if (--timeout > 0) {
            return;
        }

        timeout = 10;

        if (navigation.startMovingTo(owner, followSpeed)
                || entity.isLeashed()
                || entity.hasVehicle()
                || entity.squaredDistanceTo(owner) < 144) {
            return;
        }

        int x = MathHelper.floor(owner.getX()) - 2;
        int y = MathHelper.floor(owner.getBoundingBox().minY);
        int z = MathHelper.floor(owner.getZ()) - 2;

        for (int offX = 0; offX <= 4; offX++) {
            for (int offZ = 0; offZ <= 4; offZ++) {
                if ((offX < 1 || offZ < 1 || offX > 3 || offZ > 3) && canMoveInto(new BlockPos(x + offX, y - 1, z + offZ))) {

                    entity.updatePositionAndAngles((x + offX) + 0.5F, y, (z + offZ) + 0.5F, entity.headYaw, entity.pitch);
                    navigation.stop();

                    return;
                }
            }
        }
    }

    protected boolean canMoveInto(BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        return state.allowsSpawning(world, pos, entity.getType())
                && world.isAir(pos.up())
                && world.isAir(pos.up(2));
    }
}
