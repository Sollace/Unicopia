package com.minelittlepony.unicopia.entity;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.event.GameEvent;

public class Transportation<T extends LivingEntity> implements Tickable {

    private final Living<T> living;

    @Nullable
    private MultiBoundingBoxEntity vehicle;
    @Nullable
    private Entity vehicleEntity;
    @Nullable
    private Box vehicleBox;

    private int ticksInVehicle;

    private Vec3d lastVehiclePosition = Vec3d.ZERO;

    Transportation(Living<T> living) {
        this.living = living;
    }

    public <E extends Entity & MultiBoundingBoxEntity> void setVehicle(@Nullable E vehicle) {
        this.vehicle = vehicle;
        this.vehicleEntity = vehicle;
        updatePreviousPosition();
    }

    @Override
    public void tick() {
        if (vehicle != null) {
            ticksInVehicle++;
        } else {
            ticksInVehicle = 0;
        }

        if (ticksInVehicle > 20 && vehicle instanceof AirBalloonEntity) {
            UCriteria.RIDE_BALLOON.trigger(living.asEntity());
        }
    }

    public void updatePreviousPosition() {
        vehicleBox = getVehicleBox();
        lastVehiclePosition = vehicleEntity == null ? Vec3d.ZERO : vehicleEntity.getPos();
        Entity entity = living.asEntity();
        if (vehicleBox != null && living.asEntity().getBoundingBox().intersects(vehicleBox.expand(0.001, 0.5001, 0.001))) {
            entity.setOnGround(true);
            entity.onLanding();
            entity.verticalCollision = true;
            entity.groundCollision = true;
            entity.velocityDirty = true;
            entity.velocityModified = true;
        }
    }

    public void onMove(MovementType movementType) {
        if (vehicleBox == null || vehicleEntity == null) {
            return;
        }

        Entity entity = living.asEntity();

        Box passengerBox = entity.getBoundingBox().expand(0.001);
        Vec3d vehicleMovement = vehicleEntity.getPos().subtract(lastVehiclePosition);

        List<VoxelShape> shapes = new ArrayList<>();
        vehicle.getCollissionShapes(ShapeContext.of(entity), shapes::add);
        vehicleMovement = vehicleMovement.add(vehicleEntity.getVelocity());
        vehicleMovement = Entity.adjustMovementForCollisions(entity, vehicleMovement, passengerBox, entity.getWorld(), shapes);

        Vec3d newPos = entity.getPos().add(vehicleMovement);

        if (!vehicleEntity.isOnGround()) {
            // surface check to prevent the player from floating
            if (newPos.getY() > vehicleBox.minY + 0.1 || newPos.getY() < vehicleBox.minY + 0.1) {
                newPos = new Vec3d(newPos.getX(), vehicleBox.minY + 0.01, newPos.getZ());
            }
            // containment checks to prevent the player from falling out of the basket when in flight
            if (newPos.getY() < vehicleEntity.getPos().getY() + 3) {
                double maxDeviation = 0.1;
                double z = MathHelper.clamp(newPos.getZ(), vehicleBox.minZ + maxDeviation, vehicleBox.maxZ - maxDeviation);
                double x = MathHelper.clamp(newPos.getX(), vehicleBox.minX + maxDeviation, vehicleBox.maxX - maxDeviation);

                newPos = new Vec3d(x, newPos.getY(), z);
            }

            entity.setPosition(newPos);
            entity.updateTrackedPosition(newPos.x, newPos.y, newPos.z);
            entity.setVelocity(Vec3d.ZERO);
        }

        entity.setOnGround(true);
        entity.onLanding();
        entity.verticalCollision = true;
        entity.groundCollision = true;

        if (entity.distanceTraveled > ((EntityDuck)entity).getNextStepSoundDistance()) {
            entity.distanceTraveled -= 0.5;
            entity.playSound(vehicle.getWalkedOnSound(entity.getY()), 0.5F, 1);
            if (!entity.isSneaky()) {
                entity.getWorld().emitGameEvent(entity, GameEvent.STEP, entity.getBlockPos());
            }
        }
    }

    @Nullable
    private Box getVehicleBox() {
        if (!EntityPredicates.EXCEPT_SPECTATOR.test(living.asEntity())) {
            return null;
        }
        if (vehicle == null) {
            return null;
        }

        Box entityBox = living.asEntity().getBoundingBox().stretch(living.asEntity().getVelocity());
        for (Box box : vehicle.getGravityZoneBoxes()) {
            if (entityBox.intersects(box.expand(0.001).stretch(vehicleEntity.getVelocity().multiply(1)))) {
                return box;
            }
        }

        setVehicle(null);
        return null;
    }
}
