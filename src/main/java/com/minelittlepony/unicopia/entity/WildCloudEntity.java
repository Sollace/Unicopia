package com.minelittlepony.unicopia.entity;

import java.util.Iterator;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnEntry;
import net.minecraft.world.chunk.WorldChunk;

public class WildCloudEntity extends CloudEntity {

    public static final SpawnEntry SPAWN_ENTRY_LAND = new SpawnEntry(UEntities.WILD_CLOUD, 1, 1, 15);
    public static final SpawnEntry SPAWN_ENTRY_OCEAN = new SpawnEntry(UEntities.WILD_CLOUD, 1, 1, 7);

    public WildCloudEntity(EntityType<WildCloudEntity> type, World world) {
        super(type, world);

        inanimate = true;
    }

    /*@Override
    public boolean isNotColliding() {
        Box boundingbox = getBoundingBox();

        return checkNoEntityCollision(boundingbox, this)
                && world.isSkyVisible(getBlockPos())
                && world.doesNotCollide(this, boundingbox)
                && !world.intersectsFluid(boundingbox);
    }*/

    /**
     * Returns true if there are no solid, live entities in the specified Box, excluding the given entity
     *
     * @ref World.checkNoEntityCollision(Box area, Entity entity)
     */
    /*public boolean checkNoEntityCollision(Box area, Entity entity) {
        ServerWorld s;
        ((ServerWorld)world).intersectsEntities(entity_1, voxelShape_1)

        for (Entity i : world.getEntities(entity, area)) {
            if (!i.removed && (i.inanimate || i instanceof CloudEntity) && (!entity.hasVehicle() || !entity.isConnectedThroughVehicle(i))) {
                return false;
            }
        }
        return true;
    }*/

    @Override
    public boolean canSpawn(IWorld world, SpawnType type) {
        if (type == SpawnType.NATURAL) {
            int count = 0;

            ChunkPos cpos = new ChunkPos(getBlockPos());

            WorldChunk chunk = world.getChunkManager().getWorldChunk(cpos.x, cpos.z, false);
            for (TypeFilterableList<Entity> i : chunk.getEntitySectionArray()) {
                Iterator<CloudEntity> iter = i.getAllOfType(CloudEntity.class).iterator();
                while (iter.hasNext()) {
                    iter.next();

                    if (count++ > 2) {
                        return false;
                    }
                }
            }
        }

        BlockPos pos = getBlockPos().down();
        return world.getBlockState(pos).allowsSpawning(world, pos, getType());
    }

    @Override
    public EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType type, @Nullable EntityData data, @Nullable CompoundTag tag) {
        data = super.initialize(world, difficulty, type, data, tag);

        if (!(data instanceof PackData)) {
            float minSpawnHeight = getMinimumFlyingHeight();

            targetAltitude = getRandomFlyingHeight();

            if (y < minSpawnHeight) {
                minSpawnHeight += random.nextInt(Math.max(1,  (int)getMaximumFlyingHeight() - (int)minSpawnHeight));

                setPositionAndAngles(x, minSpawnHeight - 1, z, yaw, pitch);
                moveToBoundingBoxCenter();
            }

            if (this.world.hasRain(getBlockPos())) {
                setIsRaining(true);
            }

            if (this.world.isThundering()) {
                setIsThundering(true);
            }

            data = new PackData(this);
        } else {
            PackData packData = (PackData)data;
            targetAltitude = packData.leader.targetAltitude;

            Vec3d position = packData.getUnOccupiedPosition(getCloudSize());

            setIsRaining(packData.leader.getIsRaining());
            setIsThundering(packData.leader.getIsThundering());

            setPositionAndAngles(position.x, position.y, position.z, packData.leader.yaw, packData.leader.pitch);
            checkBlockCollision();
        }

        return data;
    }

    static class PackData implements EntityData {

        final CloudEntity leader;

        PackData(CloudEntity leader) {
            this.leader = leader;
        }

        Vec3d getUnOccupiedPosition(int size) {
            return leader.getPos();
        }
    }
}
