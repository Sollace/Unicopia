package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.Codec;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public record PersistentStateKey<T extends PersistentState> (
        Identifier id,
        Function<World, T> constructor,
        PersistentStateType<T> key
    ) {

    public PersistentStateKey(Identifier id, Function<PersistentState.Context, Codec<T>> codec, Function<World, T> factory) {
        this(id, factory, new PersistentStateType<>(
                id.toUnderscoreSeparatedString(),
                context -> factory.apply(context.getWorldOrThrow()),
                codec,
                DataFixTypes.LEVEL
        ));
    }

    public PersistentStateKey(Identifier id, Codec<T> codec, Supplier<T> factory) {
        this(id, world -> factory.get(), new PersistentStateType<>(
                id.toUnderscoreSeparatedString(),
                factory,
                codec,
                DataFixTypes.LEVEL
        ));
    }

    public T get(WorldView world) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getPersistentStateManager().getOrCreate(key);
        }

        return ClientInstance.of((World)world, id, constructor).instance();
    }

    record ClientInstance<T extends PersistentState>(WeakReference<World> world, T instance) {
        private static final Map<Identifier, ClientInstance<?>> INSTANCES = new HashMap<>();

        public static <T extends PersistentState> ClientInstance<T> of(World world, Identifier id, Function<World, T> factory) {
            return Untyped.cast(INSTANCES.compute(id, (i, instance) -> {
                if (instance == null || !instance.matches(world)) {
                    return new ClientInstance<>(world, factory);
                }
                return instance;
            }));
        }

        public ClientInstance(World world, Function<World, T> factory) {
            this(new WeakReference<>(world), factory.apply(world));
        }

        public boolean matches(World world) {
            return world().get() == world;
        }
    }
}
