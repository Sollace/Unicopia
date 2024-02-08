package com.minelittlepony.unicopia.entity.mob;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class IgnominiousBulbEntity extends MobEntity {
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(IgnominiousBulbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> AGE = DataTracker.registerData(IgnominiousBulbEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int BABY_AGE = PassiveEntity.BABY_AGE;
    private static final int HAPPY_TICKS = 40;
    private static final List<BlockPos> TENTACLE_OFFSETS = List.of(
        new BlockPos(-3, 0, -3), new BlockPos(0, 0, -4), new BlockPos(3, 0, -3),
        new BlockPos(-4, 0,  0),                         new BlockPos(4, 0,  0),
        new BlockPos(-3, 0,  3), new BlockPos(0, 0,  4), new BlockPos(3, 0,  4)
    );

    @Nullable
    private Map<BlockPos, EntityReference<TentacleEntity>> tentacles;

    private int prevAge;
    private int happyTicks;
    private int angryTicks;

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
        dataTracker.startTracking(AGE, 0);
    }

    @Override
    public boolean isBaby() {
        return getAge() < 0;
    }

    @Override
    public void setBaby(boolean baby) {
        setAge(BABY_AGE);
    }

    protected int getAge() {
        return dataTracker.get(AGE);
    }

    protected void setAge(int age) {
        dataTracker.set(AGE, age);
    }

    public float getScale(float tickDelta) {
        return Math.max(0.2F, 1 - (MathHelper.clamp(MathHelper.lerp(tickDelta, prevAge, getAge()), BABY_AGE, 0F) / BABY_AGE));
    }

    public boolean isAngry() {
        return dataTracker.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        if (angry != isAngry()) {
            dataTracker.set(ANGRY, angry);
        }
    }

    public void setAngryFor(int angryTicks) {
        this.angryTicks = angryTicks;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);
        if (isBaby() && stack.isOf(Items.BONE_MEAL)) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            growUp(10);
            if (!getWorld().isClient) {
                getWorld().syncWorldEvent(WorldEvents.BONE_MEAL_USED, getBlockPos(), 0);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
        Vec3d center = getPos();
        Supplier<Vec3d> offset = VecHelper.supplier(() -> (getWorld().random.nextBoolean() ? 1 : -1) * 3);
        setPosition(center.add(offset.get().multiply(1, 0, 1)));
        child.setPosition(center.add(offset.get().multiply(1, 0, 1)));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        Vec3d change = new Vec3d(x, y, z).subtract(getPos());
        super.setPosition(x, y, z);
        getTentacles().values().forEach(tentacle -> {
            tentacle.ifPresent(getWorld(), t -> {
                t.setPosition(t.getPos().add(change));
            });
        });
    }

    public void growUp(int age) {
        int currentAge = getAge();
        if (currentAge < 0) {
            setAge((age * 20) + currentAge);
            happyTicks = HAPPY_TICKS;
        }
    }

    @Override
    public void tick() {
        if (!getWorld().isClient && !isRemoved()) {
            var center = new BlockPos.Mutable();
            var tentacles = getTentacles();

            if (!isBaby() && !firstUpdate) {
                TENTACLE_OFFSETS.forEach(offset -> {
                    tentacles.compute(adjustForTerrain(center, offset), this::updateTentacle);
                });
            }

            for (EntityReference<TentacleEntity> tentacle : tentacles.values()) {
                if (getWorld().random.nextInt(isAngry() ? 12 : 1200) == 0) {
                    tentacle.ifPresent(getWorld(), t -> {
                        t.addActiveTicks(120);
                    });
                }
            }
            LivingEntity target = getAttacker();
            if (!canTarget(target)) {
                target = null;
                setAttacker(null);
            }

            if (angryTicks > 0) {
                angryTicks--;
            }

            setAngry(!isBaby() && (angryTicks > 0 || target != null));

            float healthPercentage = getHealth() / getMaxHealth();
            if (isAngry() && target != null && getWorld().random.nextInt(1 + (int)(healthPercentage * 30)) == 0) {
                if (target instanceof PlayerEntity player) {
                    Pony.of(player).getMagicalReserves().getEnergy().add(6);
                }

                final LivingEntity t = target;
                tentacles.values()
                        .stream()
                        .flatMap(tentacle -> tentacle.getOrEmpty(getWorld()).stream())
                        .sorted(Comparator.comparing(a -> a.distanceTo(t)))
                        .limit(2)
                        .forEach(tentacle -> {
                    tentacle.setTarget(t);
                });
            }

            if (target != null) {
                lookAtEntity(target, 10, 10);
            }
        }

        super.tick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        prevAge = getAge();

        if (getWorld().isClient) {
            if (happyTicks > 0 && --happyTicks % 4 == 0) {
                getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, getParticleX(1), getRandomBodyY() + 0.5, getParticleZ(1), 0, 0, 0);
            }
        } else {
            if (prevAge < 0) {
                setAge(prevAge + 1);
            } else if (prevAge > 0) {
                setAge(prevAge - 1);
            }
        }
    }

    private Map<BlockPos, EntityReference<TentacleEntity>> getTentacles() {
        if (tentacles == null) {
            tentacles = getWorld().getEntitiesByClass(TentacleEntity.class, getBoundingBox().expand(5, 0, 5), EntityPredicates.VALID_ENTITY)
                    .stream()
                    .collect(Collectors.toMap(TentacleEntity::getBlockPos, tentacle -> {
                        tentacle.setBulb(this);
                        return new EntityReference<>(tentacle);
                    }));
        }
        return tentacles;
    }

    private EntityReference<TentacleEntity> updateTentacle(BlockPos pos, @Nullable EntityReference<TentacleEntity> tentacle) {
        if (tentacle == null || tentacle.getOrEmpty(getWorld()).filter(Entity::isAlive).isEmpty()) {
            var created = new TentacleEntity(getWorld(), pos);
            created.updatePositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, getWorld().random.nextFloat() * 360, 0);
            created.setBulb(this);

            if (getWorld().isTopSolid(pos.down(), this)) {
                getWorld().spawnEntity(created);
            }

            return new EntityReference<>(created);
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
        getTentacles().values().forEach(tentacle -> {
            tentacle.ifPresent(getWorld(), t -> t.remove(reason));
        });
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) { }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) { }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        if (isBaby()) {
            return SoundEvents.ENTITY_HORSE_BREATHE;
        }
        return USounds.ENTITY_IGNIMEOUS_BULB_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return USounds.ENTITY_IGNIMEOUS_BULB_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!isBaby() && getWorld().random.nextInt(2) == 0) {
            return SoundEvents.ENTITY_CAMEL_AMBIENT;
        }
        return SoundEvents.ITEM_BONE_MEAL_USE;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("angry", isAngry());
        nbt.putInt("age", getAge());
        NbtList tentacles = new NbtList();
        getTentacles().forEach((pos, tentacle) -> {
            var compound = new NbtCompound();
            compound.put("pos", NbtSerialisable.BLOCK_POS.write(pos));
            compound.put("target", tentacle.toNBT());
            tentacles.add(compound);
        });
        nbt.put("tentacles", tentacles);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setAngry(nbt.getBoolean("angry"));
        setAge(nbt.getInt("age"));
        if (!getWorld().isClient) {
            if (nbt.contains("tentacles", NbtElement.LIST_TYPE)) {
                var tentacles = new HashMap<BlockPos, EntityReference<TentacleEntity>>();
                nbt.getList("tentacles", NbtElement.COMPOUND_TYPE).forEach(tag -> {
                    var compound = (NbtCompound)tag;
                    tentacles.put(NbtSerialisable.BLOCK_POS.read(compound.getCompound("pos")), new EntityReference<>(compound.getCompound("target")));
                });
                this.tentacles = tentacles;
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (AGE.equals(data)) {
            calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.changing(3, 2).scaled(getScale(1));
    }
}
