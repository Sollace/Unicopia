package com.minelittlepony.unicopia.advancement;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

public record RacePredicate(Set<Race> include, Set<Race> exclude) implements Predicate<ServerPlayerEntity> {
    public static final RacePredicate EMPTY = new RacePredicate(Set.of(), Set.of());

    public static RacePredicate fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return EMPTY;
        }

        if (json.isJsonArray()) {
            return of(getRaces(json.getAsJsonArray()), Set.of());
        }

        JsonObject root = JsonHelper.asObject(json, "race");
        return of(getRaces(root, "include"), getRaces(root, "exclude"));
    }

    private static RacePredicate of(Set<Race> include, Set<Race> exclude) {
        if (include.isEmpty() && exclude.isEmpty()) {
            return EMPTY;
        }
        return new RacePredicate(include, exclude);
    }

    private static @Nullable Set<Race> getRaces(JsonObject json, String field) {
        return json.has(field) ? getRaces(JsonHelper.getArray(json, field)) : Set.of();
    }

    private static Set<Race> getRaces(JsonArray array) {
        return array.asList()
                .stream()
                .map(el -> Race.fromName(el.getAsString(), Race.EARTH))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public boolean test(ServerPlayerEntity player) {
        Race race = Pony.of(player).getSpecies();
        return (include.isEmpty() || include.contains(race)) && !(!exclude.isEmpty() && exclude.contains(race));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (!include.isEmpty()) {
            JsonArray arr = new JsonArray();
            include.forEach(r -> arr.add(Race.REGISTRY.getId(r).toString()));
            json.add("include", arr);
        }
        if (!exclude.isEmpty()) {
            JsonArray arr = new JsonArray();
            exclude.forEach(r -> arr.add(Race.REGISTRY.getId(r).toString()));
            json.add("exclude", arr);
        }
        return json;
    }
}
