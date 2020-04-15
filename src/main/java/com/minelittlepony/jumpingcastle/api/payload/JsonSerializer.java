package com.minelittlepony.jumpingcastle.api.payload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializer {
    private static final Gson READER_GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static <T> T read(BinaryPayload payload, Class<T> type) {
        return (T)READER_GSON.fromJson(payload.readString(), type);
    }

    public static <T> void write(BinaryPayload payload, T object) {
        payload.writeString(READER_GSON.toJson(object));
    }

}
