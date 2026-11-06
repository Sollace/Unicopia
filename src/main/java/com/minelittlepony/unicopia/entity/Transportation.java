package com.minelittlepony.unicopia.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.FlyingVehicleEntity;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class Transportation<T extends LivingEntity> implements Tickable {

    private final Living<T> living;

    @Nullable
    private FlyingVehicleEntity vehicle;

    private int ticksInVehicle;

    @Nullable
    private Vec3d lastVehiclePosition;

    Transportation(Living<T> living) {
        this.living = living;
    }

    public void setVehicle(@Nullable FlyingVehicleEntity vehicle) {
        this.vehicle = vehicle;
        lastVehiclePosition = vehicle == null ? null : vehicle.getPos();
    }

    @Override
    public void tick() {
        if (vehicle != null) {
            ticksInVehicle++;
        } else {
            ticksInVehicle = 0;
        }

        if (ticksInVehicle > 20 && vehicle instanceof AirBalloonEntity b
                && !b.isOnGround() && b.hasBalloon() && b.hasBurner() && b.isAscending()) {
            UCriteria.RIDE_BALLOON.trigger(living.asEntity());
        }
    }

    public void updatePreviousPosition() {
        lastVehiclePosition = vehicle == null ? Vec3d.ZERO : vehicle.getPos();
    }

    public void tickMovement() {
        if (vehicle == null) {
            setVehicle(findVehicle());
            if (vehicle == null) {
                return;
            }
        }
        Entity entity = living.asEntity();

        @Nullable
        Box constraintBox = getContraintBox(vehicle, entity);
        if (constraintBox == null) {
            setVehicle(null);
            Unicopia.LOGGER.info("Passenger disconnected from vehicle");
            return;
        }

        if (!entity.noClip) {
            Vec3d movement = lastVehiclePosition == null ? vehicle.getVelocity() : vehicle.getPos().subtract(lastVehiclePosition);
            Unicopia.LOGGER.info("[{}] Passenger Moving Self {}", vehicle.getWorld().isClient ? "CLIENT" : "SERVER", entity.getName());
            moveWithinVehicle(vehicle, entity, movement);
        }

        lastVehiclePosition = vehicle.getPos();
    }

    public Vec3d beforeMove(Vec3d velocity, MovementType movementType) {
        if (vehicle == null) {
            return velocity;
        }

        Entity entity = living.asEntity();

        if (!entity.noClip) {
            //Vec3d movement = lastVehiclePosition == null ? Vec3d.ZERO : vehicle.getPos().subtract(lastVehiclePosition);
            //moveWithinVehicle(vehicle, entity, movement);
        }
        return velocity;
    }

    public void afterMove(MovementType movementType) {
        if (vehicle == null) {
            return;
        }

        Entity entity = living.asEntity();

        if (!entity.noClip) {
            //Vec3d movement = lastVehiclePosition == Vec3d.ZERO ? Vec3d.ZERO : vehicle.getPos().subtract(lastVehiclePosition);
            //moveWithinVehicle(vehicle, entity, movement);
        }
    }

    public static void moveWithinVehicle(FlyingVehicleEntity vehicle, Entity passenger, Vec3d movement) {
        @Nullable
        Box constraintBox = getContraintBox(vehicle, passenger);
        if (constraintBox == null) {
            return;
        }

        List<VoxelShape> shapes = new ArrayList<>();
        vehicle.getCollissionShapes(ShapeContext.of(passenger), shapes::add);
        //movement = Entity.adjustMovementForCollisions(passenger, movement, passenger.getBoundingBox(), vehicle.getWorld(), shapes);

        Vec3d newPos = passenger.getPos().add(movement);

        if (!vehicle.isOnGround()) {

            // surface check to prevent the player from floating
            if (newPos.getY() > constraintBox.minY + 0.1 || newPos.getY() < constraintBox.minY + 0.1) {
               // newPos = new Vec3d(newPos.getX(), constraintBox.minY + 0.01, newPos.getZ());
            }

            // containment checks to prevent the player from falling out of the basket when in flight
            if (passenger.getPos().getY() < vehicle.getY()) {
                double maxDeviation = 0.1;
                double z = MathHelper.clamp(newPos.getZ(), constraintBox.minZ + maxDeviation, constraintBox.maxZ - maxDeviation);
                double x = MathHelper.clamp(newPos.getX(), constraintBox.minX + maxDeviation, constraintBox.maxX - maxDeviation);

                //newPos = new Vec3d(x, constraintBox.maxY, z);
            }
        }

        passenger.setPosition(newPos);
        passenger.updateTrackedPosition(newPos.getX(), newPos.getY(), newPos.getZ());
    }

    public Optional<BlockState> getSteppingState(BlockPos pos) {
        Entity entity = living.asEntity();
        Box boundingBox = entity.getBoundingBox().stretch(entity.getVelocity()).expand(0.1);
        if (vehicle == null) {
            return Optional.empty();
        }

        setOnGround();
        return vehicle.getSteppingState(boundingBox);
    }

    private FlyingVehicleEntity findVehicle() {
        Entity entity = living.asEntity();
        return living.asWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.5), e -> e instanceof FlyingVehicleEntity)
                .stream()
                .map(v -> (FlyingVehicleEntity)v)
                .findFirst().orElse(null);
    }

    private void setOnGround() {
        if (vehicle == null) {
            return;
        }
        Entity entity = living.asEntity();
        entity.setOnGround(true);
        entity.onLanding();
        entity.verticalCollision = true;
        entity.groundCollision = getVehicleRelativeVelocity().getY() < 0;
    }

    private Vec3d getVehicleRelativeVelocity() {
        return living.asEntity().getVelocity().subtract(vehicle.getVelocity());
    }

    @Nullable
    private static Box getContraintBox(FlyingVehicleEntity vehicle, Entity passenger) {
        Box entityBox = passenger.getBoundingBox().stretch(passenger.getVelocity()).expand(0.1, 1, 0.1);
        for (Box box : vehicle.getGravityZoneBoxes()) {
            if (entityBox.intersects(box.stretch(vehicle.getVelocity()))) {
                return box;
            }
        }

        return null;
    }
}
