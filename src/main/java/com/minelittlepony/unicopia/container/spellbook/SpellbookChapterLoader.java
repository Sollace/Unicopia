package com.minelittlepony.unicopia.container.spellbook;

import java.util.*;
import java.util.concurrent.*;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Untyped;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

public class SpellbookChapterLoader {
    public static final Identifier ID = Unicopia.id("spellbook/chapters");
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private boolean dirty;
    private Map<Identifier, SpellbookChapter> chapters = new HashMap<>();

    public void sendUpdate(MinecraftServer server) {
        if (dirty) {
            dirty = false;
            Channel.SERVER_RESOURCES.sendToAllPlayers(new MsgServerResources(server), server);
        }
    }

    public Map<Identifier, SpellbookChapter> getChapters() {
        return Untyped.cast(chapters);
    }

    public Reload createReloader(RegistryWrapper.WrapperLookup registries) {
        return new Reload(registries);
    }

    public final class Reload extends JsonDataLoader<SpellbookChapter> implements IdentifiableResourceReloadListener {
        public Reload(RegistryWrapper.WrapperLookup registries) {
            super(registries, SpellbookChapter.CODEC, RegistryKey.ofRegistry(ID));
        }

        @Override
        public Identifier getFabricId() {
            return ID;
        }

        @Override
        protected void apply(Map<Identifier, SpellbookChapter> data, ResourceManager manager, Profiler profiler) {
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
}
