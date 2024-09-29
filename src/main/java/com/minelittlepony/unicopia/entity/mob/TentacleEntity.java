package com.minelittlepony.unicopia.entity.mob;

import java.util.Comparator;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class TentacleEntity extends AbstractDecorationEntity {
    static final byte ATTACK_STATUS = 54;
    static final int MAX_GROWTH = 25;
    private static final TrackedData<Integer> GROWTH = DataTracker.registerData(TentacleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MOTION_OFFSET = DataTracker.registerData(TentacleEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Nullable
    private Box visibilityBox;

    private int prevGrowth;
    private int attackingTicks;
    private float prevAttackingTicks;

    private int ticksActive;
    private int prevMotionOffset;

    @Nullable
    private LivingEntity target;
    private final Comparator<LivingEntity> targetSorting = Comparator.comparing(this::distanceTo);

    @Nullable
    private IgnominiousBulbEntity bulb;

    public TentacleEntity(EntityType<? extends TentacleEntity> type, World world) {
        super(type, world);
    }

    public TentacleEntity(World world, BlockPos pos) {
        super(UEntities.TENTACLE, world, pos);
    }

    @Override
    protected void initDataTracker(Builder builder) {
        builder.add(GROWTH, 0);
        builder.add(MOTION_OFFSET, 0);
    }

    public void setBulb(IgnominiousBulbEntity bulb) {
        this.bulb = bulb;
    }

    public void attack(BlockPos pos) {
        var offset = pos.toCenterPos().subtract(getBlockPos().toCenterPos());

        double dX = offset.x;
        double dY = offset.y;
        double dZ = offset.z;
        double radius = Math.sqrt(dX * dX + dZ * dZ);

        setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(dY, radius) * MathHelper.DEGREES_PER_RADIAN))));
        setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN) - 90));
        getWorld().sendEntityStatus(this, ATTACK_STATUS);
        attackingTicks = 30;
        if (bulb != null) {
            bulb.setAngryFor(10);
            bulb.lookAt(EntityAnchor.FEET, pos.toCenterPos());
        }
    }

    public float getAttackProgress(float tickDelta) {
        return (30F - MathHelper.lerp(tickDelta, prevAttackingTicks, attackingTicks)) / 30F;
    }

    public float getGrowth(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevGrowth, getGrowth()) / (float)MAX_GROWTH;
    }

    public int getGrowth() {
        return dataTracker.get(GROWTH);
    }

    public void setGrowth(int growth) {
        dataTracker.set(GROWTH, Math.max(0, growth));
    }

    public float getAnimationTimer(float tickDelta) {
        return (age + tickDelta + (getUuid().getMostSignificantBits() % 100)) * 0.00043F
            + MathHelper.lerp(tickDelta, prevMotionOffset, getMotionOffset()) * 0.002F;
    }

    public int getMotionOffset() {
        return dataTracker.get(MOTION_OFFSET);
    }

    public void setMotionOffset(int motionOffset) {
        dataTracker.set(MOTION_OFFSET, motionOffset);
    }

    public boolean isAttacking() {
        return attackingTicks > 0;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (player.getStackInHand(Hand.MAIN_HAND).isIn(ItemTags.AXES)) {
                kill();
                ParticleUtils.spawnParticles(ParticleTypes.EFFECT, this, 10);
            }
            if (getWorld().random.nextInt(5) == 0 && canTarget(player)) {
                setTarget(player);
            }
        }
        addActiveTicks(20 + getWorld().random.nextInt(30));
        playSound(USounds.ENTITY_TENTACLE_ROAR, 5, 1);
        emitGameEvent(GameEvent.RESONATE_15);
        return true;
    }

    public void addActiveTicks(int ticks) {
        ticksActive = Math.min(200, ticksActive + ticks);
    }

    @Override
    public void tick() {
        prevMotionOffset = getMotionOffset();
        int growth = getGrowth();
        prevGrowth = growth;
        super.tick();
        prevAttackingTicks = attackingTicks;
        if (isAttacking()) {
            if (--attackingTicks == 12) {
                if (target != null) {
                    if (!canTarget(target)) {
                        target = null;
                    } else {
                        target.damage(getDamageSources().create(DamageTypes.MOB_ATTACK, this), 15);
                        Vec3d diff = target.getPos().subtract(getPos());
                        target.takeKnockback(1, diff.x, diff.z);

                        ParticleUtils.spawnParticles(ParticleTypes.CLOUD, target, 10);

                        for (Entity bystander : getWorld().getOtherEntities(target, target.getBoundingBox().expand(3))) {
                            if (bystander instanceof LivingEntity l) {
                                diff = l.getPos().subtract(getPos());
                                l.takeKnockback(1, diff.x, diff.z);
                                ParticleUtils.spawnParticles(ParticleTypes.CLOUD, target, 10);
                            }
                        }
                    }

                    target = null;
                }
            }
        }

        ParticleUtils.spawnParticles(ParticleTypes.ASH, this, 4);
        var sphere = new Sphere(false, 10).translate(getPos());
        ParticleUtils.spawnParticles(getWorld(), sphere, ParticleTypes.ASH, 4);

        if (!getWorld().isClient) {
            if (growth >= MAX_GROWTH / 2) {
                if (age % 50 == 0) {
                    updateTarget();
                }

                if (target != null && !isAttacking()) {
                    attack(target.getBlockPos());
                }
            }

            if (growth < MAX_GROWTH) {
                setGrowth(growth + 1);

                if (growth == 0) {
                    playSound(USounds.ENTITY_TENTACLE_DIG, 1, 1);
                    emitGameEvent(GameEvent.RESONATE_1);
                }
            }

            if (getWorld().random.nextInt(110) == 0) {
                playSound(USounds.ENTITY_TENTACLE_AMBIENT, 1, 0.3F);
            }

            if (ticksActive > 0) {
                ticksActive--;
                setMotionOffset(getMotionOffset() + ticksActive);
            }
        } else {
            if (growth < MAX_GROWTH && age % 15 == getWorld().random.nextInt(14)) {
                getWorld().addBlockBreakParticles(getBlockPos().down(), getWorld().getBlockState(getBlockPos().down()));
            }
        }
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        playSound(USounds.ENTITY_TENTACLE_ROAR, 5, 1);

        if (target instanceof PlayerEntity player) {
            Pony.of(player).getMagicalReserves().getEnergy().add(6);
        }
    }

    public LivingEntity getTarget() {
        return target;
    }

    private void updateTarget() {
        if (!canTarget(target)) {
            target = null;
        }

        if (target == null && !isAttacking()) {
            getWorld().getEntitiesByClass(HostileEntity.class, getBoundingBox().expand(10, 3, 10), this::canTarget)
                .stream()
                .sorted(targetSorting)
                .findFirst()
                .ifPresent(this::setTarget);
        }
    }

    protected boolean canTarget(LivingEntity target) {
        return target != null
            && target.isPartOfGame()
            && target.canTakeDamage()
            && !target.isSneaky()
            && canSee(target);
    }

    private boolean canSee(Entity entity) {
        return entity.getWorld() == getWorld()
                && distanceTo(entity) <= 128
                && getWorld().raycast(new RaycastContext(getPos(), entity.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case ATTACK_STATUS:
                attackingTicks = 30;
                break;
            default:
                super.handleStatus(status);
        }
    }

    @Override
    public void onBreak(Entity breaker) {

    }

    @Override
    public void onPlace() {

    }


    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (getWorld().isAir(getBlockPos()) && getWorld().getBlockState(getBlockPos().down()).isIn(BlockTags.DIRT)) {
            getWorld().setBlockState(getBlockPos(), UBlocks.CURING_JOKE.getDefaultState());
        }
    }


    @Override
    public boolean canStayAttached() {
        return getWorld().isTopSolid(getBlockPos().down(), this);
    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (visibilityBox == null) {
            visibilityBox = getBoundingBox().expand(10, 0, 10).stretch(0, 10, 0);
        }
        return visibilityBox;
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        visibilityBox = null;
        return Box.of(pos.toCenterPos(), 1, 1, 1).stretch(0, 2, 0);
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) { }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) { }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("growth", getGrowth());
        nbt.putInt("motion_offset", getMotionOffset());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setGrowth(nbt.getInt("growth"));
        setMotionOffset(nbt.getInt("motion_offset"));
    }
}
