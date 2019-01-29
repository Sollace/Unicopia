package com.minelittlepony.unicopia.entity;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;

public class EntityWildCloud extends EntityCloud {

    public static final SpawnListEntry SPAWN_ENTRY_LAND = new SpawnListEntry(EntityWildCloud.class, 1, 1, 15);
    public static final SpawnListEntry SPAWN_ENTRY_OCEAN = new SpawnListEntry(EntityWildCloud.class, 1, 1, 7);

    public EntityWildCloud(World world) {
		super(world);

		preventEntitySpawning = true;
	}

    @Override
    public boolean isNotColliding() {
        AxisAlignedBB boundingbox = getEntityBoundingBox();

    	return checkNoEntityCollision(boundingbox, this)
	            && world.canBlockSeeSky(new BlockPos(this))
    	        && world.getCollisionBoxes(this, boundingbox).isEmpty()
    	        && !world.containsAnyLiquid(boundingbox);
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
        int count = 0;

        BlockPos pos = new BlockPos(this);

        Chunk chunk = world.getChunk(pos);
        for (ClassInheritanceMultiMap<Entity> i : chunk.getEntityLists()) {
            Iterator<EntityCloud> iter = i.getByClass(EntityCloud.class).iterator();
            while (iter.hasNext()) {
                iter.next();

                if (count++ > 2) {
                    return false;
                }
            }
        }

        return world.getBlockState(pos.down()).canEntitySpawn(this);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData pack) {
        if (!(pack instanceof PackData)) {
            float minSpawnHeight = getMinimumFlyingHeight();

            altitude = getRandomFlyingHeight();

            if (posY < minSpawnHeight) {
                minSpawnHeight += world.rand.nextInt(Math.max(1,  (int)getMaximumFlyingHeight() - (int)minSpawnHeight));

                setLocationAndAngles(posX, minSpawnHeight - 1, posZ, rotationYaw, rotationPitch);
                collideWithNearbyEntities();
            }

            pack = new PackData(this);
        } else {
            PackData packData = (PackData)pack;
            altitude = packData.leader.altitude;

            Vec3d position = packData.getUnOccupiedPosition(getCloudSize());

            setLocationAndAngles(position.x, position.y, position.z, packData.leader.rotationYaw, packData.leader.rotationPitch);
            collideWithNearbyEntities();
        }

    	return super.onInitialSpawn(difficulty, pack);
    }

    static class PackData implements IEntityLivingData {

        EntityCloud leader;

        PackData(EntityCloud leader) {
            this.leader = leader;
        }

        Vec3d getUnOccupiedPosition(int size) {
            return leader.getPositionVector();
        }
    }
}
