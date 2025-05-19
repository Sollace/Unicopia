package com.minelittlepony.unicopia.datafix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.datafixer.fix.AttributeIdFix;
import net.minecraft.util.Util;

public class AttributeIdsFixBuilder {
    private final Map<UUID, String> uuids = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();

    public AttributeIdsFixBuilder add(String uuid, String name, String id) {
        uuids.put(UUID.fromString(uuid), id);
        names.put(name, id);
        return this;
    }

    public AttributeIdsFixBuilder add(String uuid, String id) {
        uuids.put(UUID.fromString(uuid), id);
        return this;
    }

    public void register() {
        AttributeIdFix.UUID_TO_ID = Map.copyOf(Util.make(new HashMap<>(AttributeIdFix.UUID_TO_ID), map -> map.putAll(uuids)));
        AttributeIdFix.NAME_TO_ID = Map.copyOf(Util.make(new HashMap<>(AttributeIdFix.NAME_TO_ID), map -> map.putAll(names)));
    }
}
