package com.minelittlepony.unicopia.container.spellbook;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.gson.*;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Resources;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

public class SpellbookChapterLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ID = Unicopia.id("spellbook/chapters");
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private boolean dirty;
    private Map<Identifier, SpellbookChapter> chapters = new HashMap<>();

    public SpellbookChapterLoader() {
        super(Resources.GSON, ID.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public Map<Identifier, SpellbookChapter> getChapters() {
        return chapters;
    }

    public void sendUpdate(MinecraftServer server) {
        if (dirty) {
            dirty = false;
            Channel.SERVER_RESOURCES.sendToAllPlayers(new MsgServerResources(), server);
        }
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        try {
            chapters = data.entrySet().stream().map(entry -> {
                return Chapter.CODEC.decode(JsonOps.INSTANCE, entry.getValue())
                    .ifError(error -> LOGGER.error("Could not load spellbook chapters due to exception {}", error))
                    .result()
                    .map(Pair::getFirst)
                    .map(chapter -> new IdentifiableChapter(entry.getKey(), chapter))
                    .orElse(null);
            }).filter(Objects::nonNull).collect(Collectors.toMap(IdentifiableChapter::id, Function.identity()));
        } catch (IllegalStateException | JsonParseException e) {
            LOGGER.error("Could not load spellbook chapters due to exception", e);
        }

        if (Debug.SPELLBOOK_CHAPTERS) {
            CompletableFuture.runAsync(() -> {
                try {
                    Util.waitAndApply(executor -> reload(CompletableFuture::completedFuture, manager, profiler, profiler, Util.getMainWorkerExecutor(), executor)).get();
                } catch (InterruptedException | ExecutionException e) {
                }
                dirty = true;
            }, EXECUTOR);
        }
    }

    record IdentifiableChapter(Identifier id, Chapter chapter) implements SpellbookChapter {
        public void write(RegistryByteBuf buffer) {
            buffer.writeIdentifier(id);
            chapter.write(buffer);
        }
    }
}
