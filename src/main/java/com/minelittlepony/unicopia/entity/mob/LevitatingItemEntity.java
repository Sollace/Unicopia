package com.minelittlepony.unicopia.entity.mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.entity.Trap;
import com.minelittlepony.unicopia.entity.player.LevitatedItemsInventory;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class LevitatingItemEntity extends Entity implements Owned<PlayerEntity>, MagicImmune, Trap {
    private static final TrackedData<ItemStack> STACK = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Integer> SLOT = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<UUID>> OWNER_ID = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Optional<BlockPos>> MINING_POS = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Direction> MINING_FACE = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.FACING);
    private static final TrackedData<Integer> HOLDING_POSITION = DataTracker.registerData(LevitatingItemEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private Vec3d polarPositionOffset = Vec3d.ZERO;
    private Vec3d manualPositionOffset = Vec3d.ZERO;
    @Nullable
    private BlockState miningBlockState;
    @Nullable
    private BlockPos interactingPos;
    @Nullable
    private Vec3d holdPosition;

    @Nullable
    private LevitatedItemsInventory.BlockBreakingRecord blockBreakingRecord;

    private Vec3d lerpPos = Vec3d.ZERO;
    private int lerpTicks;
    private int swingTicks;

    @Nullable
    private PlayerEntity master;

    @Nullable
    private List<Action> validActions;

    LevitatingItemEntity(EntityType<? extends LevitatingItemEntity> type, World world) {
        super(type, world);
    }

    public LevitatingItemEntity(int slot, PlayerEntity player, ItemStack stack) {
        this(UEntities.LEVITATING_ITEM, player.getWorld());
        setMaster(player);
        setStack(stack);
        setPosition(player.getPos());
        setPolarPositionOffset(new Vec3d(-0.5F, 0, 1.5).add(VecHelper.gaussian(getWorld().random).multiply(0.1)));
        setSlot(slot);
    }

    @Override
    protected void initDataTracker(Builder builder) {
        builder.add(STACK, ItemStack.EMPTY);
        builder.add(OWNER_ID, Optional.empty());
        builder.add(SLOT, 0);
        builder.add(MINING_POS, Optional.empty());
        builder.add(MINING_FACE, Direction.UP);
        builder.add(HOLDING_POSITION, 0);
    }

    public void setSlot(int slot) {
        dataTracker.set(SLOT, slot);
    }

    public int getSlot() {
        return dataTracker.get(SLOT);
    }

    public void setPolarPositionOffset(Vec3d newOffset) {
        polarPositionOffset = newOffset;
    }

    public void setManualPositionOffset(Vec3d newOffset) {
        manualPositionOffset = newOffset;
    }

    public void setHoldingPosition(@Nullable Vec3d pos) {
        holdPosition = pos;
        dataTracker.set(HOLDING_POSITION, holdPosition == null ? 0 : 1);
    }

    public void setForcedHoldingPosition(boolean holding) {
        setHoldingPosition(holding ? getPos() : null);
        dataTracker.set(HOLDING_POSITION, holding ? 3 : 2);
    }

    public boolean isHoldingPosition() {
        return dataTracker.get(HOLDING_POSITION) % 2 == 1;
    }

    public boolean canChangePositionHoldingFreely() {
        return dataTracker.get(HOLDING_POSITION) > 1;
    }

    public Vec3d getRelativePosition() {
        return polarPositionOffset;
    }

    public Optional<BlockPos> getMiningPos() {
        return dataTracker.get(MINING_POS);
    }

    public Direction getMiningFace() {
        return dataTracker.get(MINING_FACE);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        var myDimensions = super.getDimensions(pose);
        if (this.hasPassengers()) {
            var dimensions = getPassengerList().getFirst().getDimensions(pose);
            return new EntityDimensions(
                    Math.max(dimensions.width() + 0.1F, myDimensions.width()),
                    Math.max(dimensions.height() * 0.7F, myDimensions.height()),
                    myDimensions.eyeHeight(), myDimensions.attachments(), false);
        }
        return myDimensions;
    }

    public Action getDefaultAction() {
        return Action.GRAB;
    }

    public List<Action> getValidActions() {
        if (validActions == null) {
            validActions = List.copyOf(Util.make(new ArrayList<>(), a -> recomputeValidActions(a::add)));
        }

        return validActions;
    }

    protected void recomputeValidActions(Consumer<Action> collector) {
        if (!hasPassengers()) {
            collector.accept(Action.GRAB);
        }
        collector.accept(Action.MOVE);
        collector.accept(Action.DROP);
        collector.accept(isHoldingPosition() ? Action.FOLLOW : Action.TETHER);

        if (getMiningPos().isPresent()) {
            collector.accept(Action.STOP);
        }
    }

    public void handleAction(Action action, PlayerEntity player, Optional<Vec3d> direction) {
        switch (action) {
            case GRAB:
                player.giveItemStack(getStack());
                remove(RemovalReason.DISCARDED);
                playSound(SoundEvents.ENTITY_ITEM_PICKUP, 2, 1);
                break;
            case DROP:
                kill();
                break;
            case FOLLOW:
                setHoldingPosition(null);
                break;
            case MOVE:
                if (direction.isPresent()) {
                    Vec3d movement = direction.get().normalize().multiply(0.1F);
                    if (holdPosition != null) {
                        holdPosition = holdPosition.add(movement);
                    } else {
                        setManualPositionOffset(manualPositionOffset.add(movement));
                    }
                    move(MovementType.SELF, movement);
                }
                break;
            case MOVE_HORIZONTALLY:
                break;
            case MOVE_VERTICALLY:
                break;
            case STOP:
                getMiningPos().ifPresent(this::stopMining);
                break;
            case TETHER:
                setHoldingPosition(getPos());
                setForcedHoldingPosition(true);
                break;
        }
    }

    public void setMaster(PlayerEntity player) {
        dataTracker.set(OWNER_ID, Optional.of(player.getUuid()));
        master = player;
    }

    @Override
    public @Nullable PlayerEntity getMaster() {
        Optional<UUID> masterId = getMasterId();

        if (masterId.isEmpty()) {
            master = null;
            return null;
        }

        if (master == null || master.getUuid() != masterId.get()) {
            if (getWorld() instanceof ServerWorld sw) {
                master = masterId.map(sw.getServer().getPlayerManager()::getPlayer).orElse(null);
            } else {
                master = masterId.map(getWorld()::getPlayerByUuid).orElse(null);
            }
        }

        return master;
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

    public double getAttackDamage(PlayerEntity player) {
        AttributeModifiersComponent.Builder damageModifiersComponent = AttributeModifiersComponent.builder();
        getStack().applyAttributeModifier(AttributeModifierSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
                damageModifiersComponent.add(attribute, modifier, AttributeModifierSlot.MAINHAND);
            }
        });

        AttributeModifiersComponent damageModifiers = damageModifiersComponent.build();

        if (damageModifiers.modifiers().isEmpty()) {
            return 0;
        }
        return damageModifiers.applyOperations(player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), EquipmentSlot.MAINHAND);
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
            setHoldingPosition(getPos());
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
            setHoldingPosition(null);
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

        calculateDimensions();

        if (getStack().isEmpty() && getPassengerList().isEmpty()) {
            discard();
        }

        if (master.getWorld() instanceof ServerWorld sw) {
            if (master.getWorld() != getWorld()) {
                Vec3d newPos = master.getPos().add(polarPositionOffset);
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

            if (!master.shouldCancelInteraction() && holdPosition != null && blockBreakingRecord == null && squaredDistanceTo(master) < 4 && canChangePositionHoldingFreely()) {
                setHoldingPosition(null);
            }

            boolean isBeingLookedAt = Pony.of(master).isLookingAt(this) || isConnectedThroughVehicle(master);

            Vec3d targetPosition = isBeingLookedAt ? getPos().add(0, MathHelper.sin(age / 15F) * 0.02F, 0) : (holdPosition == null ? master.getEyePos().add(master.getRotationVector(
                    (float)polarPositionOffset.x * MathHelper.DEGREES_PER_RADIAN,
                    master.getBodyYaw() + (float)polarPositionOffset.z * MathHelper.DEGREES_PER_RADIAN
            )).add(manualPositionOffset) : holdPosition).add(0, MathHelper.sin(age / 15F) * 0.2F + 0.2F, 0);

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

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        if (!noClip) {
            if (hasPassengers()) {
                Entity passenger = getPassengerList().get(0);
                Box box = passenger.getBoundingBox();
                Vec3d adjustedMovement = Entity.adjustMovementForCollisions(passenger, movement, box, getWorld(), getWorld().getEntityCollisions(passenger, box.stretch(movement)));
                if (!adjustedMovement.equals(movement)) {
                    Vec3d downMove = movement.add(0, master.getY() - getY() - 1, 0);
                    adjustedMovement = Entity.adjustMovementForCollisions(passenger, downMove, box, getWorld(), getWorld().getEntityCollisions(passenger, box.stretch(downMove)));
                }

                movement = adjustedMovement;
            }
        }
        super.move(movementType, movement);
    }

    private float getSwingProgress() {
        if (getMiningPos().isPresent()) {
            return (age % 10F) / 10F;
        }

        if (isSwinging()) {
            swingTicks--;
            return MathHelper.clamp(Math.abs(swingTicks) / 10F, 0, 1);
        }

        return 0;
    }

    public boolean isSwinging() {
        return swingTicks > -10;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        InteractionManager.getInstance().interactLevitatingItem(this, player);
        return ActionResult.PASS;
    }

    @Override
    public boolean attemptDismount(Entity passenger) {
        if (!(passenger instanceof LivingEntity living)) {
            return true;
        }
        Pony master = Pony.of(getMaster());
        if (master == null) {
            return true;
        }
        playSound(USounds.ENTITY_PLAYER_KICK, 0.6F, (float)random.nextTriangular(0.6, 0.3));
        getWorld().syncWorldEvent(WorldEvents.BLOCK_WAXED, getBlockPos(), Block.getRawIdFromState(Blocks.SLIME_BLOCK.getDefaultState()));
        emitGameEvent(GameEvent.ENTITY_ACTION, passenger);
        master.asEntity().damage(passenger instanceof PlayerEntity player ? getDamageSources().playerAttack(player) : getDamageSources().mobAttack(living), 0.5F);
        return master.subtractEnergyCost(10);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (STACK.equals(data)) {
            getStack().setHolder(this);
        }

        if (HOLDING_POSITION.equals(data) || MINING_POS.equals(data)) {
            validActions = null;
        }

        if (getWorld().isClient) {
            if (OWNER_ID.equals(data) || SLOT.equals(data)) {
                if (master != null) {
                    Pony.of(master).getLevitatingItems().onEntityDespawned(this);
                }
                PlayerEntity master = getMaster();
                if (master != null) {
                    Pony.of(master).getLevitatingItems().onEntitySpawned(this);
                }
            }
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
        NbtList polarOffset = nbt.getList("polarPositionOffset", NbtElement.DOUBLE_TYPE);
        NbtList manualOffset = nbt.getList("manualPositionOffset", NbtElement.DOUBLE_TYPE);
        setPolarPositionOffset(NbtSerialisable.readPositionVector(polarOffset));
        setManualPositionOffset(NbtSerialisable.readPositionVector(manualOffset));
        if (nbt.contains("holdPosition", NbtElement.LIST_TYPE)) {
            setHoldingPosition(NbtSerialisable.readPositionVector(nbt.getList("holdPosition", NbtElement.DOUBLE_TYPE)));
        }
        setForcedHoldingPosition(holdPosition != null && nbt.getBoolean("movementRestricted"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!getStack().isEmpty()) {
            nbt.put("stack", getStack().encode(getWorld().getRegistryManager()));
        }
        getMasterId().ifPresent(owner -> nbt.putUuid("owner", owner));
        nbt.put("polarPositionOffset", toNbtList(polarPositionOffset.x, polarPositionOffset.y, polarPositionOffset.z));
        nbt.put("manualPositionOffset", toNbtList(manualPositionOffset.x, manualPositionOffset.y, manualPositionOffset.z));
        if (holdPosition != null) {
            nbt.put("holdPosition", toNbtList(holdPosition.x, holdPosition.y, holdPosition.z));
        }
        nbt.putBoolean("movementRestricted", !canChangePositionHoldingFreely());
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
        return hasPassengers() && super.shouldSave();
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
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entry) {
        PlayerEntity master = getMaster();
        return new EntitySpawnS2CPacket(this, entry, master == null ? 0 : master.getId());
    }

    @Override
    public void onRemoved() {
        @Nullable
        PlayerEntity master = getMaster();
        if (master != null) {
            Pony.of(master).getLevitatingItems().onEntityDespawned(this);
        }
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (getWorld().getEntityById(packet.getEntityData()) instanceof PlayerEntity m) {
            master = m;
        }
    }

    public enum Action {
        MOVE,
        MOVE_VERTICALLY,
        MOVE_HORIZONTALLY,
        DROP,
        GRAB,
        FOLLOW,
        TETHER,
        STOP;

        private final Text label = Text.translatable("entity.unicopia.levitating_item.action." + name().toLowerCase(Locale.ROOT));

        public int getU() {
            return (ordinal() % 4);
        }

        public int getV() {
            return ordinal() / 4;
        }

        public Text getLabel() {
            return label;
        }
    }
}
