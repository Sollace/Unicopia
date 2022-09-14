package com.minelittlepony.unicopia.block.state;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;
import com.mojang.logging.LogUtils;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

public class StateMapLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/state_maps");

    public static final StateMapLoader INSTANCE = new StateMapLoader();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_SUFFIX = ".json";
    private static final int FILE_SUFFIX_LENGTH = ".json".length();
    private static final String DATA_TYPE = "state_maps";

    private Map<Identifier, ReversableBlockStateConverter> converters = new HashMap<>();

    public StateMapLoader() {
        super(Resources.GSON, "state_maps");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, Profiler profiler) {
        Map<Identifier, JsonElement> map = Maps.newHashMap();
        int i = DATA_TYPE.length() + 1;

        resourceManager.findResources(DATA_TYPE, id -> id.endsWith(FILE_SUFFIX)).stream().forEach(entry -> {
            Identifier resId = entry;
            String path = resId.getPath();
            Identifier id = new Identifier(resId.getNamespace(), path.substring(i, path.length() - FILE_SUFFIX_LENGTH));

            JsonArray entries = new JsonArray();
            try {
                for (var resource : resourceManager.getAllResources(entry)) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        JsonObject json = JsonHelper.deserialize(Resources.GSON, reader, JsonObject.class);

                        if (json != null) {
                            if (json.has("entries")) {

                                JsonArray incoming = JsonHelper.getArray(json, "entries");
                                if (json.has("replace") && json.get("replace").getAsBoolean()) {
                                    entries = incoming;
                                } else {
                                    entries.addAll(incoming);
                                }
                            }

                            continue;
                        }

                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, resId);
                    } catch (JsonParseException | IOException | IllegalArgumentException e) {
                        LOGGER.error("Couldn't parse data file {} from {}", id, resId, e);
                    }
                }
            } catch (IOException e) {
            }

            map.put(id, entries);
        });
        return map;
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
