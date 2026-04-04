package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class WorldOverlay<T extends WorldOverlay.State> extends PersistentState implements Tickable {

    private final World world;

    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final Object locker = new Object();

    private final Supplier<T> factory;
    @Nullable
    private final BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender;

    public static <T extends PersistentState> Accessor<T> createAccessor(Identifier id, Function<PersistentState.Context, Codec<T>> codec, Function<World, T> factory) {
        var type = new PersistentStateType<>(
                id.getNamespace() + "_" + id.getPath().replace('/', '_'),
                context -> factory.apply(context.getWorldOrThrow()),
                codec,
                DataFixTypes.LEVEL
        );

        return world -> {
            if (world instanceof ServerWorld serverWorld) {
                return serverWorld.getPersistentStateManager().getOrCreate(type);
            }

            return ClientInstance.of((World)world, id, factory).instance();
        };
    }

    public static <T extends PersistentState> Accessor<T> createAccessor(Identifier id, Codec<T> codec, Supplier<T> factory) {
        var type = new PersistentStateType<>(
                id.getNamespace() + "_" + id.getPath().replace('/', '_'),
                factory,
                codec,
                DataFixTypes.LEVEL
        );

        return world -> {
            if (world instanceof ServerWorld serverWorld) {
                return serverWorld.getPersistentStateManager().getOrCreate(type);
            }

            return ClientInstance.of((World)world, id, w -> factory.get()).instance();
        };
    }

    interface Accessor<T extends PersistentState> {
        T get(WorldView world);
    }

    public static <T extends State> Accessor<WorldOverlay<T>> createAccessor(Identifier id, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender) {
        return createAccessor(id, w -> new WorldOverlay<>(w, factory, updateSender));
    }

    public static  <T extends State> Accessor<WorldOverlay<T>> createAccessor(Identifier id, Function<World, WorldOverlay<T>> overlayFactory) {
        return createAccessor(id, context -> {
            return NbtCompound.CODEC.xmap(tag -> {
                WorldOverlay<T> overlay = overlayFactory.apply(context.getWorldOrThrow());
                overlay.readNbt(tag, context.getWorldOrThrow().getRegistryManager());
                return overlay;
            }, overlay -> {
                return overlay.writeNbt(new NbtCompound(), context.getWorldOrThrow().getRegistryManager());
            });
        }, overlayFactory);
    }

    WorldOverlay(World world, Supplier<T> factory, @Nullable BiConsumer<Long2ObjectMap<T>, List<ServerPlayerEntity>> updateSender) {
        this.world = world;
        this.factory = factory;
        this.updateSender = updateSender;
    }

    public NbtCompound writeNbt(NbtCompound compound, WrapperLookup lookup) {
        NbtCompound destructions = new NbtCompound();
        this.chunks.forEach((id, chunk) -> {
            destructions.put(id.toString(), chunk.toNBT(lookup));
        });
        compound.put("chunks", destructions);
        return compound;
    }

    public void readNbt(NbtCompound compound, WrapperLookup lookup) {
        NbtCompound d = compound.getCompoundOrEmpty("chunks");
        d.getKeys().forEach(id -> {
            chunks.computeIfAbsent(Long.valueOf(id), Chunk::new).fromNBT(d.getCompoundOrEmpty(id), lookup);
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
        return chunks.computeIfAbsent(ChunkPos.toLong(pos), Chunk::new);
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

            ServerChunkLoadingManager storage = world.getChunkManager().chunkLoadingManager;

            List<ServerPlayerEntity> players = storage.getPlayersWatchingChunk(new ChunkPos(pos), false);

            if (!players.isEmpty()) {
                updateSender.accept(states, players);
            }
        }

        @Override
        public void toNBT(NbtCompound compound, WrapperLookup lookup) {
            NbtCompound states = new NbtCompound();
            this.states.forEach((id, state) -> {
                states.put(id.toString(), state.toNBT(lookup));
            });
            compound.put("states", states);
        }

        @Override
        public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
            NbtCompound d = compound.getCompoundOrEmpty("states");
            chunks.clear();
            d.getKeys().forEach(id -> {
                states.computeIfAbsent(Long.valueOf(id), i -> factory.get()).fromNBT(d.getCompoundOrEmpty(id), lookup);
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
