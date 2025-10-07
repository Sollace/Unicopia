package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.player.LevitatedItemsInventory;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.VecHelper;
import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LevitatingItemEntity extends Entity implements Owned<PlayerEntity>, MagicImmune {
    private static final TrackedData<ItemStack> STACK = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Integer> SLOT = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<UUID>> OWNER_ID = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Optional<BlockPos>> MINING_POS = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Direction> MINING_FACE = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.FACING);

    private Vec3d positionOffset = Vec3d.ZERO;
    @Nullable
    private BlockState miningBlockState;
    @Nullable
    private BlockPos interactingPos;
    @Nullable
    private Vec3d holdPosition;

    @Nullable
    private LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord;

    private Vec3d lerpPos = new Vec3d(0, 0, 0);
    private int lerpTicks;
    private int swingTicks;

    LevitatingItemEntity(EntityType<? extends LevitatingItemEntity> type, World world) {
        super(type, world);
    }

    public LevitatingItemEntity(int slot, PlayerEntity player, ItemStack stack) {
        this(UEntities.LEVITATING_ITEM, player.getWorld());
        setMaster(player);
        setStack(stack);
        setPosition(player.getPos().add(positionOffset));
        setSlot(slot);
    }

    public void setSlot(int slot) {
        dataTracker.set(SLOT, slot);
    }

    public int getSlot() {
        return dataTracker.get(SLOT);
    }

    public void setRelativePosition(Vec3d newOffset) {
        positionOffset = newOffset;
    }

    public Vec3d getRelativePosition() {
        return positionOffset;
    }

    @Override
    protected void initDataTracker(Builder builder) {
        builder.add(STACK, ItemStack.EMPTY);
        builder.add(OWNER_ID, Optional.empty());
        builder.add(SLOT, 0);
        builder.add(MINING_POS, Optional.empty());
        builder.add(MINING_FACE, Direction.UP);
    }

    public Optional<BlockPos> getMiningPos() {
        return dataTracker.get(MINING_POS);
    }

    public Direction getMiningFace() {
        return dataTracker.get(MINING_FACE);
    }

    public void setMaster(PlayerEntity player) {
        dataTracker.set(OWNER_ID, Optional.of(player.getUuid()));
    }

    @Override
    public @Nullable PlayerEntity getMaster() {
        if (getWorld() instanceof ServerWorld sw) {
            return getMasterId().map(sw.getServer().getPlayerManager()::getPlayer).orElse(null);
        }
        return getMasterId().map(getWorld()::getPlayerByUuid).orElse(null);
    }

    @Override
    public Optional<UUID> getMasterId() {
        return dataTracker.get(OWNER_ID);
    }

    public ItemStack getStack() {
        return this.dataTracker.get(STACK);
    }

    public void setStack(ItemStack stack) {
        this.dataTracker.set(STACK, stack);
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        lerpPos = new Vec3d(x, y, z);
        lerpTicks = 3;
        setRotation(yaw, pitch);
    }

    @Override
    public double getLerpTargetX() {
        return lerpTicks > 0 ? lerpPos.x : getX();
    }

    @Override
    public double getLerpTargetY() {
        return lerpTicks > 0 ? lerpPos.y : getY();
    }

    @Override
    public double getLerpTargetZ() {
        return lerpTicks > 0 ? lerpPos.z : getZ();
    }

    public void swingAt(BlockPos pos) {
        swingTicks = 10;
        this.interactingPos = pos;
    }

    public boolean startMining(PlayerEntity miner, LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord, Direction direction) {
        if (getMiningPos().isPresent()) {
            return false;
        }
        if (!getStack().getItem().canMine(blockBreakingRecord.state, getWorld(), blockBreakingRecord.pos, miner)) {
            return false;
        }
        if (!blockBreakingRecord.state.isToolRequired() || getStack().isSuitableFor(blockBreakingRecord.state)) {
            stopMining(blockBreakingRecord.pos);
            dataTracker.set(MINING_POS, Optional.of(blockBreakingRecord.pos));
            dataTracker.set(MINING_FACE, direction);
            this.blockBreakingRecord = blockBreakingRecord;
            blockBreakingRecord.claimants.add(this);
            holdPosition = getPos();
            return true;
        }
        return false;
    }

    public void stopMining(BlockPos pos) {
        if (pos.equals(getMiningPos().orElse(null))) {
            dataTracker.set(MINING_POS, Optional.empty());
            miningBlockState = null;
            if (blockBreakingRecord != null) {
                blockBreakingRecord.claimants.remove(this);
            }
            blockBreakingRecord = null;
            holdPosition = null;
            if (getWorld() instanceof ServerWorld sw) {
                sw.setBlockBreakingInfo(getId(), pos, -1);
            }
        }
    }

    @Override
    public void tick() {
        @Nullable
        PlayerEntity master = getMaster();
        if (master == null) {
            setInvisible(true);
            return;
        } else {
            setInvisible(false);

            if (master.isDead()) {
                kill();
            }
        }

        if (getStack().isEmpty() && getPassengerList().isEmpty()) {
            discard();
        }

        if (master.getWorld() instanceof ServerWorld sw) {
            if (master.getWorld() != getWorld()) {
                Vec3d newPos = master.getPos().add(positionOffset);
                teleport(sw, newPos.x, newPos.y, newPos.z, PositionFlag.VALUES, 0, 0);
            }
        }

        super.tick();
        updatePosition(master);

        if (getWorld() instanceof ServerWorld sw && getMiningPos().isPresent() && blockBreakingRecord != null) {
            tickMining(master, sw, blockBreakingRecord);
        }

        if (getWorld().isClient) {
            if (getRandom().nextInt(10) == 0) {
                ParticleUtils.spawnParticles(new MagicParticleEffect(Colors.WHITE), this, 1 + getRandom().nextInt(2));
            }
        }
    }

    private void tickMining(PlayerEntity master, ServerWorld sw, LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord) {
        BlockState state = getWorld().getBlockState(blockBreakingRecord.pos);
        lookAt(EntityAnchor.EYES, blockBreakingRecord.pos.toCenterPos());

        if (!state.equals(blockBreakingRecord.state) || !master.canModifyAt(sw, blockBreakingRecord.pos) || squaredDistanceTo(blockBreakingRecord.pos.toCenterPos()) > 100) {
            stopMining(blockBreakingRecord.pos);
        } else {
            FakePlayer fakePlayer = FakePlayer.get(sw, new GameProfile(getMasterId().orElse(null), "[Levitated Item Entity " + getUuid().toString() + "]"));
            fakePlayer.setStackInHand(Hand.MAIN_HAND, getStack());
            blockBreakingRecord.breakingProgress += state.calcBlockBreakingDelta(fakePlayer, sw, blockBreakingRecord.pos);


            if (age % 4 == 0) {
                BlockSoundGroup group = state.getSoundGroup();
                sw.playSound(null, blockBreakingRecord.pos, state.getSoundGroup().getHitSound(), SoundCategory.BLOCKS,
                        (group.getVolume() + 1) / 8F,
                        group.getPitch() / 2F);
            }

            if (blockBreakingRecord.breakingProgress >= 1) {
                if (!fakePlayer.interactionManager.tryBreakBlock(blockBreakingRecord.pos)) {
                    ((ServerPlayerEntity)master).networkHandler.sendPacket(new BlockUpdateS2CPacket(blockBreakingRecord.pos, sw.getBlockState(blockBreakingRecord.pos)));
                }
                stopMining(blockBreakingRecord.pos);
            } else {
                int stage = (int)(blockBreakingRecord.breakingProgress * 10F);
                if (stage != blockBreakingRecord.breakingStage) {
                    blockBreakingRecord.breakingStage = stage;
                    sw.setBlockBreakingInfo(getId(), blockBreakingRecord.pos, blockBreakingRecord.breakingStage);
                }
            }
        }
    }

    private void updatePosition(PlayerEntity master) {
        if (isLogicalSideForUpdatingMovement()) {

            Vec3d targetPosition = holdPosition == null ? master.getEyePos().add(master.getRotationVector(
                    (float)positionOffset.x * MathHelper.DEGREES_PER_RADIAN,
                    master.getBodyYaw() + (float)positionOffset.z * MathHelper.DEGREES_PER_RADIAN
            )) : holdPosition;

            BlockPos miningPos = getMiningPos().orElse(null);

            if (miningPos != null) {
                targetPosition = VecHelper.lerp(getSwingProgress(), targetPosition, miningPos.toCenterPos());
            }
            if (interactingPos != null) {
                targetPosition = VecHelper.lerp(getSwingProgress(), interactingPos.toCenterPos(), targetPosition);
                if (swingTicks <= -10) {
                    interactingPos = null;
                }
            }

            move(MovementType.SELF, targetPosition.subtract(getPos()).multiply(0.3));

            lerpTicks = 0;
            updateTrackedPosition(getX(), getY(), getZ());
        }

        if (lerpTicks > 0) {
            lerpPosAndRotation(lerpTicks, lerpPos.x, lerpPos.y, lerpPos.z, getYaw(), getPitch());
            lerpTicks--;
        }
    }

    private float getSwingProgress() {
        if (getMiningPos().isPresent()) {
            return (age % 10F) / 10F;
        }

        if (swingTicks > -10) {
            swingTicks--;
            return MathHelper.clamp(Math.abs(swingTicks) / 10F, 0, 1);
        }

        return 0;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        player.giveItemStack(getStack());
        remove(RemovalReason.DISCARDED);
        playSound(SoundEvents.ENTITY_ITEM_PICKUP, 2, 1);
        return ActionResult.PASS;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (STACK.equals(data)) {
            getStack().setHolder(this);
        }
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED) {
            dropAsItemEntity();
        }
        super.remove(reason);
    }

    @Override
    protected double getGravity() {
        return 0;
    }

    public void dropAsItemEntity() {
        if (getWorld() instanceof ServerWorld sw) {
            Vec3d dropPos = getPos();
            sw.spawnEntity(new ItemEntity(sw, dropPos.x, dropPos.y, dropPos.z, getStack()));
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setStack(ItemStack.fromNbtOrEmpty(getWorld().getRegistryManager(), nbt.getCompound("stack")));
        dataTracker.set(OWNER_ID, nbt.containsUuid("owner") ? Optional.of(nbt.getUuid("owner")) : Optional.empty());
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!getStack().isEmpty()) {
            nbt.put("stack", getStack().encode(getWorld().getRegistryManager()));
        }
        getMasterId().ifPresent(owner -> nbt.putUuid("owner", owner));
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof LevitatingItemEntity l) {
            setSlot(l.getSlot());
        }
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return getStack().contains(DataComponentTypes.FIRE_RESISTANT) || super.isFireImmune();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damage) {
        return !isInvulnerable() && super.isInvulnerableTo(damage);
    }

    @Override
    public boolean isInvulnerable() {
        return age < 50 || super.isInvulnerable();
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        PlayerEntity master = getMaster();
        if (master != null) {
            Pony.of(master).getLevitatingItems().onEntitySpawned(this);
        }
    }
}
