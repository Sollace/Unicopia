package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDestructionManager {

    public static final int UNSET_DAMAGE = -1;
    public static final int MAX_DAMAGE = 10;

    private final Destruction emptyDestruction = new Destruction(BlockPos.ORIGIN);

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
            destructions.computeIfAbsent(pos.asLong(), p -> new Destruction(pos)).set(amount);
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

    public void tick() {
        synchronized (locker) {
            destructions.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());
        }
    }

    private class Destruction {
        BlockPos pos;
        int amount = -1;
        int age = 50;

        Destruction(BlockPos pos) {
            this.pos = pos;
        }

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
            if (world instanceof ServerWorld) {
                Channel.SERVER_BLOCK_DESTRUCTION.send(world, new MsgBlockDestruction(pos, this.amount));
            }
        }
    }

    public interface Source {
        BlockDestructionManager getDestructionManager();
    }
}
