package com.minelittlepony.unicopia.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;

public class EntityWildCloud extends EntityCloud {

    public static final SpawnListEntry SPAWN_ENTRY_LAND = new SpawnListEntry(EntityWildCloud.class, 3, 2, 5);
    public static final SpawnListEntry SPAWN_ENTRY_OCEAN = new SpawnListEntry(EntityWildCloud.class, 3, 1, 2);

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
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, area);
        for (Entity i : list) {
            if (!i.isDead && (i.preventEntitySpawning || i instanceof EntityCloud) && i != entity && (!entity.isRiding() || !entity.isRidingOrBeingRiddenBy(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        float minSpawnHeight = world.provider.getAverageGroundLevel() + 18;

        if (posY < minSpawnHeight) {
            minSpawnHeight += world.rand.nextInt(Math.max(1,  world.provider.getActualHeight() - (int)minSpawnHeight));

            setLocationAndAngles(posX, minSpawnHeight - 1, posZ, rotationYaw, rotationPitch);
            collideWithNearbyEntities();
        }

    	return super.onInitialSpawn(difficulty, livingdata);
    }
}
