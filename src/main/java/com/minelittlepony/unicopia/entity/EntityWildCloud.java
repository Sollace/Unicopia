package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;

public class EntityWildCloud extends EntityCloud {

    public static final SpawnListEntry SPAWN_ENTRY_LAND = new SpawnListEntry(EntityWildCloud.class, 1, 1, 3);
    public static final SpawnListEntry SPAWN_ENTRY_OCEAN = new SpawnListEntry(EntityWildCloud.class, 1, 1, 2);

    public EntityWildCloud(World world) {
		super(world);
	}

    @Override
    public boolean isNotColliding() {
    	AxisAlignedBB boundingbox = getEntityBoundingBox();
    	return checkNoEntityCollision(boundingbox, this) && world.getCollisionBoxes(this, boundingbox).isEmpty() && !world.containsAnyLiquid(boundingbox);
    }

    /**
     * Returns true if there are no solid, live entities in the specified AxisAlignedBB, excluding the given entity
     *
     * @ref World.checkNoEntityCollision(AxisAlignedBB area, Entity entity)
     */
    public boolean checkNoEntityCollision(AxisAlignedBB area, Entity entity) {

        for (Entity i : world.getEntitiesWithinAABBExcludingEntity(entity, area)) {
            if (!i.isDead && (i.preventEntitySpawning || i instanceof EntityCloud) && (!entity.isRiding() || !entity.isRidingOrBeingRiddenBy(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this).down();

        return world.getBlockState(pos).canEntitySpawn(this);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        float minSpawnHeight = getMinimumFlyingHeight();

        altitude = getRandomFlyingHeight();

        if (posY < minSpawnHeight) {
            minSpawnHeight += world.rand.nextInt(Math.max(1,  (int)getMaximumFlyingHeight() - (int)minSpawnHeight));

            setLocationAndAngles(posX, minSpawnHeight - 1, posZ, rotationYaw, rotationPitch);
            collideWithNearbyEntities();
        }

    	return super.onInitialSpawn(difficulty, livingdata);
    }
}
