package com.minelittlepony.unicopia.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityWildCloud extends EntityCloud {

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
    	checkLocation();
    	return super.onInitialSpawn(difficulty, livingdata);
    }
}
