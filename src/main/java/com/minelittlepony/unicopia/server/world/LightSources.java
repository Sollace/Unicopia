package com.minelittlepony.unicopia.server.world;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.DynamicLightSource;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class LightSources extends PersistentState {
    private static final Identifier ID = Unicopia.id("light_sources");

    private final Set<UUID> lightSourceIds = new HashSet<>();
    private final AbstractObject2IntMap<UUID> lightSourceClientIds = new Object2IntOpenHashMap<>();
    private volatile boolean empty = true;

    private final World world;

    private final Function<UUID, Entity> entitySupplier;

    public static LightSources get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, LightSources::new, LightSources::new);
    }

    LightSources(World world, NbtCompound compound) {
        this(world);
    }

    LightSources(World world) {
        this.world = world;
        entitySupplier = world instanceof ServerWorld s ? s::getEntity : id -> world.getEntityById(lightSourceClientIds.getInt(id));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound, WrapperLookup lookup) {
        return compound;
    }

    public void addLightSource(Entity entity) {
        synchronized (lightSourceIds) {
            lightSourceIds.add(entity.getUuid());
            lightSourceClientIds.put(entity.getUuid(), entity.getId());
            empty = false;
        }
    }

    public void removeLightSource(Entity entity) {
        if (empty) {
            return;
        }
        synchronized (lightSourceIds) {
            lightSourceIds.remove(entity.getUuid());
            lightSourceClientIds.removeInt(entity.getUuid());
            empty = lightSourceIds.isEmpty();
        }
    }

    public int getLuminance(long blockPos) {
        if (empty) {
            return 0;
        }
        final int[] result = {0};
        forEachLightSource((pos, level) -> {
           if (pos.asLong() == blockPos) {
               result[0] += level;
           }
        });
        return result[0];
    }

    public void forEachLightSource(ChunkPos chunkPos, LightSourceConsumer consumer) {
        forEachLightSource((pos, level) -> {
            if (checkPos(chunkPos, pos)) {
                consumer.accept(pos, level);
            }
        });
    }

    public void forEachLightSource(LightSourceConsumer consumer) {
        if (empty) {
            return;
        }
        synchronized (lightSourceIds) {
            lightSourceIds.removeIf(id -> {
                Entity entity = entitySupplier.apply(id);
                if (entity instanceof DynamicLightSource source) {
                    consumer.accept(entity.getBlockPos(), source.getLightLevel());
                    return false;
                }
                return true;
            });
            empty = lightSourceIds.isEmpty();
        }
    }

    private boolean checkPos(ChunkPos chunkPos, BlockPos pos) {
        return world.isInBuildLimit(pos)
            && checkPos(pos.getX(), chunkPos.getStartX(), chunkPos.getEndX())
            && checkPos(pos.getZ(), chunkPos.getStartZ(), chunkPos.getEndZ());
    }

    private boolean checkPos(int p, int min, int max) {
        return p >= min && p <= max;
    }

    public interface LightSourceConsumer {
        void accept(BlockPos pos, int light);
    }
}
