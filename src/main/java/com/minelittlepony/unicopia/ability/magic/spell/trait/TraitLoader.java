package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class TraitLoader extends SinglePreparationResourceReloader<Map<Identifier, SpellTraits>> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = new Identifier("unicopia", "data/traits");

    private static final TypeToken<Map<String, String>> TYPE = new TypeToken<>() {};

    public static final TraitLoader INSTANCE = new TraitLoader();

    Map<Identifier, SpellTraits> values = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Map<Identifier, SpellTraits> prepare(ResourceManager manager, Profiler profiler) {
        profiler.startTick();

        Map<Identifier, SpellTraits> prepared = new HashMap<>();

        for (Identifier path : new HashSet<>(manager.findResources("traits", p -> p.endsWith(".json")))) {
            profiler.push(path.toString());
            try {
                for (Resource resource : manager.getAllResources(path)) {
                    profiler.push(resource.getResourcePackName());

                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        Map<String, String> data = JsonHelper.deserialize(Resources.GSON, reader, TYPE);

                        data.forEach((name, set) -> {
                            try {
                                Identifier id = new Identifier(name);
                                SpellTraits.fromEntries(Arrays.stream(set.split(" ")).map(a -> a.split(":")).map(pair -> {
                                    Trait key = Trait.REGISTRY.get(pair[0].toUpperCase());
                                    if (key == null) {
                                        Unicopia.LOGGER.warn("Failed to load trait entry for item {} in {}. {} is not a valid trait", id, resource.getResourcePackName(), pair[0]);
                                        return null;
                                    }
                                    try {
                                        return Map.entry(key, Float.parseFloat(pair[1]));
                                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                        Unicopia.LOGGER.warn("Failed to load trait entry for item {} in {}. {} is not a valid weighting", id, resource.getResourcePackName(), Arrays.toString(pair));
                                        return null;
                                    }
                                })).ifPresent(value -> prepared.put(id, value));
                            } catch (InvalidIdentifierException e) {
                                Unicopia.LOGGER.warn("Failed to load traits for item {} in {}.", name, resource.getResourcePackName(), e);
                            }
                        });
                    } finally {
                        profiler.pop();
                    }

                }
            } catch (IOException | JsonParseException e) {
            } finally {
                profiler.pop();
            }
        }


        return prepared;
    }

    @Override
    protected void apply(Map<Identifier, SpellTraits> prepared, ResourceManager manager, Profiler profiler) {
        values = prepared;
    }
}
