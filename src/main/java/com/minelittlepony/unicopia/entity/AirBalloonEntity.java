package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class AirBalloonEntity extends FlyingEntity {

    public AirBalloonEntity(EntityType<? extends AirBalloonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    public void tick() {

        if (!world.isClient) {

            float xSpeed = 0;//-0.015F * (this.age % 1000 < 500 ? -1 : 1);

            addVelocity(xSpeed, 0, 0);
        }


        for (var e : this.world.getOtherEntities(this, getBoundingBox().expand(0.2, 1.0E-7, 0.2))) {
            if (!(e instanceof PlayerEntity)) {
                e.setVelocity(e.getVelocity().multiply(0.3).add(getVelocity().multiply(0.84)));

                double diff = (getBoundingBox().maxY + getVelocity().y) - e.getBoundingBox().minY;

                if (diff > 0) {
                    e.addVelocity(0, diff, 0);
                }
            }

            e.distanceTraveled = 0;
            e.horizontalSpeed = 0;
            if (e instanceof LivingEntity l) {
                l.limbAngle = 0;
                l.limbDistance = 0;
            }
        }

        super.tick();

    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        player.setVelocity(player.getVelocity().multiply(0.9).add(getVelocity().multiply(0.56)));

        double diff = (getBoundingBox().maxY + getVelocity().y) - player.getBoundingBox().minY;

        if (diff > 0) {
            player.addVelocity(0, diff, 0);
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
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
    }

}
