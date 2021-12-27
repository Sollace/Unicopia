package com.minelittlepony.unicopia.entity;

import java.util.EnumSet;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FairyEntity extends PathAwareEntity implements LightEmittingEntity, Owned<LivingEntity> {
    private final EntityReference<LivingEntity> owner = new EntityReference<>();

    private final EntityReference<LivingEntity> assignment = new EntityReference<>();

    private final LightEmitter<?> emitter = new LightEmitter<>(this);

    private Optional<BlockPos> stayingPos = Optional.empty();

    protected FairyEntity(EntityType<FairyEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(true);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    protected void initGoals() {
        goalSelector.add(5, new StayGoal());
        goalSelector.add(6, new FollowEntityGoal(2, 2, 30));
        goalSelector.add(7, new WanderAroundGoal(this, 1));
    }

    @Override
    public int getLightLevel() {
        return 15;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.owner.set(owner);
    }

    @Override
    public LivingEntity getMaster() {
        return owner.get(((Entity)this).world);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    public boolean isStaying() {
        return stayingPos.isPresent();
    }

    public void setStaying(BlockPos pos) {
        stayingPos = Optional.ofNullable(pos);
    }

    @Override
    public void tick() {
        this.onGround = true;
        super.tick();
        emitter.tick();

        if (world.random.nextInt(20) == 3) {
            world.addParticle(new MagicParticleEffect(0xFFFFFF), getParticleX(1), getY(), getParticleZ(1), 0, 0, 0);
        }

        if (age % 60 == 0) {
            playSound(USounds.ITEM_MAGIC_AURA, 0.1F, 6);
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (isTouchingWater()) {
            updateVelocity(0.02f, movementInput);
            move(MovementType.SELF, getVelocity());
            setVelocity(getVelocity().multiply(0.8f));
        } else if (isInLava()) {
            updateVelocity(0.02f, movementInput);
            move(MovementType.SELF, getVelocity());
            setVelocity(getVelocity().multiply(0.5));
        } else {
            float f = 0.91f;
            if (onGround) {
                f = world.getBlockState(getBlockPos().down()).getBlock().getSlipperiness() * 0.91f;
            }
            float g = 0.16277137f / (f * f * f);
            f = 0.91f;
            if (onGround) {
                f = world.getBlockState(getBlockPos().down()).getBlock().getSlipperiness() * 0.91f;
            }
            updateVelocity(onGround ? 0.1f * g : 0.02f, movementInput);
            move(MovementType.SELF, getVelocity());
            setVelocity(getVelocity().multiply(f));
        }
        updateLimbs(this, false);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {

        if (hand == Hand.MAIN_HAND && isStaying() && player == getMaster()) {
            stayingPos = Optional.empty();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        emitter.remove();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.put("owner", owner.toNBT());
        stayingPos.ifPresent(pos -> {
            tag.put("stayingPos", NbtHelper.fromBlockPos(pos));
        });
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        if (tag.contains("owner")) {
            owner.fromNBT(tag.getCompound("owner"));
        }
        stayingPos = tag.contains("stayingPos") ? Optional.of(NbtHelper.toBlockPos(tag.getCompound("stayingPos"))) : Optional.empty();
    }

    class FollowEntityGoal extends Goal {
        @Nullable
        private LivingEntity target;

        private final double speed;
        private int updateCountdownTicks;

        private final float minDistance;
        private final float maxDistance;
        private float oldWaterPathFindingPenalty;

        public FollowEntityGoal(double speed, float minDistance, float maxDistance) {
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            target = assignment.get(world);

            if (target == null) {
                target = getMaster();
            }
            if (target == null) {
                target = world.getClosestPlayer(FairyEntity.this, maxDistance);
            }
            return target != null;
        }

        @Override
        public boolean shouldContinue() {
            return target != null && !isStaying() && !getNavigation().isIdle() && squaredDistanceTo(target) > minDistance * minDistance;
        }

        @Override
        public void start() {
            updateCountdownTicks = 0;
            oldWaterPathFindingPenalty = getPathfindingPenalty(PathNodeType.WATER);
            setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        }

        @Override
        public void stop() {
            target = null;
            getNavigation().stop();
            setPathfindingPenalty(PathNodeType.WATER, oldWaterPathFindingPenalty);
        }

        @Override
        public void tick() {
            if (target == null || isLeashed() || isStaying()) {
                return;
            }

            getLookControl().lookAt(target, 10, getMaxLookPitchChange());

            if (--updateCountdownTicks > 0) {
                return;
            }

            updateCountdownTicks = getTickCount(10);

            if (getPos().squaredDistanceTo(target.getPos()) <= minDistance * minDistance) {

                BlockPos pos = FuzzyPositions.localFuzz(FairyEntity.this.world.random, 5, 5);
                if (pos != null) {
                    getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), speed);
                } else {
                    getNavigation().stop();
                }
            } else {
                getNavigation().startMovingTo(target, speed);
            }
        }
    }

    class StayGoal extends Goal {
        private int updateCountdownTicks;

        public StayGoal() {
            setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return isStaying();
        }

        @Override
        public boolean shouldContinue() {
            return isStaying() && !getNavigation().isIdle();
        }

        @Override
        public void start() {
            updateCountdownTicks = 0;
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public void tick() {
            if (--updateCountdownTicks > 0) {
                return;
            }

            updateCountdownTicks = getTickCount(10);

            stayingPos.ifPresent(pos -> {
                if (pos.getSquaredDistance(getBlockPos()) <= 1) {
                    getNavigation().stop();
                } else {
                    getNavigation().startMovingTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, speed);
                }
            });
        }
    }
}
