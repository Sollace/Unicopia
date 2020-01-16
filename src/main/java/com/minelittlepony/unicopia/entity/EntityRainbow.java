package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Race;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.EntitySpawnGlobalS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ThreadExecutor;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityRainbow extends Entity implements IInAnimate {

    public static final SpawnListEntry SPAWN_ENTRY = new SpawnListEntry(EntityRainbow.Spawner.class, 1, 1, 1);

    private int ticksAlive;

    private double radius;

    public static final int RAINBOW_MAX_SIZE = 180;
    public static final int RAINBOW_MIN_SIZE = 50;

    public static final Box SPAWN_COLLISSION_RADIUS = new Box(
            -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE,
             RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE
     ).grow(RAINBOW_MAX_SIZE);



    public EntityRainbow(World world) {
        this(world, 0, 0, 0);
    }

    public EntityRainbow(World world, double x, double y, double z) {
        super(world);

        float yaw = (int)MathHelper.nextDouble((world == null ? rand : world.random), 0, 360);

        setLocationAndAngles(x, y, z, yaw, 0);

        radius = MathHelper.nextDouble(world == null ? rand : world.random, RAINBOW_MIN_SIZE, RAINBOW_MAX_SIZE);
        ticksAlive = 10000;

        ignoreFrustumCheck = true;

        width = (float)radius;
        height = width;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public boolean canInteract(Race race) {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        posX = x;
        posY = y;
        posZ = z;

        setBoundingBox(new Box(
                x - width, y - radius/2, z,
                x + width, y + radius/2, z
        ));
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
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
            Box bounds = SPAWN_COLLISSION_RADIUS.offset(getPosition());

            world.getEntities(EntityRainbow.class, bounds).forEach(this::attackCompetitor);
            world.getEntities(EntityRainbow.Spawner.class, bounds).forEach(this::attackCompetitor);
        }
    }

    private void attackCompetitor(Entity other) {
        if (other != this) {
            other.remove();
        }
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag var1) {
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag var1) {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new SpawnPacket(this);
    }

    public static class Spawner extends MobEntity {

        public Spawner(World worldIn) {
            super(worldIn);
            this.setInvisible(true);
        }

        @Override
        public boolean getCanSpawnHere() {
            Box bounds = SPAWN_COLLISSION_RADIUS.offset(getPos());

            return super.getCanSpawnHere()
                    && world.getEntities(EntityRainbow.class, bounds).isEmpty()
                    && world.getEntities(EntityRainbow.Spawner.class, bounds).isEmpty();
        }

        @Override
        public int getMaxSpawnedInChunk() {
            return 1;
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.dead) {
                remove();
                trySpawnRainbow();
            }
        }

        public void trySpawnRainbow() {
            EntityRainbow rainbow = new EntityRainbow(world);
            rainbow.setPosition(x, y, z);
            world.spawnEntity(rainbow);
        }
    }

    static class SpawnPacket extends EntitySpawnGlobalS2CPacket {
        public SpawnPacket(EntityRainbow entity) {
            super(entity);
        }

        @Override
        public void method_11188(ClientPlayPacketListener listener) {
            // TODO: Packet needs to be registered, and handling separated
            MinecraftClient client = MinecraftClient.getInstance();

            NetworkThreadUtils.forceMainThread(this, listener, client);
            double x = getX();
            double y = getY();
            double z = getZ();
            LightningEntity entity = new LightningEntity(client.world, x, y, z, false);
            entity.updateTrackedPosition(x, y, z);
            entity.yaw = 0;
            entity.pitch = 0;
            entity.setEntityId(getId());
            client.world.addLightning(entity);
        }
    }
}
