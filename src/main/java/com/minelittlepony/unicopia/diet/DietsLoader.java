package com.minelittlepony.unicopia.diet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class DietsLoader implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ID = Unicopia.id("diets");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        var foodGroupsFuture = CompletableFuture.supplyAsync(() -> loadData(manager, prepareExecutor, "diet/food_groups", FoodGroup.EFFECTS_CODEC).collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new FoodGroup(entry.getKey(), entry.getValue())
        )), prepareExecutor);
        @SuppressWarnings("unchecked")
        var profilesFuture = CompletableFuture.supplyAsync(() -> {
            return Map.<Race, DietProfile>ofEntries(loadData(manager, prepareExecutor, "diet/races", DietProfile.CODEC)
                    .flatMap(entry -> Race.REGISTRY.getOptionalValue(entry.getKey()).map(race -> Map.entry(race, entry.getValue())).stream())
                    .toArray(Map.Entry[]::new));
        }, prepareExecutor);

        return CompletableFuture.allOf(foodGroupsFuture, profilesFuture).thenCompose(sync::whenPrepared).thenAcceptAsync(v -> {
            Map<Race, DietProfile> profiles = profilesFuture.getNow(Map.of());
            Map<Identifier, FoodGroup> foodGroups = foodGroupsFuture.getNow(Map.of());
            profiles.entrySet().removeIf(entry -> {
                StringBuilder issueList = new StringBuilder();
                entry.getValue().validate(issue -> {
                    issueList.append(System.lineSeparator()).append(issue);
                }, foodGroups::containsKey);
                if (!issueList.isEmpty()) {
                    LOGGER.error("Could not load diet profile {}. Caused by {}", entry.getKey(), issueList.toString());
                }
                return !issueList.isEmpty();
            });
            PonyDiets.load(new PonyDiets(profiles, foodGroups));
        }, applyExecutor);
    }

    private static <T> Stream<Map.Entry<Identifier, T>> loadData(ResourceManager manager, Executor prepareExecutor, String path, Codec<T> codec) {
        Map<Identifier, T> results = new HashMap<>();
        JsonDataLoader.load(manager, ResourceFinder.json(path), JsonOps.INSTANCE, codec, results);
        return results.entrySet().stream();
    }
}
