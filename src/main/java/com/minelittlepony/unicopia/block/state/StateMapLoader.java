package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

public class StateMapLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/state_maps");

    public static final StateMapLoader INSTANCE = new StateMapLoader();

    private Map<Identifier, ReversableBlockStateConverter> converters = new HashMap<>();

    public StateMapLoader() {
        super(Resources.GSON, "state_maps");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        converters = data.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new JsonReversableBlockStateConverter(entry.getValue())
        ));
    }

    static class Indirect<T extends BlockStateConverter> implements ReversableBlockStateConverter {
        private final Identifier id;
        private final BlockStateConverter inverse;

        public Indirect(Identifier id, Optional<BlockStateConverter> inverse) {
            this.id = id;
            this.inverse = inverse.orElseGet(() -> new StateMapLoader.Indirect<>(id, Optional.of(this)) {
                @Override
                public Optional<BlockStateConverter> get() {
                    return Optional.ofNullable(INSTANCE.converters.get(id)).map(map -> map.getInverse());
                }
            });
        }

        @Override
        public boolean canConvert(@Nullable BlockState state) {
            return get().filter(map -> map.canConvert(state)).isPresent();
        }

        @Override
        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
            return get().map(map -> map.getConverted(world, state)).orElse(state);
        }

        @SuppressWarnings("unchecked")
        public Optional<T> get() {
            return Optional.ofNullable((T)INSTANCE.converters.get(id));
        }

        @Override
        public BlockStateConverter getInverse() {
            return inverse;
        }
    }
}
