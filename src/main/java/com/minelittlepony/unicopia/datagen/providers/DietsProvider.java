package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.FoodGroup;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.data.server.tag.TagProvider.TagLookup;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DietsProvider implements DataProvider {
    private final DataCollector dietsCollector;
    private final DataCollector categoriesCollector;

    private final CompletableFuture<TagLookup<Item>> itemTagLookup;

    public DietsProvider(FabricDataOutput output, TagProvider<Item> tagProvider) {
        this.dietsCollector = new DataCollector(output.getResolver(DataOutput.OutputType.DATA_PACK, "diet/races"));
        this.categoriesCollector = new DataCollector(output.getResolver(DataOutput.OutputType.DATA_PACK, "diet/food_groups"));
        itemTagLookup = tagProvider.getTagLookupFuture();
    }

    @Override
    public String getName() {
        return "Diets";
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return itemTagLookup.thenCompose(tagLookup -> {
            var diets = categoriesCollector.prime();
            Map<Identifier, Set<Identifier>> keyToGroupId = new HashMap<>();
            new FoodGroupsGenerator().generate((id, builder) -> {
                var attributes = builder.build();
                attributes.tags().forEach(key -> {
                    if (!tagLookup.contains(TagKey.of(RegistryKeys.ITEM, key.id()))) {
                        throw new IllegalArgumentException("Food group " + id + " references unknown item tag " + key.id());
                    }
                    keyToGroupId.computeIfAbsent(key.id(), i -> new HashSet<>()).add(id);
                });
                diets.accept(id, () -> FoodGroup.EFFECTS_CODEC.encode(attributes, JsonOps.INSTANCE, new JsonObject()).result().get());
            });
            var profiles = dietsCollector.prime();
            new DietProfileGenerator().generate((race, profile) -> {
                Identifier id = Race.REGISTRY.getId(race);
                StringBuilder issues = new StringBuilder();
                profile.validate(issue -> {
                    issues.append(System.lineSeparator()).append(issue);
                }, categoriesCollector::isDefined);
                if (!issues.isEmpty()) {
                    throw new IllegalArgumentException("Diet profile " + id + " failed validation: " + issues.toString());
                }
                profiles.accept(id, () -> DietProfile.CODEC.encode(profile, JsonOps.INSTANCE, new JsonObject()).result().get());
            });
            keyToGroupId.forEach((tag, groups) -> {
               if (groups.size() > 1) {
                   throw new IllegalArgumentException("Multiple groups referenced the same tag " + tag + " held by "
                           + groups.stream().map(Identifier::toString).collect(Collectors.joining())
                   );
               }
            });

            return CompletableFuture.allOf(categoriesCollector.upload(writer), dietsCollector.upload(writer));
        });
    }
}
