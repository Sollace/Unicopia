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

            if ("unicopiamc".equalsIgnoreCase(id.getNamespace())) {
                extentionTableIds.put(Identifier.ofVanilla(id.getPath()), id);
            }
        });
        LootTableEvents.ALL_LOADED.register((resourceManager, registry) -> {
            extentionTableIds.forEach((base, extra) -> {
                registry.getOptionalValue(base).ifPresent(table -> {
                    registry.getOptionalValue(extra).ifPresent(extraTable -> {
                        if (table.pools.isEmpty() || extraTable.pools.size() > 1 || (base.getPath().indexOf("gameplay") == -1 && base.getPath().indexOf("archaeology") == -1)) {
                            table.pools = Stream.concat(table.pools.stream(), extraTable.pools.stream()).toList();
                        } else {
                            table.pools.getLast().entries = Stream.concat(table.pools.getLast().entries.stream(), extraTable.pools.getLast().entries.stream()).toList();
                        }
                    });
                });
            });
            extentionTableIds.clear();
        });
    }
}
