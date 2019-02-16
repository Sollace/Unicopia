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

    private static UConfig instance = new UConfig();

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    public static UConfig instance() {
        return instance;
    }

    static void init(File directory) {
        File file = new File(directory, "unicopia.json");

        try {
            if (file.exists()) {
                try(JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file)));) {
                    instance = gson.fromJson(reader, UConfig.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (instance == null) {
                instance = new UConfig();
            }
        }

        instance.file = file;
        instance.save();
    }

    private File file;

    @Expose(deserialize = false)
    private final String speciesWhiteListComment =
            "A whitelist of races permitted on the server. " +
            "Races added to this list can be used by anyone, whilst any ones left off are not permitted. " +
            "An empty list disables whitelisting entirely.";
    @Expose
    private final List<Race> speciesWhiteList = Lists.newArrayList();

    @Expose(deserialize = false)
    private final String preferredRaceComment =
            "The default preferred race. " +
            "This is the race a client requests when first joining a game. " +
            "It is the default used both when Mine Little Pony is not installed and when they respond with a human race.";
    @Expose
    private Race preferredRace = Race.EARTH;

    @Expose(deserialize = false)
    private final String ignoreMineLPComment =
            "If true Mine Little Pony will not be considered when determining the race to use. " +
            "The result will always be what is set by this config file.";
    @Expose
    private boolean ignoreMineLP = false;

    public List<Race> getSpeciesWhiteList() {
        return speciesWhiteList;
    }

    public boolean ignoresMineLittlePony() {
        return ignoreMineLP;
    }

    public void setIgnoreMineLittlePony(boolean value) {
        if (value != ignoreMineLP) {
            ignoreMineLP = value;
            save();
        }
    }

    public void setPreferredRace(Race race) {
        if (preferredRace != race) {
            preferredRace = race;
            save();
        }
    }

    public Race getPrefferedRace() {
        if (preferredRace == null) {
            setPreferredRace(Race.EARTH);
        }

        return preferredRace;
    }

    public void save() {
        if (file.exists()) {
            file.delete();
        }

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            writer.setIndent("    ");

            gson.toJson(this, UConfig.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
