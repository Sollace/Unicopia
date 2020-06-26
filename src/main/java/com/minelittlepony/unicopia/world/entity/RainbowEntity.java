package com.minelittlepony.unicopia.world.entity;

import com.minelittlepony.unicopia.InAnimate;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpawnRainbow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome.SpawnEntry;

public class RainbowEntity extends MobEntity implements InAnimate {

    public static final SpawnEntry SPAWN_ENTRY = new SpawnEntry(UEntities.RAINBOW, 1, 1, 1);

    private int ticksAlive;

    private final double radius;

    public static final int RAINBOW_MAX_SIZE = 180;
    public static final int RAINBOW_MIN_SIZE = 50;

    public static final Box SPAWN_COLLISSION_RADIUS = new Box(
            -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE,
             RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE
     ).expand(RAINBOW_MAX_SIZE);


    public RainbowEntity(EntityType<RainbowEntity> type, World world) {
        super(type, world);

        float yaw = (int)MathHelper.nextDouble(random, 0, 360);

        updatePositionAndAngles(0, 0, 0, yaw, 0);

        radius = MathHelper.nextDouble(random, RAINBOW_MIN_SIZE, RAINBOW_MAX_SIZE);
        ticksAlive = 10000;

        ignoreCameraFrustum = true;

        calculateDimensions();
    }

    @Override
    public boolean canInteract(Race race) {
        return false;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);

        float width = getDimensions(getPose()).width;
        setBoundingBox(new Box(
                x - width, y - radius/2, z,
                x + width, y + radius/2, z
        ));
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.changing((float)getRadius(), (float)getRadius());
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public void tick() {
        super.tick();

        if (ticksAlive-- <= 0) {
            remove();
        }

        if (!removed) {
            Box bounds = SPAWN_COLLISSION_RADIUS.offset(getPos());

            world.getEntities(RainbowEntity.class, bounds, null).forEach(this::attackCompetitor);
        }
    }

    private void attackCompetitor(Entity other) {
        if (other != this) {
            other.remove();
        }
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason type) {

        Box bounds = SPAWN_COLLISSION_RADIUS.offset(getPos());

        return super.canSpawn(world, type)
                && world.getEntities(RainbowEntity.class, bounds, null).isEmpty();
    }

    @Override
    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag var1) {
    }

    @Override
    public void writeCustomDataToTag(CompoundTag var1) {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SPAWN_RAINBOW.toPacket(new MsgSpawnRainbow(this));
    }
}
