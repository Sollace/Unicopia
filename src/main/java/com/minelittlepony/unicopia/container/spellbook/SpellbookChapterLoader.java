package com.minelittlepony.unicopia.container.spellbook;

import java.util.*;
import java.util.concurrent.*;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Untyped;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

public class SpellbookChapterLoader extends JsonDataLoader<Chapter> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("spellbook/chapters");
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private boolean dirty;
    private Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterLoader() {
        super(Chapter.CODEC, ResourceFinder.json(ID.getPath()));
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public Map<Identifier, SpellbookChapter> getChapters() {
        return Untyped.cast(chapters);
    }

    public void sendUpdate(MinecraftServer server) {
        if (dirty) {
            dirty = false;
            Channel.SERVER_RESOURCES.sendToAllPlayers(new MsgServerResources(server), server);
        }
    }

    @Override
    protected void apply(Map<Identifier, Chapter> data, ResourceManager manager, Profiler profiler) {
        chapters = data;

        if (Debug.SPELLBOOK_CHAPTERS) {
            CompletableFuture.runAsync(() -> {
                try {
                    Util.waitAndApply(executor -> reload(CompletableFuture::completedFuture, manager, Util.getMainWorkerExecutor(), executor)).get();
                } catch (InterruptedException | ExecutionException e) {
                }
                dirty = true;
            }, EXECUTOR);
        }
    }
}
