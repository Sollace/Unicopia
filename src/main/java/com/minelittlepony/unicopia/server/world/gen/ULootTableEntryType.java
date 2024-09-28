package com.minelittlepony.unicopia.server.world.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.util.Identifier;

public interface ULootTableEntryType {
    static void bootstrap() {
        Map<Identifier, Identifier> extentionTableIds = new HashMap<>();
        LootTableEvents.MODIFY.register((key, supplier, source, registries) -> {
            Identifier id = key.getValue();

            if ("unicopiamc".equalsIgnoreCase(id.getPath())) {
                extentionTableIds.put(Identifier.ofVanilla(id.getPath()), id);
            }
        });
        LootTableEvents.ALL_LOADED.register((resourceManager, registry) -> {
            extentionTableIds.forEach((base, extra) -> {
                registry.getOrEmpty(base).ifPresent(table -> {
                    registry.getOrEmpty(extra).ifPresent(extraTable -> {
                        table.pools = Stream.concat(table.pools.stream(), extraTable.pools.stream()).toList();
                    });
                });
            });
            extentionTableIds.clear();
        });
    }
}