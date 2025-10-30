package com.minelittlepony.unicopia.block.state;

import java.io.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

public class StateMapLoader extends JsonDataLoader<JsonReversableBlockStateConverter> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/state_maps");

    public static final StateMapLoader INSTANCE = new StateMapLoader();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_SUFFIX = ".json";
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private static final String DATA_TYPE = "state_maps";

    private Map<Identifier, JsonReversableBlockStateConverter> converters = new HashMap<>();

    public StateMapLoader() {
        super(JsonReversableBlockStateConverter.CODEC, "state_maps");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Map<Identifier, JsonReversableBlockStateConverter> prepare(ResourceManager resourceManager, Profiler profiler) {
        int i = DATA_TYPE.length() + 1;

        Map<Identifier, JsonReversableBlockStateConverter> map = Maps.newHashMap();

        resourceManager.findAllResources(DATA_TYPE, id -> id.getPath().endsWith(FILE_SUFFIX)).entrySet().stream().forEach(entry -> {
            Identifier resId = entry.getKey();
            Identifier id = resId.withPath(p -> p.substring(i, p.length() - FILE_SUFFIX_LENGTH));

            JsonArray entries = new JsonArray();
            for (var resource : entry.getValue()) {
                try (BufferedReader reader = resource.getReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    if (!json.has("entries")) {
                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, resId);
                        continue;
                    }

                    JsonArray incoming = JsonHelper.getArray(json, "entries");
                    if (json.has("replace") && json.get("replace").getAsBoolean()) {
                        entries = incoming;
                    } else {
                        entries.addAll(incoming);
                    }
                } catch (JsonParseException | IOException | IllegalArgumentException e) {
                    LOGGER.error("Couldn't parse data file {} from {}", id, resId, e);
                }
            }

            JsonReversableBlockStateConverter.CODEC.decode(JsonOps.INSTANCE, entries).result().ifPresent(pair -> {
                map.put(id, pair.getFirst());
            });
        });
        return map;
    }

    @Override
    protected void apply(Map<Identifier, JsonReversableBlockStateConverter> data, ResourceManager manager, Profiler profiler) {
        converters = data;
    }

    static class Indirect<T extends BlockStateConverter> implements ReversableBlockStateConverter {
        private final Identifier id;
        private final BlockStateConverter inverse;

        public Indirect(Identifier id, Optional<BlockStateConverter> inverse) {
            this.id = id;
            this.inverse = inverse.orElseGet(() -> new StateMapLoader.Indirect<>(id, Optional.of(this)) {
                @Override
                public Optional<BlockStateConverter> get() {
                    return Optional.ofNullable(INSTANCE.converters.get(id)).map(ReversableBlockStateConverter::getInverse);
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
