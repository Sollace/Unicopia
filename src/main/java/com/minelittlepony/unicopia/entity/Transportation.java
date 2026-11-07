package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.FlyingVehicleEntity;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Transportation<T extends Entity> implements Tickable {
    private final T entity;

    @Nullable
    private UUID lastVehicleUuid;
    @Nullable
    private FlyingVehicleEntity vehicle;

    private Box groundBox = null;

    private int ticksInVehicle;

    private final Stack<Boolean> inVehicleTick = new Stack<>();

    private boolean wasNoGravity;

    public Transportation(T entity) {
        this.entity = entity;
    }

    public boolean hasVehicle() {
        return vehicle != null && !vehicle.isOnGround();
    }

    public boolean canTick() {
        if (entity instanceof PlayerEntity && vehicle == null) {
            //Unicopia.LOGGER.info(entity.age + " Normal player tick");
        }
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
        // TODO: Figure out a better way of fixing this... I'm tired. I want to go to bed...
        if (entity instanceof PlayerEntity) {
            if (vehicle != null && !vehicle.isOnGround()) {
                wasNoGravity = entity.hasNoGravity();
                entity.setNoGravity(true);
            } else {
                entity.setNoGravity(wasNoGravity);
            }
        }
        groundBox = null;
        if (vehicle != null) {
            Box box = entity.getBoundingBox().expand(MathHelper.EPSILON);
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
        if (hasVehicle() && !vehicle.getUuid().equals(lastVehicleUuid)) {
            lastVehicleUuid = vehicle.getUuid();
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
                entity.setPos(entity.getX(), groundBox.maxY, entity.getZ());
            }
        }
    }

    public void tickRiding() {
        if (!hasVehicle()) {
            return;
        }
        int count = inVehicleTick.size();
        try {
            inVehicleTick.push(true);

            Vec3d pos = new Vec3d(entity.lastX, entity.lastY, entity.lastZ);

            if (entity instanceof ServerPlayerEntity player) {
                //player.networkHandler.syncWithPlayerPosition();
                player.getServerWorld().getChunkManager().updatePosition(player);
                //player.setMovement(Vec3d.ZERO);
                Unicopia.LOGGER.info("Player Pos: " + entity.getPos().subtract(pos));
            } else {

            }
            var oldPos = vehicle.getPos();
            vehicle.setPos(0, -100, 0);
            InteractionManager.getInstance().tickEntity(entity);
            vehicle.setPos(oldPos.x, oldPos.y, oldPos.z);

            if (groundBox != null) {
                entity.setVelocity(entity.getVelocity().multiply(1, 0.3, 1));
                Box entityBox = entity.getBoundingBox();
                if (entityBox.minY < groundBox.maxY) {
                    entity.setPos(entity.getX(), groundBox.maxY + 0.001, entity.getZ());
                }
                if (entityBox.minY > groundBox.maxY) {
                    entity.addVelocity(0, -0.001, 0);
                }
            }

            entity.lastX = entity.lastRenderX = pos.x;
            entity.lastY = entity.lastRenderY = pos.y;
            entity.lastZ = entity.lastRenderZ = pos.z;
        } finally {
            if (count < inVehicleTick.size()) {
                Preconditions.checkState(inVehicleTick.pop(), "Vehicle tick recursion stack got corrupted");
            }
        }
    }

    public void tickMovement() {
        if (!hasVehicle()) {
            return;
        }
        if (groundBox != null) {
            Box entityBox = entity.getBoundingBox();
            if (entityBox.minY < groundBox.maxY) {
                entity.setPos(entity.getX(), groundBox.maxY + 0.001, entity.getZ());
                entity.setVelocity(entity.getVelocity().multiply(1, 0, 1));
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
        if (!hasVehicle()) {
            return;
        }

        if (groundBox != null) {
            Box entityBox = entity.getBoundingBox();
            if (entityBox.minY < groundBox.maxY) {
                entity.setPos(entity.getX(), groundBox.maxY + 0.001, entity.getZ());
                entity.setVelocity(entity.getVelocity().multiply(1, 0, 1));
            }
        }
    }

    public Optional<BlockState> getSteppingState() {
        if (vehicle != null) {
            setOnGround();
            return vehicle.getSteppingState(entity.getBoundingBox().expand(MathHelper.EPSILON));
        }

        return Optional.empty();
    }

    private void setOnGround() {
        entity.setOnGround(true);
        entity.onLanding();
        entity.verticalCollision = true;
        entity.groundCollision = entity.getVelocity().getY() < 0;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Transportation<T> of(T entity) {
        return ((Holder<T>)entity).get();
    }

    public interface Holder<T extends Entity> {
        Transportation<T> get();
    }
}
