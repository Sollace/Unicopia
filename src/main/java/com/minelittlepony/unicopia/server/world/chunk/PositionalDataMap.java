package com.minelittlepony.unicopia.server.world.chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;

public class PositionalDataMap<T extends PositionalDataMap.Hotspot> {
    private final Long2ObjectMap<Chunk<T>> chunks = new Long2ObjectOpenHashMap<>();
    private final Map<T, Set<Chunk<T>>> entryToChunks = new WeakHashMap<>();

    public Set<T> getState(BlockPos pos) {
        Chunk<T> chunk = chunks.get(ChunkPos.toLong(pos));
        return chunk == null ? Set.of() : chunk.getState(pos);
    }

    public void remove(T entry) {
        Set<Chunk<T>> chunks = entryToChunks.remove(entry);
        if (chunks != null) {
            chunks.forEach(chunk -> chunk.remove(entry));
        }
    }

    public void update(T entry) {
        Box entryBox = new Box(entry.getCenter()).expand(MathHelper.ceil(entry.getRadius()));
        Set<Chunk<T>> oldChunks = entryToChunks.get(entry);
        Set<Chunk<T>> newChunks = getIntersectingChunks(entryBox);
        if (oldChunks != null) {
            oldChunks.forEach(chunk -> chunk.remove(entry));
        }
        newChunks.forEach(chunk -> chunk.update(entry, entryBox));
        entryToChunks.put(entry, newChunks);
    }

    private Set<Chunk<T>> getIntersectingChunks(Box entryBox) {
        int minX = ChunkSectionPos.getSectionCoord(entryBox.minX);
        int maxX = ChunkSectionPos.getSectionCoord(entryBox.maxX);
        int minZ = ChunkSectionPos.getSectionCoord(entryBox.minZ);
        int maxZ = ChunkSectionPos.getSectionCoord(entryBox.maxZ);

        Set<Chunk<T>> chunks = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(this.chunks.computeIfAbsent(ChunkPos.toLong(x, z), Chunk::new));
            }
        }
        return chunks;
    }

    public interface Hotspot {
        float getRadius();

        BlockPos getCenter();
    }
}
