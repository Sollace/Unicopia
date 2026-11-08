package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.FlyingVehicleEntity;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class Transportation implements Tickable, NbtSerialisable {
    static final Codec<RegistryKey<DimensionType>> DIMENSION_CODEC = RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE);
    private final Entity entity;

    @Nullable
    private FlyingVehicleEntity vehicle;
    @Nullable
    private UUID vehicleId;
    @Nullable
    private Vec3d relativePosition;
    @Nullable
    private RegistryKey<DimensionType> vehicleDimension;
    @Nullable
    private Box groundBox;

    @Nullable
    private Vec3d lastVehiclePos;

    private int ticksInVehicle;

    private final Stack<Boolean> inVehicleTick = new Stack<>();

    public Transportation(EntityConvertable<?> entity) {
        this.entity = entity.asEntity();
    }

    public boolean hasVehicle() {
        return vehicle != null && !vehicle.isOnGround();
    }

    @Nullable
    public Box getGroundBox() {
        return groundBox;
    }

    public boolean canTick() {
        restoreVehicle();
        return !hasVehicle() || (!inVehicleTick.isEmpty() && inVehicleTick.pop());
    }

    public boolean setVehicle(@Nullable FlyingVehicleEntity vehicle) {
        if (vehicle != null && (entity.isRemoved()
                || entity.noClip
                || entity.isSpectator()
                || (entity instanceof PlayerEntity ply && ply.getAbilities().flying)
                || entity.isConnectedThroughVehicle(vehicle))) {
            vehicle = null;
        }
        this.vehicle = vehicle;
        this.vehicleId = vehicle == null ? null : vehicle.getUuid();
        this.relativePosition = vehicle == null ? null : entity.getPos().subtract(vehicle.getPos());
        this.vehicleDimension = vehicle == null ? null : vehicle.getWorld().getDimensionEntry().getKey().orElse(null);

        groundBox = null;
        if (vehicle != null) {
            Box box = entity.getBoundingBox().expand(MathHelper.EPSILON).stretch(0, -2, 0);
            for (var zone : vehicle.getBoundingBoxes(vehicle.getBoundingBox())) {
                if (box.intersects(zone)) {
                    groundBox = zone;
                    break;
                }
            }
        }
        return vehicle != null;
    }

    @Override
    public void tick() {
        if (hasVehicle() && !vehicle.getUuid().equals(vehicleId)) {
            ticksInVehicle = 0;
        }
        ticksInVehicle = hasVehicle() ? (ticksInVehicle + 1) : 0;

        if (ticksInVehicle > 20 && vehicle instanceof AirBalloonEntity b
                && !b.isOnGround() && b.hasBalloon() && b.hasBurner() && b.isAscending()) {
            UCriteria.RIDE_BALLOON.trigger(entity);
        }
    }

    public void move(Vec3d positionChange) {
        if (!hasVehicle()) {
            return;
        }
        Vec3d newPos = entity.getPos().add(positionChange);
        if (groundBox != null) {
            newPos = new Vec3d(newPos.x, Math.max(newPos.y, groundBox.maxY), newPos.z);
        }

        entity.resetPosition();
        entity.move(MovementType.SELF, newPos.subtract(entity.getPos()));
        if (groundBox != null) {
            Box entityBox = entity.getBoundingBox();
            if (entityBox.minY < groundBox.maxY) {
                entity.setPosition(entity.getX(), groundBox.maxY, entity.getZ());
            }
        }

        lastVehiclePos = vehicle.getPos();
    }

    public void onPlayerTick(Runnable tickEntity) {
        if (vehicle == null) {
            tickEntity.run();
            return;
        }
        var oldPos = vehicle.getPos();
        vehicle.setPosition(0, -100, 0);
        tickEntity.run();
        vehicle.setPosition(oldPos.x, oldPos.y, oldPos.z);
        moveToSurface();
        restoreRelativePosition();
    }

    public void onPlayerMoved(PlayerMoveC2SPacket packet) {
        if (vehicle != null && lastVehiclePos != null) {
            Vec3d vehicleMotion = vehicle.getPos().subtract(lastVehiclePos);
            move(vehicleMotion);
        }
        moveToSurface();
    }

    public void tickRiding() {
        if (!hasVehicle()) {
            return;
        }
        int count = inVehicleTick.size();
        try {
            inVehicleTick.push(true);

            Vec3d pos = new Vec3d(entity.lastX, entity.lastY, entity.lastZ);
            var oldPos = vehicle.getPos();
            vehicle.setPosition(0, -100, 0);
            InteractionManager.getInstance().tickEntity(entity);
            vehicle.setPosition(oldPos.x, oldPos.y, oldPos.z);
            moveToSurface();
            updatePositionOffset();

            entity.lastX = entity.lastRenderX = pos.x;
            entity.lastY = entity.lastRenderY = pos.y;
            entity.lastZ = entity.lastRenderZ = pos.z;
        } finally {
            if (count < inVehicleTick.size()) {
                Preconditions.checkState(inVehicleTick.pop(), "Vehicle tick recursion stack got corrupted");
            }
        }
    }

    private void moveToSurface() {
        if (vehicle != null) {
            if (groundBox != null) {
                if (entity.getVelocity().y < 0) {
                    entity.setVelocity(entity.getVelocity().multiply(1, 0.999, 1));
                }
                Box entityBox = entity.getBoundingBox();
                if (entityBox.minY < groundBox.maxY) {
                    entity.setPosition(entity.getX(), groundBox.maxY, entity.getZ());
                }
            }
        }
    }

    private void updatePositionOffset() {
        relativePosition = entity.getPos().subtract(vehicle.getPos());
    }

    private void restoreRelativePosition() {
        if (relativePosition != null) {
            entity.setPosition(vehicle.getPos().add(relativePosition));
        }
    }

    public void tickMovement() {
        if (!hasVehicle()) {
            return;
        }
        if (groundBox != null) {
            Box entityBox = entity.getBoundingBox();
            if (entityBox.minY < groundBox.maxY) {
                entity.setPosition(entity.getX(), groundBox.maxY, entity.getZ());
            }
        }
    }

    public Vec3d beforeMove(Vec3d velocity, MovementType movementType) {
        if (!hasVehicle()) {
            return velocity;
        }

        Box entityBox = entity.getBoundingBox();
        if (groundBox != null && entityBox.minY + velocity.y < groundBox.maxY) {
            return new Vec3d(velocity.x, groundBox.maxY - entityBox.minY, velocity.z);
        }
        return velocity;
    }

    public void afterMove(MovementType movementType) {
        tickMovement();
    }

    public Optional<BlockState> getSteppingState() {
        if (vehicle != null) {
            setOnGround();
            return vehicle.getSteppingState(entity.getBoundingBox().expand(0.5));
        }

        return Optional.empty();
    }

    private void setOnGround() {
        entity.setOnGround(true);
        entity.onLanding();
        entity.verticalCollision = true;
        entity.groundCollision = entity.getVelocity().getY() < 0;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.putNullable("vehicle", Uuids.CODEC, vehicleId);
        compound.putNullable("relative_pos", CodecUtils.VECTOR, relativePosition);
        compound.putNullable("dimension", DIMENSION_CODEC, vehicleDimension);
        compound.putInt("ticks_riding", ticksInVehicle);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        vehicleId = compound.get("vehicle", Uuids.CODEC).orElse(null);
        relativePosition = compound.get("relative_pos", CodecUtils.VECTOR).orElse(null);
        vehicleDimension = compound.get("dimension", DIMENSION_CODEC).orElse(null);
        ticksInVehicle = compound.getInt("ticks_riding", ticksInVehicle);

        restoreVehicle();
    }

    private void restoreVehicle() {
        if (vehicleDimension != null && !entity.getWorld().getDimensionEntry().matchesKey(vehicleDimension)) {
            setVehicle(null);
            return;
        }
        if (vehicle == null && vehicleId != null) {
            @Nullable
            Entity restoredVehicle = entity.getWorld().getEntity(vehicleId);
            if (restoredVehicle instanceof FlyingVehicleEntity v) {
                restoreRelativePosition();
                setVehicle(v);
            }
        }
    }
}
