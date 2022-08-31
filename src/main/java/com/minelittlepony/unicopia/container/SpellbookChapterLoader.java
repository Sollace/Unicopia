package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.google.gson.*;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.container.SpellbookChapterList.*;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.*;
import net.minecraft.util.profiler.Profiler;

public class SpellbookChapterLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static boolean DEBUG = true;

    private static final Identifier ID = Unicopia.id("spellbook/chapters");

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterLoader() {
        super(Resources.GSON, ID.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public Set<SpellbookChapterList.Chapter> getChapters() {
        return new HashSet<>(chapters.values());
    }

    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        chapters = data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            JsonObject json = JsonHelper.asObject(entry.getValue(), "root");

            return new Chapter(entry.getKey(),
                    TabSide.valueOf(JsonHelper.getString(json, "side")),
                    JsonHelper.getInt(json, "y_position"),
                    JsonHelper.getInt(json, "color", 0),
                    loadContent(JsonHelper.getObject(json, "content", new JsonObject()))
            );
        }));

        if (DEBUG) {
            CompletableFuture.runAsync(() -> {
                reload(CompletableFuture::completedFuture, manager, profiler, profiler, Util.getMainWorkerExecutor(), MinecraftClient.getInstance());
            }, EXECUTOR);
        }
    }

    private Optional<Content> loadContent(JsonObject json) {
        return Optional.of(JsonHelper.getArray(json, "pages", new JsonArray()))
            .filter(pages -> pages.size() > 0)
            .map(DynamicContent::new);
    }
}
