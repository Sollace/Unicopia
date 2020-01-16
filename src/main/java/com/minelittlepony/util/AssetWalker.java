package com.minelittlepony.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.util.crafting.CraftingManager;

import net.minecraft.util.Identifier;

public class AssetWalker {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final String loadLocation;

    private final String namespace;

    private final JsonConsumer consumer;

    public AssetWalker(Identifier assetLocation, JsonConsumer consumer) {
        this.consumer = consumer;
        this.namespace = assetLocation.getNamespace();

        loadLocation = "/assets/" + namespace + "/" + assetLocation.getPath();
    }

    public void walk() {
        try {
            URL url = AssetWalker.class.getResource(loadLocation);

            if (url == null) {
                LOGGER.error("Couldn't find .mcassetsroot");
                return;
            }

            URI uri = url.toURI();

            if ("file".equals(uri.getScheme())) {
                readFiles(Paths.get(CraftingManager.class.getResource(loadLocation).toURI()));
            } else {
                if (!"jar".equals(uri.getScheme())) {
                    LOGGER.error("Unsupported scheme " + uri + " trying to list all recipes");

                    return;
                }

                try (FileSystem filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    readFiles(filesystem.getPath(loadLocation));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Couldn't get a list of all json files", e);
        }
    }

    private void readFiles(@Nullable Path path) throws IOException {
        if (path == null) {
            return;
        }

        Iterator<Path> iterator = Files.walk(path).iterator();

        while (iterator.hasNext()) {
            Path i = iterator.next();

            if ("json".equals(FilenameUtils.getExtension(i.toString()))) {
                Identifier id = new Identifier(namespace, FilenameUtils.removeExtension(path.relativize(i).toString()).replaceAll("\\\\", "/"));

                try(BufferedReader bufferedreader = Files.newBufferedReader(i)) {
                    consumer.accept(id, GSON.fromJson(bufferedreader, JsonObject.class));
                } catch (JsonParseException e) {
                    LOGGER.error("Parsing error loading recipe " + id, e);

                    return;
                } catch (IOException e) {
                    LOGGER.error("Couldn't read recipe " + id + " from " + i, e);

                    return;
                }
            }
        }
    }

    @FunctionalInterface
    public interface JsonConsumer {
        void accept(Identifier id, JsonObject json) throws JsonParseException;
    }
}
