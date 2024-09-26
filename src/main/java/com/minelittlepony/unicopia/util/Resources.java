package com.minelittlepony.unicopia.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.minelittlepony.common.util.settings.ToStringAdapter;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public interface Resources {
    Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new ToStringAdapter<>(Identifier::of))
            .create();

    static Stream<Resource> getResources(ResourceManager manager, Identifier id) {
        return manager.getAllResources(id).stream();
    }

    static Stream<Identifier> loadFile(Resource res, Type type, String msg) throws JsonParseException {
        try (InputStreamReader reader = new InputStreamReader(res.getInputStream())) {
            return (GSON.<List<Identifier>>fromJson(reader, type)).stream();
        } catch (JsonParseException e) {
            Unicopia.LOGGER.warn(msg + res.getPackId(), e);
        } catch (IOException ignored) {}

        return Stream.empty();
    }
}
