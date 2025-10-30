package com.minelittlepony.unicopia.server.world;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.DynamicLightSource;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

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

    private final Object2LongMap<UUID> lightSourceLocations = new Object2LongOpenHashMap<>();
    private final Long2ObjectMap<ObjectSet<UUID>> lightSources = new Long2ObjectOpenHashMap<>();
    private final Object2IntMap<UUID> lightSourceClientIds = new Object2IntOpenHashMap<>();

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

    public <T extends Entity & DynamicLightSource> void addLightSource(T entity) {
        synchronized (lightSources) {
            lightSourceLocations.computeLong(entity.getUuid(), (id, lastPos) -> {
                lightSourceClientIds.put(id, entity.getId());
                long pos = entity.getLightSourcePosition().asLong();
                if (lastPos == null || lastPos != pos) {
                    if (lastPos != null) {
                        lightSources.get(lastPos.longValue()).remove(id);
                        checkBlock(lastPos.longValue());
                    }
                    lightSources.computeIfAbsent(pos, l -> new ObjectOpenHashSet<>()).add(id);
                    checkBlock(pos);
                }
                return pos;
            });
            empty = false;
        }
    }

    private void checkBlock(long pos) {
        try {
            world.getLightingProvider().checkBlock(BlockPos.fromLong(pos));
        } catch (Exception ignored) { }
    }

    public void removeLightSource(UUID id) {
        if (empty) {
            return;
        }
        synchronized (lightSources) {
            lightSourceClientIds.removeInt(id);
            if (lightSourceLocations.containsKey(id)) {
                lightSources.computeIfPresent(lightSourceLocations.removeLong(id), (p, ids) -> {
                    ids.remove(id);
                    checkBlock(p);
                    return ids.isEmpty() ? null : ids;
                });
            }
            empty = lightSources.isEmpty();
        }
    }

    public int getLuminance(long blockPos) {
        if (empty) {
            return 0;
        }

        int result = 0;
        synchronized (lightSources) {
            ObjectSet<UUID> ids = lightSources.get(blockPos);
            if (ids != null) {
                final Iterator<UUID> each = ids.iterator();
                while (each.hasNext()) {
                    UUID id = each.next();
                    int level = getLightLevel(id);
                    if (level > 0) {
                        result += level;
                        if (result >= 15) {
                            break;
                        }
                    } else {
                        each.remove();
                        lightSourceClientIds.removeInt(id);
                        lightSourceLocations.removeLong(id);
                    }
                }
                if (ids.isEmpty()) {
                    lightSources.remove(blockPos);
                }
            }
        }
        return Math.min(result, 15);
    }

    public void forEachLightSource(ChunkPos chunkPos, LightSourceConsumer consumer) {
        if (empty) {
            return;
        }

        synchronized (lightSources) {
            lightSources.long2ObjectEntrySet().removeIf(entry -> {
                if (checkPos(chunkPos, entry.getLongKey())) {
                    int result = 0;
                    final Iterator<UUID> each = entry.getValue().iterator();
                    while (each.hasNext()) {
                        UUID id = each.next();
                        int level = getLightLevel(id);
                        if (level > 0) {
                            result += level;
                            if (result >= 15) {
                                break;
                            }
                        } else {
                            each.remove();
                            lightSourceClientIds.removeInt(id);
                            lightSourceLocations.removeLong(id);
                        }
                    }

                    consumer.accept(entry.getLongKey(), Math.min(result, 15));
                }
                return entry.getValue().isEmpty();
            });
            empty = lightSources.isEmpty();
        }
    }

    private int getLightLevel(UUID id) {
        Entity entity = entitySupplier.apply(id);
        if (entity instanceof DynamicLightSource source && !entity.isRemoved()) {
            return source.getLightLevel();
        }
        return 0;
    }

    private boolean checkPos(ChunkPos chunkPos, long pos) {
        return world.isOutOfHeightLimit(BlockPos.unpackLongY(pos))
            && checkPos(BlockPos.unpackLongX(pos), chunkPos.getStartX(), chunkPos.getEndX())
            && checkPos(BlockPos.unpackLongZ(pos), chunkPos.getStartZ(), chunkPos.getEndZ());
    }

    private boolean checkPos(int p, int min, int max) {
        return p >= min && p <= max;
    }

    public interface LightSourceConsumer {
        void accept(long pos, int light);
    }
}
