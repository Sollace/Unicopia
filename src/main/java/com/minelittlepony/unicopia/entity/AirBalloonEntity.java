package com.minelittlepony.unicopia.entity;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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
        return getFlag((byte)(HAS_BURNER | BURNER_ACTIVE));
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
        return hasBalloon() && (!onGround || isBurnerActive());
    }

    @Override
    public void tick() {
        this.setHasBalloon(true);
        this.setHasBurner(true);
        this.setBurnerActive(false);

        setAir(getMaxAir());

        if (isAirworthy()) {
            setVelocity(getVelocity()
                    .add(getWind(world, getBlockPos()))
                    .normalize()
                    .multiply(0.2)
                    .add(0, isBurnerActive() ? 0.09F : isTouchingWater() ? 0.02F : -0.06F, 0));
        } else {
            addVelocity(0, isTouchingWater() ? 0.02F : -0.02F, 0);
        }

        if (isLeashed()) {
            Vec3d leashPost = getHoldingEntity().getPos();
            Vec3d pos = getPos();

            if (leashPost.distanceTo(pos) >= 5) {
                Vec3d newVel = leashPost.subtract(pos).multiply(0.01);
                setVelocity(newVel.lengthSquared() < 0.03 ? Vec3d.ZERO : newVel);
            }
        }

        if (age % 20 < 10) {
            if (getVelocity().y < 0.1) {
                addVelocity(0, 0.01, 0);
            }
        } else {
            if (getVelocity().y > -0.1) {
                addVelocity(0, -0.01, 0);
            }
        }

        //setVelocity(Vec3d.ZERO);
        float weight = 0;

        if (getVelocity().length() > 0) {
            Box box = getBoundingBox();
            for (var e : this.world.getOtherEntities(this, box.expand(1, 1.0E-7, 1))) {
                Vec3d vel = e.getVelocity();

                if (getVelocity().y > 0 && box.maxY > e.getBoundingBox().minY) {
                    e.setPosition(e.getX(), box.maxY + 0.1, e.getZ());
                }

                if (!(e instanceof PlayerEntity)) {
                    e.setVelocity(vel.multiply(0.3).add(getVelocity().multiply(0.786)));
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

                weight++;
            }

            Box balloonTopBox = getBoundingBox().offset(0.125, 11, 0).expand(2.25, 0, 2);

            for (var e : this.world.getOtherEntities(this, balloonTopBox.expand(1.0E-7))) {
                Vec3d vel = e.getVelocity();

                double yVel = vel.y + Math.max(balloonTopBox.maxY - e.getBoundingBox().minY, 0);
                yVel /= 8;
                yVel += 0.3;

                e.setVelocity(vel.getX(), yVel, vel.getZ());
                e.setVelocity(e.getVelocity().multiply(0.3).add(getVelocity().multiply(0.786)));
                e.setOnGround(true);
            }
        }

        if (getVelocity().y > -0.6 && !isTouchingWater()) {
            if (isBurnerActive()) {
                weight -= 3;
            }
            addVelocity(0, MathHelper.clamp(-weight / 10F, -1, isLeashed() ? 0.2F : 1), 0);
        }

        super.tick();
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (getVelocity().lengthSquared() > 0) {
           // player.setVelocity(getVelocity().multiply(1.3));
            player.setVelocity(player.getVelocity().multiply(0.3).add(getVelocity().multiply(
                    getVelocity().y < 0 ? 0.828 : 0.728
            )));

            double diff = (getBoundingBox().maxY + getVelocity().y) - player.getBoundingBox().minY;

            if (diff > 0) {
                player.addVelocity(0, diff, 0);
            }
        }
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
        return getBoundingBox().expand(30, 100, 30);
    }

    @Override
    public void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {

        Box box = getBoundingBox().expand(0.3, 0, 0.3);

        double wallheight = box.maxY + 1;
        double wallThickness = 0.7;

        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness + 0.2, wallheight, box.minZ + wallThickness)));
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness - 0.2, box.minY, box.minZ, box.maxX, wallheight, box.minZ + wallThickness)));
        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.maxZ - wallThickness, box.maxX, wallheight, box.maxZ)));

        output.accept(VoxelShapes.cuboid(new Box(box.minX, box.minY, box.minZ, box.minX + wallThickness, wallheight, box.maxZ)));
        output.accept(VoxelShapes.cuboid(new Box(box.maxX - wallThickness, box.minY, box.minZ, box.maxX, wallheight, box.maxZ)));

        // top of balloon
        if (hasBalloon()) {
            output.accept(VoxelShapes.cuboid(getBoundingBox().offset(0.125, 11, 0).expand(2.25, 0, 2)));
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
        return Vec3d.ofCenter(pos).normalize().multiply(1, 0, 1).multiply(0.2);//.multiply((world.getRandom().nextFloat() - 0.5) * 0.2);
    }
}









