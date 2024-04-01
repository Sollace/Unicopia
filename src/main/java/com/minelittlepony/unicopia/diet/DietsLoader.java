package com.minelittlepony.unicopia.diet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class DietsLoader implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ID = Unicopia.id("diets");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager manager,
            Profiler prepareProfiler, Profiler applyProfiler,
            Executor prepareExecutor, Executor applyExecutor) {

        CompletableFuture<Map<Identifier, Effect>> foodGroupsFuture = CompletableFuture.supplyAsync(() -> {
            Map<Identifier, Effect> foodGroups = new HashMap<>();
            for (var group : loadData(manager, prepareExecutor, "diets/food_groups").entrySet()) {
                try {
                    Effect.CODEC.parse(JsonOps.INSTANCE, group.getValue())
                        .resultOrPartial(error -> LOGGER.error("Could not load food group {}: {}", group.getKey(), error))
                        .ifPresent(value -> {
                            foodGroups.put(group.getKey(), value);
                        });
                } catch (Throwable t) {
                    LOGGER.error("Could not load food effects {}", group.getKey(), t);
                }
            }
            return foodGroups;
        }, prepareExecutor);
        CompletableFuture<Map<Race, DietProfile>> profilesFuture = CompletableFuture.supplyAsync(() -> {
            Map<Race, DietProfile> profiles = new HashMap<>();
            for (var entry : loadData(manager, prepareExecutor, "diets/races").entrySet()) {
                Identifier id = entry.getKey();
                try {
                    Race.REGISTRY.getOrEmpty(id).ifPresentOrElse(race -> {
                        DietProfile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                                .resultOrPartial(error -> LOGGER.error("Could not load diet profile {}: {}", id, error))
                                .ifPresent(profile -> profiles.put(race, profile));
                    }, () -> LOGGER.warn("Skipped diet for unknown race: " + id));
                } catch (Throwable t) {
                    LOGGER.error("Could not load diet profile {}", id, t);
                }
            }
            return profiles;
        }, prepareExecutor);

        return CompletableFuture.allOf(foodGroupsFuture, profilesFuture).thenCompose(sync::whenPrepared).thenAcceptAsync(v -> {
            var profiles = profilesFuture.getNow(Map.of());
            var foodGroups = foodGroupsFuture.getNow(Map.of());
            profiles.entrySet().removeIf(entry -> {
                StringBuilder issueList = new StringBuilder();
                entry.getValue().validate(issue -> {
                    issueList.append(System.lineSeparator()).append(issue);
                }, foodGroups::containsKey);
                if (!issueList.isEmpty()) {
                    LOGGER.error("Could not load diet profile {}. Caused by {}", entry.getKey(), issueList.toString());
                }
                return issueList.isEmpty();
            });
            PonyDiets.load(new PonyDiets(profiles, foodGroups));
        }, applyExecutor);
    }

    private static Map<Identifier, JsonElement> loadData(ResourceManager manager, Executor prepareExecutor, String path) {
        Map<Identifier, JsonElement> results = new HashMap<>();
        JsonDataLoader.load(manager, path, Resources.GSON, results);
        return results;
    }
}
