package com.minelittlepony.unicopia.entity.mob;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IgnominiousBulbEntity extends MobEntity {
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(IgnominiousBulbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final List<BlockPos> TENTACLE_OFFSETS = List.of(
        new BlockPos(-3, 0, -3), new BlockPos(0, 0, -4), new BlockPos(3, 0, -3),
        new BlockPos(-4, 0,  0),                         new BlockPos(4, 0,  0),
        new BlockPos(-3, 0,  3), new BlockPos(0, 0,  4), new BlockPos(3, 0,  4)
    );

    @Nullable
    private Map<BlockPos, TentacleEntity> tentacles;

    public IgnominiousBulbEntity(EntityType<? extends IgnominiousBulbEntity> type, World world) {
        super(type, world);
    }

    public IgnominiousBulbEntity(World world) {
        super(UEntities.IGNOMINIOUS_BULB, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(ANGRY, false);
    }

    public boolean isAngry() {
        return dataTracker.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        dataTracker.set(ANGRY, angry);
    }

    @Override
    public void tick() {
        if (!getWorld().isClient && !isRemoved()) {
            var center = new BlockPos.Mutable();
            var tentacles = getTentacles();

            TENTACLE_OFFSETS.forEach(offset -> {
                tentacles.compute(adjustForTerrain(center, offset), this::updateTentacle);
            });

            if (getWorld().random.nextInt(isAngry() ? 12 : 1200) == 0) {
                for (TentacleEntity tentacle : tentacles.values()) {
                    tentacle.addActiveTicks(120);
                }
            }
            LivingEntity target = getAttacker();

            setAngry(target != null);

            if (isAngry() && getWorld().random.nextInt(30) == 0) {
                if (target instanceof PlayerEntity player) {
                    Pony.of(player).getMagicalReserves().getEnergy().add(6);
                }

                tentacles.values()
                        .stream()
                        .sorted(Comparator.comparing(a -> a.distanceTo(target)))
                        .limit(2)
                        .forEach(tentacle -> {
                    tentacle.setTarget(target);
                });
            }
        }

        super.tick();
    }

    private Map<BlockPos, TentacleEntity> getTentacles() {
        if (tentacles == null) {
            tentacles = getWorld().getEntitiesByClass(TentacleEntity.class, this.getBoundingBox().expand(5, 0, 5), EntityPredicates.VALID_ENTITY)
                    .stream()
                    .collect(Collectors.toMap(TentacleEntity::getBlockPos, Function.identity()));
        }
        return tentacles;
    }

    private TentacleEntity updateTentacle(BlockPos pos, @Nullable TentacleEntity tentacle) {
        if (tentacle == null || tentacle.isRemoved()) {
            tentacle = new TentacleEntity(getWorld(), pos);
            tentacle.updatePositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, getWorld().random.nextFloat() * 360, 0);
            getWorld().spawnEntity(tentacle);
        }
        return tentacle;
    }

    private BlockPos adjustForTerrain(BlockPos.Mutable mutable, BlockPos offset) {
        World w = getWorld();
        mutable.set(getBlockPos());
        mutable.move(offset);
        while (isSpace(w, mutable.down()) && w.isInBuildLimit(mutable)) {
            mutable.move(Direction.DOWN);
        }
        while (!isPosValid(w, mutable) && w.isInBuildLimit(mutable)) {
            mutable.move(Direction.UP);
        }
        if (w.getBlockState(mutable).isReplaceable()) {
            w.breakBlock(mutable, true);
        }
        return mutable.toImmutable();
    }

    private boolean isPosValid(World w, BlockPos pos) {
        return w.isTopSolid(pos.down(), this) && isSpace(w, pos);
    }

    private boolean isSpace(World w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        return state.isAir() || state.isReplaceable();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        getTentacles().values().forEach(tentacle -> tentacle.remove(reason));
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) { }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) { }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return USounds.ENTITY_IGNIMEOUS_BULB_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return USounds.ENTITY_IGNIMEOUS_BULB_DEATH;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("angry", isAngry());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setAngry(nbt.getBoolean("angry"));
    }
}
