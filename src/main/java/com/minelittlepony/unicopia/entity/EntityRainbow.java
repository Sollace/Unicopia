package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;

public class EntityRainbow extends EntityWeatherEffect {

    public static final SpawnListEntry SPAWN_ENTRY = new SpawnListEntry(EntityRainbow.Spawner.class, 1, 1, 1);

    private int ticksAlive;

    private double radius;

    public static final int RAINBOW_MAX_SIZE = 180;
    public static final int RAINBOW_MIN_SIZE = 50;

    public EntityRainbow(World world) {
        this(world, 0, 0, 0);
    }

    public EntityRainbow(World world, double x, double y, double z) {
        super(world);

        float yaw = (int)MathHelper.nextDouble((world == null ? rand : world.rand), 0, 360);

        setLocationAndAngles(x, y, z, yaw, 0);

        radius = MathHelper.nextDouble(world == null ? rand : world.rand, RAINBOW_MIN_SIZE, RAINBOW_MAX_SIZE);
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
    public void setPosition(double x, double y, double z) {
        posX = x;
        posY = y;
        posZ = z;

        setEntityBoundingBox(new AxisAlignedBB(
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
    public void onUpdate() {
        super.onUpdate();

        if (ticksAlive-- <= 0) {
            setDead();
        }
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    public static class Spawner extends EntityLiving {

        public static final AxisAlignedBB SPAWN_COLLISSION_RADIUS = new AxisAlignedBB(
                -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE, -RAINBOW_MAX_SIZE,
                 RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE,  RAINBOW_MAX_SIZE
         );

        public Spawner(World worldIn) {
            super(worldIn);
            this.setInvisible(true);
        }

        @Override
        public boolean getCanSpawnHere() {
            if (super.getCanSpawnHere()) {
                return world.getEntitiesWithinAABB(EntityRainbow.class, SPAWN_COLLISSION_RADIUS.offset(getPosition())).size() == 0;
            }

            return false;
        }

        @Override
        public int getMaxSpawnedInChunk() {
            return 1;
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            setDead();

            EntityRainbow rainbow = new EntityRainbow(world);
            rainbow.setPosition(posX, posY, posZ);
            world.spawnEntity(rainbow);
        }
    }
}
