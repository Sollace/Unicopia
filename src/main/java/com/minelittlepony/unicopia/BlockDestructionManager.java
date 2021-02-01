package com.minelittlepony.unicopia;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class BlockDestructionManager {

    public static final int UNSET_DAMAGE = -1;
    public static final int MAX_DAMAGE = 10;

    private final Destruction emptyDestruction = new Destruction();

    private final World world;

    private final Long2ObjectMap<Chunk> destructions = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    public BlockDestructionManager(World world) {
        this.world = world;
    }

    public int getBlockDestruction(BlockPos pos) {
        return getDestructions(pos).getBlockDestruction(pos);
    }

    private Chunk getDestructions(BlockPos pos) {
        return destructions.computeIfAbsent(new ChunkPos(pos).toLong(), Chunk::new);
    }

    public void clearBlockDestruction(BlockPos pos) {
        setBlockDestruction(pos, -1);
    }

    public void setBlockDestruction(BlockPos pos, int amount) {
        synchronized (locker) {
            getDestructions(pos).setBlockDestruction(pos, amount);
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
                destructions.forEach((chunkPos, chunk) -> chunk.sendUpdates((ServerWorld)world));
            }
        }
    }

    private class Chunk {
        private final Long2ObjectMap<Destruction> destructions = new Long2ObjectOpenHashMap<>();

        private final long pos;

        Chunk(long pos) {
            this.pos = pos;
        }

        public int getBlockDestruction(BlockPos pos) {
            return destructions.getOrDefault(pos.asLong(), emptyDestruction).amount;
        }

        public void setBlockDestruction(BlockPos pos, int amount) {
            destructions.computeIfAbsent(pos.asLong(), p -> new Destruction()).set(amount);
        }

        boolean tick() {
            destructions.long2ObjectEntrySet().removeIf(e -> e.getValue().tick());
            return destructions.isEmpty();
        }

        void sendUpdates(ServerWorld world) {
            if (!world.getChunkManager().isChunkLoaded(ChunkPos.getPackedX(pos), ChunkPos.getPackedZ(pos))) {
                return;
            }

            ThreadedAnvilChunkStorage storage = world.getChunkManager().threadedAnvilChunkStorage;

            List<PlayerEntity> players = storage.getPlayersWatchingChunk(new ChunkPos(pos), false).collect(Collectors.toList());

            if (!players.isEmpty()) {
                Long2ObjectOpenHashMap<Integer> values = new Long2ObjectOpenHashMap<>();

                destructions.forEach((blockPos, item) -> {
                    if (item.dirty) {
                        item.dirty = false;
                        values.put(blockPos.longValue(), (Integer)item.amount);
                    }
                });

                MsgBlockDestruction msg = new MsgBlockDestruction(values);

                if (msg.toBuffer().writerIndex() > 1048576) {
                    throw new IllegalStateException("Payload may not be larger than 1048576 bytes. Here's what we were trying to send: ["
                            + values.size() + "]\n"
                            + Arrays.toString(values.values().stream().mapToInt(Integer::intValue).toArray()));
                }

                players.forEach(player -> Channel.SERVER_BLOCK_DESTRUCTION.send(player, msg));
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
