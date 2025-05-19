package com.minelittlepony.unicopia.server.world.chunk;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class Section<T extends PositionalDataMap.Hotspot> {
    private final Set<T> entries = weakSet();
    private Set<T>[] states;

    final long pos;

    public Section(long pos) {
        this.pos = pos;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public boolean remove(T entry) {
        if (entries.remove(entry)) {
            states = null;
            return true;
        }
        return false;
    }

    public boolean update(T entry, Box box) {
        entries.add(entry);
        states = null;
        return true;
    }

    @SuppressWarnings("unchecked")
    public Set<T> getState(BlockPos pos) {
        int localPos = toLocalIndex(pos);
        if (states == null) {
            states = new Set[16 * 16 * 16];
        }
        Set<T> state = states[localPos];
        return state == null ? (states[localPos] = calculateState(pos)) : state;
    }

    private Set<T> calculateState(BlockPos pos) {
        Set<T> state = weakSet();

        for (T entry : entries) {
            BlockPos center = entry.getCenter();
            int radius = MathHelper.ceil(entry.getRadius());

            if (pos.equals(center)
                   || (isInRange(pos.getX(), center.getX(), radius)
                    && isInRange(pos.getZ(), center.getZ(), radius)
                    && isInRange(pos.getY(), center.getY(), radius)
                    && center.isWithinDistance(pos, radius))) {
                state.add(entry);
            }
        }

        return state;
    }

    static boolean isInRange(int value, int center, int radius) {
        return value >= center - radius && value <= center + radius;
    }

    static int toLocalIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int y = pos.getY() % 16;
        int z = pos.getZ() % 16;
        return x + (y * 16) + (z * 16 * 16);
    }

    static<T> Set<T> weakSet() {
        return Collections.newSetFromMap(new WeakHashMap<>());
    }
}
