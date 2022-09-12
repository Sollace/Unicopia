package com.minelittlepony.unicopia;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class BlockDestructionManager extends PersistentState {
    public static final int DESTRUCTION_COOLDOWN = 50;
    public static final int UNSET_DAMAGE = -1;
    public static final int MAX_DAMAGE = 10;

    private final Destruction emptyDestruction = new Destruction();

    private final World world;

    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    public static Supplier<BlockDestructionManager> create(World world) {
        if (world instanceof ServerWorld serverWorld) {
            return Suppliers.memoize(() -> {
                return serverWorld.getPersistentStateManager().getOrCreate(
                        compound -> new BlockDestructionManager(world, compound),
                        () -> new BlockDestructionManager(world),
                        "unicopia:destruction_manager"
                );
            });
        }
        return Suppliers.memoize(() -> new BlockDestructionManager(world));
    }

    BlockDestructionManager(World world) {
        this.world = world;
    }

    BlockDestructionManager(World world, NbtCompound compound) {
        this(world);
        NbtCompound d = compound.getCompound("chunks");
        d.getKeys().forEach(id -> {
            chunks.computeIfAbsent(Long.valueOf(id), Chunk::new).fromNBT(d.getCompound(id));
        });
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        NbtCompound destructions = new NbtCompound();
        this.chunks.forEach((id, chunk) -> {
            destructions.put(id.toString(), chunk.toNBT());
        });
        compound.put("chunks", destructions);
        return compound;
    }

    public int getBlockDestruction(BlockPos pos) {
        return getChunk(pos).getBlockDestruction(pos);
    }

    private Chunk getChunk(BlockPos pos) {
        return chunks.computeIfAbsent(new ChunkPos(pos).toLong(), Chunk::new);
    }

    public void setBlockDestruction(BlockPos pos, int amount) {
        synchronized (locker) {
            getChunk(pos).setBlockDestruction(pos, amount);
            markDirty();
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
            setBlockDestruction(pos, UNSET_DAMAGE);
        }
    }

    public void tick() {
        synchronized (locker) {
            chunks.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());

            if (world instanceof ServerWorld) {
                chunks.forEach((chunkPos, chunk) -> chunk.sendUpdates((ServerWorld)world));
            }
        }
    }

    private class Chunk implements NbtSerialisable {
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

            List<ServerPlayerEntity> players = storage.getPlayersWatchingChunk(new ChunkPos(pos), false);

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

                players.forEach(player -> {
                    if (player instanceof ServerPlayerEntity) {
                        Channel.SERVER_BLOCK_DESTRUCTION.send(player, msg);
                    }
                });
            }
        }

        @Override
        public void toNBT(NbtCompound compound) {
            NbtCompound states = new NbtCompound();
            destructions.forEach((id, state) -> {
                states.put(id.toString(), state.toNBT());
            });
            compound.put("states", states);
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            NbtCompound d = compound.getCompound("states");
            chunks.clear();
            d.getKeys().forEach(id -> {
                destructions.computeIfAbsent(Long.valueOf(id), i -> new Destruction()).fromNBT(d.getCompound(id));
            });
        }
    }

    private class Destruction implements NbtSerialisable {
        int amount = UNSET_DAMAGE;
        int age = DESTRUCTION_COOLDOWN;
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
            this.age = DESTRUCTION_COOLDOWN;
            this.amount = amount >= 0 && amount < MAX_DAMAGE ? amount : UNSET_DAMAGE;
            this.dirty = true;
        }

        @Override
        public void toNBT(NbtCompound compound) {
            compound.putInt("destruction", amount);
            compound.putInt("age", age);
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            amount = compound.getInt("destruction");
            age = compound.getInt("age");
            dirty = true;
        }
    }

    public interface Source {
        BlockDestructionManager getDestructionManager();
    }
}
