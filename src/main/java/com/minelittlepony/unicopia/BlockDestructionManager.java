package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDestructionManager {

    public static final int UNSET_DAMAGE = -1;
    public static final int MAX_DAMAGE = 10;

    private final Destruction emptyDestruction = new Destruction();

    private final World world;

    private final Long2ObjectMap<Destruction> destructions = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    public BlockDestructionManager(World world) {
        this.world = world;
    }

    public int getBlockDestruction(BlockPos pos) {
        return destructions.getOrDefault(pos.asLong(), emptyDestruction).amount;
    }

    public void clearBlockDestruction(BlockPos pos) {
        setBlockDestruction(pos, -1);
    }

    public void setBlockDestruction(BlockPos pos, int amount) {
        synchronized (locker) {
            destructions.computeIfAbsent(pos.asLong(), p -> new Destruction()).set(amount);
        }
    }

    public int damageBlock(BlockPos pos, int amount) {
        if (amount == 0) {
            return getBlockDestruction(pos);
        }
        amount = Math.max(getBlockDestruction(pos), 0) + amount;
        setBlockDestruction(pos, amount);
        return amount;
    }

    public void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newstate) {
        if (oldState.getBlock() != newstate.getBlock()) {
            clearBlockDestruction(pos);
        }
    }

    public void tick() {
        synchronized (locker) {
            destructions.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());

            if (world instanceof ServerWorld) {
                Long2ObjectMap<Integer> sent = new Long2ObjectOpenHashMap<>();
                destructions.forEach((p, item) -> {
                    if (item.dirty) {
                        sent.put(p.longValue(), (Integer)item.amount);
                    }
                });
                if (!sent.isEmpty()) {
                    Channel.SERVER_BLOCK_DESTRUCTION.send(world, new MsgBlockDestruction(sent));
                }
            }
        }
    }

    private class Destruction {
        int amount = -1;
        int age = 50;
        boolean dirty;

        boolean tick() {
            if (age-- > 0) {
                return false;
            }

            if (amount >= 0) {
                set(amount - 1);
            }
            return amount < 0 || age-- <= 0;
        }

        void set(int amount) {
            this.age = 50;
            this.amount = amount >= 0 && amount < MAX_DAMAGE ? amount : UNSET_DAMAGE;
            this.dirty = true;
        }
    }

    public interface Source {
        BlockDestructionManager getDestructionManager();
    }
}
