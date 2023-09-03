package com.minelittlepony.unicopia.entity.mob;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.block.SideShapeType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CrystalShardsEntity extends StationaryObjectEntity {
    static final byte SHAKE = 1;

    static final int FULL_GROWTH_AGE = 25;

    private static final Set<Direction> ALL_DIRECTIONS = Set.of(Direction.values());
    private static final TrackedData<Direction> ATTACHMENT_FACE = DataTracker.registerData(CrystalShardsEntity.class, TrackedDataHandlerRegistry.FACING);
    private static final TrackedData<Integer> GROWTH = DataTracker.registerData(CrystalShardsEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DECAYING = DataTracker.registerData(CrystalShardsEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CORRUPT = DataTracker.registerData(CrystalShardsEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static boolean infestBlock(ServerWorld world, BlockPos pos) {
        if (world.isAir(pos) || !world.getFluidState(pos).isOf(Fluids.EMPTY)) {
            return false;
        }
        boolean success = false;
        for (Direction face : getFreeFaces(world, pos)) {
            CrystalShardsEntity shards = UEntities.CRYSTAL_SHARDS.create(world);
            shards.setPosition(pos.offset(face).toCenterPos());
            shards.setAttachmentFace(face);
            shards.setCorrupt(true);

            world.spawnEntity(shards);
            success = true;
        }
        return success;
    }

    public static Set<Direction> getFreeFaces(World world, BlockPos pos) {
        Set<Direction> freeFaces = new HashSet<>(ALL_DIRECTIONS);
        freeFaces.removeAll(getOccupiedFaces(world, pos));
        freeFaces.removeIf(face -> isInvalid(world, pos.offset(face), face));
        return freeFaces;
    }

    public static Set<Direction> getOccupiedFaces(World world, BlockPos pos) {
        return world.getEntitiesByClass(CrystalShardsEntity.class, new Box(pos).expand(4), EntityPredicates.VALID_ENTITY)
                .stream()
                .map(e -> e.getAttachmentFace())
                .distinct()
                .collect(Collectors.toSet());
    }

    static boolean isInvalid(World world, BlockPos crystalPos, Direction attachmentFace) {
        if (!world.isAir(crystalPos)) {
            return true;
        }
        BlockPos attachmentPos = crystalPos.offset(attachmentFace.getOpposite());
        return !world.getBlockState(attachmentPos).isSideSolid(world, attachmentPos, attachmentFace, SideShapeType.RIGID);
    }

    private int prevAge;

    private int ticksShaking;

    public CrystalShardsEntity(EntityType<CrystalShardsEntity> type, World world) {
        super(type, world);
        setYaw(world.getRandom().nextFloat() * 360);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(ATTACHMENT_FACE, Direction.UP);
        dataTracker.startTracking(GROWTH, 0);
        dataTracker.startTracking(DECAYING, false);
        dataTracker.startTracking(CORRUPT, false);
    }

    @Override
    public float getMaxHealth() {
        return 15F;
    }

    public float getGrowth(float tickDelta) {
        int age = getGrowth();
        float lerped = MathHelper.clamp(MathHelper.lerp(tickDelta, prevAge, age), 0, FULL_GROWTH_AGE) / (float)FULL_GROWTH_AGE;
        return lerped * 2 * (1 + Math.abs(getUuid().getLeastSignificantBits() % 20) / 20F);
    }

    public int getGrowth() {
        return dataTracker.get(GROWTH);
    }

    public void setGrowth(int growth) {
        dataTracker.set(GROWTH, Math.max(0, growth));
    }

    public void setDecaying(boolean decaying) {
        dataTracker.set(DECAYING, decaying);
    }

    public boolean isDecaying() {
        return dataTracker.get(DECAYING);
    }
    public void setCorrupt(boolean corrupted) {
        dataTracker.set(CORRUPT, corrupted);
    }

    public boolean isCorrupt() {
        return dataTracker.get(CORRUPT);
    }

    public boolean isShaking() {
        return ticksShaking > 0;
    }

    public Direction getAttachmentFace() {
        return Objects.requireNonNullElse(dataTracker.get(ATTACHMENT_FACE), Direction.UP);
    }

    public void setAttachmentFace(@Nullable Direction face) {
        dataTracker.set(ATTACHMENT_FACE, face == null ? Direction.UP : face);
    }

    @Override
    public void tick() {
        prevAge = getGrowth();
        if (!getWorld().isClient) {
            int growAmount = 1 + Math.abs((int)getUuid().getLeastSignificantBits() % 5);
            setGrowth(prevAge + (isDecaying() ? -growAmount : growAmount));
        }
        setFireTicks(0);

        if (isDecaying() && getGrowth() == 0) {
            discard();
        }

        if (ticksShaking > 0) {
            ticksShaking--;
        }

        setPosition(getBlockPos().toCenterPos());

        super.tick();

        if (ticksShaking > 0 || getGrowth() < FULL_GROWTH_AGE) {
            if (age % random.nextBetween(2, 5) == 0) {
                playSound(USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, 1,
                        1 - MathHelper.clamp(getGrowth(1), 0, 1) * 0.5F);
            }
        }

        if (isDead() || isInvalid(getWorld(), getBlockPos(), getAttachmentFace())) {
            kill();
        }
    }

    @Override
    protected void onHurt() {
        getWorld().sendEntityStatus(this, SHAKE);
        ticksShaking = 10;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED) {
            playSound(USounds.ENTITY_CRYSTAL_SHARDS_JOSTLE, 1, 1);
            dropStack(new ItemStack(UItems.CRYSTAL_SHARD, 6));
        }
        super.remove(reason);
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case SHAKE:
                ticksShaking = 10;
                break;
            default:
                super.handleStatus(status);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("yaw", getYaw());
        nbt.putInt("growth", getGrowth());
        nbt.putString("face", getAttachmentFace().getName());
        nbt.putBoolean("decaying", isDecaying());
        nbt.putBoolean("corrupt", isCorrupt());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("yaw", NbtElement.FLOAT_TYPE)) {
            setYaw(nbt.getFloat("yaw"));
        }
        setGrowth(nbt.getInt("growth"));
        setAttachmentFace(Direction.byName(nbt.getString("face")));
        setDecaying(nbt.getBoolean("decaying"));
        setCorrupt(nbt.getBoolean("corrupt"));
        prevAge = getGrowth();
    }
}
