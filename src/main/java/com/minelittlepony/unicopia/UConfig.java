package com.minelittlepony.unicopia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class UConfig {

    private static UConfig instance;

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    public static UConfig getInstance() {
        return instance;
    }

    static void init(File directory) {
        File file = new File(directory, "unicopia.json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try(JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file)));) {
                instance = gson.fromJson(reader, UConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (instance == null) {
                instance = new UConfig();
            }
        }

        instance.file = file;

    }

    private File file;

    @Expose
    private final List<Race> speciesWhiteList = Lists.newArrayList();

    public List<Race> getSpeciesWhiteList() {
        return speciesWhiteList;
    }

    public void save() {
        if (file.exists()) {
            file.delete();
        }

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            gson.toJson(this, UConfig.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
