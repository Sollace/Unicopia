package com.minelittlepony.unicopia.client;

import java.util.SortedSet;
import com.google.common.collect.Sets;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;

public class ClientBlockDestructionManager {

    private final Long2ObjectMap<Destruction> destructions = new Long2ObjectOpenHashMap<>();

    private final Long2ObjectMap<SortedSet<BlockBreakingInfo>> combined = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    public void setBlockDestruction(long pos, float amount) {
        synchronized (locker) {
            if (amount <= 0 || amount > BlockDestructionManager.MAX_DAMAGE) {
                destructions.remove(pos);
            } else {
                destructions.computeIfAbsent(pos, p -> new Destruction(pos)).set(amount);
            }
        }
    }

    public void tick(Long2ObjectMap<SortedSet<BlockBreakingInfo>> vanilla) {
        synchronized (locker) {
            destructions.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());

            combined.clear();

            if (!destructions.isEmpty()) {
                destructions.forEach((pos, value) -> {
                    combined.computeIfAbsent(pos.longValue(), p -> Sets.newTreeSet()).add(value.info);
                });
                vanilla.forEach((pos, value) -> {
                    combined.computeIfAbsent(pos.longValue(), p -> Sets.newTreeSet()).addAll(value);
                });
            }
        }
    }

    public Long2ObjectMap<SortedSet<BlockBreakingInfo>> getCombinedDestructions(Long2ObjectMap<SortedSet<BlockBreakingInfo>> vanilla) {
        return destructions.isEmpty() ? vanilla : combined;
    }

    private class Destruction {
        int age = 50;

        BlockBreakingInfo info;

        Destruction(long pos) {
            this.info = new BlockBreakingInfo(0, BlockPos.fromLong(pos));
        }

        boolean tick() {
            if (age-- > 0) {
                return false;
            }
            int amount = info.getStage();

            if (amount >= 0) {
                amount--;
                set(amount);
            }
            return amount < 0 || age-- <= 0;
        }

        void set(float amount) {
            this.age = 50;
            info.setStage(amount >= 0 && amount < BlockDestructionManager.MAX_DAMAGE ? (int)amount : BlockDestructionManager.UNSET_DAMAGE);
        }
    }

    public interface Source {
        ClientBlockDestructionManager getDestructionManager();

        int getTicks();
    }
}
