package com.minelittlepony.unicopia.entity;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.entity.collision.EntityCollisions;

public class AirBalloonEntity extends FlyingEntity implements EntityCollisions.ComplexCollidable {
    private static final byte HAS_BALLOON = 1;
    private static final byte HAS_BURNER = 2;
    private static final byte BURNER_ACTIVE = 4;
    private static final TrackedData<Integer> FLAGS = DataTracker.registerData(AirBalloonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private Vec3d prevVehicleVel = Vec3d.ZERO;
    private Vec3d velocityBeforeTick = Vec3d.ZERO;

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(FLAGS, 0);
    }

    public boolean hasBalloon() {
        return getFlag(HAS_BALLOON);
    }

    public void setHasBalloon(boolean hasBalloon) {
        setFlag(HAS_BALLOON, hasBalloon);
    }

    public boolean hasBurner() {
        return getFlag((byte)(HAS_BURNER | HAS_BALLOON));
    }

    public void setHasBurner(boolean hasBurner) {
        setFlag(HAS_BURNER, hasBurner);
    }

    public boolean isBurnerActive() {
        return getFlag((byte)(HAS_BURNER | BURNER_ACTIVE | HAS_BALLOON));
    }

    public void setBurnerActive(boolean burnerActive) {
        setFlag(BURNER_ACTIVE, burnerActive);
    }

    private boolean getFlag(byte flag) {
        return (dataTracker.get(FLAGS).intValue() & flag) == flag;
    }

    private void setFlag(byte flag, boolean val) {
        int v = dataTracker.get(FLAGS);
        dataTracker.set(FLAGS, val ? (v | flag) : (v & ~flag));
    }

    private boolean isAirworthy() {
        return hasBalloon() && isBurnerActive();
    }

    @Override
    public void tick() {
        prevVehicleVel = getVelocity();
        setAir(getMaxAir());

        if (isAirworthy()) {
            setVelocity(getVelocity()
                    .add(getWind(world, getBlockPos()))
                    .normalize()
                    .multiply(0.2)
                    .add(0, isBurnerActive() ? 0.00F : isTouchingWater() ? 0.02F : -0.06F, 0));
        } else {
            addVelocity(0, -0.03, 0);

            if (isSubmergedInWater()) {
                double yy = getVelocity().y;
                setVelocity(getVelocity().multiply(0.9, 0.4, 0.9).add(0, Math.abs(yy) / 2F, 0));
            }
        }

        if (isLeashed()) {
            Vec3d leashPost = getHoldingEntity().getPos();
            Vec3d pos = getPos();

            if (leashPost.distanceTo(pos) >= 5) {
                Vec3d newVel = leashPost.subtract(pos).multiply(0.01);
                setVelocity(newVel.lengthSquared() < 0.03 ? Vec3d.ZERO : newVel);
            }
        }

        super.tick();

        velocityBeforeTick = getVelocity();

        float weight = 0;

        if (velocityBeforeTick.length() > 0.01 && !isSubmergedInWater()) {
            Box box = getInteriorBoundingBox();

            for (Entity e : world.getOtherEntities(this, box.expand(-0.2, 1, -0.2))) {
                updatePassenger(e, box, !onGround);
                weight++;
            }

            if (hasBalloon()) {
                Box balloonBox = getBalloonBoundingBox();

                for (Entity e : world.getOtherEntities(this, balloonBox.expand(1.0E-7))) {
                    updatePassenger(e, balloonBox, false);
                }
            }
        }

        if (getVelocity().y > -0.6 && !isTouchingWater()) {
            if (isBurnerActive()) {
                weight -= 3;
            }
            addVelocity(0, MathHelper.clamp(-weight / 10F, -1, isLeashed() ? 0.2F : 1), 0);
        }
    }

    private void updatePassenger(Entity e, Box box, boolean checkBasket) {
        Vec3d pos = e.getPos();

        double xx = checkBasket ? MathHelper.clamp(pos.x, box.minX, box.maxX) : pos.x;
        double zz = checkBasket ? MathHelper.clamp(pos.z, box.minZ, box.maxZ) : pos.z;
        double yy = pos.y;

        Box entityBox = e.getBoundingBox();

        if ((Math.abs(velocityBeforeTick.y) > 0.0001F && entityBox.minY < box.maxY)
                || (entityBox.minY > box.maxY && entityBox.minY < box.maxY + 0.01)) {
           yy = box.maxY - Math.signum(velocityBeforeTick.y) * 0.01;
        }

        if (xx != pos.x || zz != pos.z || yy != pos.y) {
            e.setPos(xx + velocityBeforeTick.x, yy, zz + velocityBeforeTick.z);
        }

        Vec3d vel = e.getVelocity();
        if (vel.lengthSquared() >= prevVehicleVel.lengthSquared()) {
            vel = vel.subtract(prevVehicleVel);
        }

        e.setVelocity(vel.multiply(0.5).add(velocityBeforeTick.multiply(0.65)));

        Living.updateVelocity(e);
        if (e instanceof ServerPlayerEntity ply) {
            ply.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ply));
        }

        e.setOnGround(true);

        if (horizontalSpeed != 0) {
            e.distanceTraveled = 0;
            e.horizontalSpeed = 0;
            if (e instanceof LivingEntity l) {
                l.limbAngle = 0;
                l.limbDistance = 0;
            }
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
      //  updatePassenger(player, getInteriorBoundingBox(), false);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public void pushAway(Entity entity) {

    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (hasBalloon()) {
            return getBoundingBox().union(getBalloonBoundingBox());
        }
        return getBoundingBox();
    }

    protected Box getInteriorBoundingBox() {
        return getBoundingBox().contract(0.7, 0, 0.7);
    }

    protected Box getBalloonBoundingBox() {
        return getBoundingBox().offset(0.125, 11, 0).expand(2.25, 0, 2);
    }

    @Override
    public void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {

        Box box = getBoundingBox().expand(0.3, 0, 0.3);

        double wallheight = box.maxY + 0.7;
        double wallThickness = 0.7;


        // front left (next to door)
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness + 0.2, wallheight, box.minZ + wallThickness)));
        // front right (next to door)
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness - 0.2, box.minY, box.minZ, box.maxX, wallheight, box.minZ + wallThickness)));

        // back
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.maxZ - wallThickness, box.maxX, wallheight, box.maxZ)));

        // left
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness, box.minY, box.minZ, box.maxX, wallheight, box.maxZ)));
        // right
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness, wallheight, box.maxZ)));

        // top of balloon
        if (hasBalloon()) {
            output.accept(VoxelShapes.cuboid(getBoundingBox().offset(0.125, 6, 0).expand(2.25, 3, 2)));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setHasBalloon(compound.getBoolean("hasBalloon"));
        setHasBurner(compound.getBoolean("hasBurner"));
        setBurnerActive(compound.getBoolean("burnerActive"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("hasBalloon", hasBalloon());
        compound.putBoolean("hasBurner", hasBurner());
        compound.putBoolean("burnerActive", isBurnerActive());
    }

    static Vec3d getWind(World world, BlockPos pos) {
        return Vec3d.ofCenter(pos).normalize().multiply(1, 0, 1).multiply(0.002);//.multiply((world.getRandom().nextFloat() - 0.5) * 0.2);
    }
}









