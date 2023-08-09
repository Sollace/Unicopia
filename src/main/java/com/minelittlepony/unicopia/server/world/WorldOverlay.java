package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class WorldOverlay<T extends WorldOverlay.State> extends PersistentState implements Tickable {

    private final World world;

    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    private final Supplier<T> factory;
    @Nullable
    private final BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender;

    public static <T extends PersistentState> T getPersistableStorage(World world, Identifier id, BiFunction<World, NbtCompound, T> loadFunc, Function<World, T> factory) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getPersistentStateManager().getOrCreate(
                    compound -> loadFunc.apply(world, compound),
                    () -> factory.apply(world),
                    id.getNamespace() + "_" + id.getPath().replace('/', '_')
            );
        }

        return ClientInstance.of(world, id, factory).instance();
    }

    public static <T extends State> WorldOverlay<T> getOverlay(World world, Identifier id, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender) {
        return getOverlay(world, id, w -> new WorldOverlay<>(w, factory, updateSender));
    }

    public static <T extends State> WorldOverlay<T> getOverlay(World world, Identifier id, Function<World, WorldOverlay<T>> overlayFactory) {
        return getPersistableStorage(world, id, (w, tag) -> {
            WorldOverlay<T> overlay = overlayFactory.apply(w);
            overlay.readNbt(tag);
            return overlay;
        }, overlayFactory);
    }

    WorldOverlay(World world, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender) {
        this.world = world;
        this.factory = factory;
        this.updateSender = updateSender;
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

    public void readNbt(NbtCompound compound) {
        NbtCompound d = compound.getCompound("chunks");
        d.getKeys().forEach(id -> {
            chunks.computeIfAbsent(Long.valueOf(id), Chunk::new).fromNBT(d.getCompound(id));
        });
    }

    @Nullable
    public T getState(BlockPos pos) {
        return getChunk(pos).getState(pos);
    }

    public T getOrCreateState(BlockPos pos) {
        synchronized (locker) {
            return getChunk(pos).getOrCreateState(pos);
        }
    }

    private Chunk getChunk(BlockPos pos) {
        return chunks.computeIfAbsent(new ChunkPos(pos).toLong(), Chunk::new);
    }

    public void setState(BlockPos pos, @Nullable T state) {
        synchronized (locker) {
            getChunk(pos).setState(pos, state);
            markDirty();
        }
    }

    @Override
    public void tick() {
        synchronized (locker) {
            chunks.long2ObjectEntrySet().removeIf(entry -> entry.getValue().tick());

            if (world instanceof ServerWorld) {
                chunks.forEach((chunkPos, chunk) -> chunk.sendUpdates((ServerWorld)world));
            }
        }
    }

    private class Chunk implements NbtSerialisable {
        private final Long2ObjectMap<T> states = new Long2ObjectOpenHashMap<>();

        private final long pos;

        Chunk(long pos) {
            this.pos = pos;
        }

        @Nullable
        public T getState(BlockPos pos) {
            return states.get(pos.asLong());
        }

        public T getOrCreateState(BlockPos pos) {
            return states.computeIfAbsent(pos.asLong(), l -> factory.get());
        }

        public void setState(BlockPos pos, @Nullable T state) {
            if (state == null) {
                states.remove(pos.asLong());
            } else {
                states.put(pos.asLong(), state);
            }
        }

        boolean tick() {
            states.long2ObjectEntrySet().removeIf(e -> e.getValue().tick());
            return states.isEmpty();
        }

        void sendUpdates(ServerWorld world) {
            if (updateSender == null) {
                return;
            }

            if (!world.getChunkManager().isChunkLoaded(ChunkPos.getPackedX(pos), ChunkPos.getPackedZ(pos))) {
                return;
            }

            ThreadedAnvilChunkStorage storage = world.getChunkManager().threadedAnvilChunkStorage;

            List<ServerPlayerEntity> players = storage.getPlayersWatchingChunk(new ChunkPos(pos), false);

            if (!players.isEmpty()) {
                updateSender.accept(states, players);
            }
        }

        @Override
        public void toNBT(NbtCompound compound) {
            NbtCompound states = new NbtCompound();
            this.states.forEach((id, state) -> {
                states.put(id.toString(), state.toNBT());
            });
            compound.put("states", states);
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            NbtCompound d = compound.getCompound("states");
            chunks.clear();
            d.getKeys().forEach(id -> {
                states.computeIfAbsent(Long.valueOf(id), i -> factory.get()).fromNBT(d.getCompound(id));
            });
        }
    }

    public interface State extends NbtSerialisable {
        boolean tick();
    }

    record ClientInstance<T extends PersistentState>(WeakReference<World> world, T instance) {
        private static final Map<Identifier, ClientInstance<?>> INSTANCES = new HashMap<>();

        @SuppressWarnings("unchecked")
        public static <T extends PersistentState> ClientInstance<T> of(World world, Identifier id, Function<World, T> factory) {
            return (ClientInstance<T>)INSTANCES.compute(id, (i, instance) -> {
                if (instance == null || !instance.matches(world)) {
                    return new ClientInstance<>(world, factory);
                }
                return instance;
            });
        }

        public ClientInstance(World world, Function<World, T> factory) {
            this(new WeakReference<>(world), factory.apply(world));
        }

        public boolean matches(World world) {
            return this.world().get() == world;
        }
    }
}
