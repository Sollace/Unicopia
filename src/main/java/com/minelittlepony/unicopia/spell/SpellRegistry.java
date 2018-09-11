package com.minelittlepony.unicopia.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class SpellRegistry {

    private static final SpellRegistry instance = new SpellRegistry();

    public static SpellRegistry instance() {
        return instance;
    }

    private final Map<String, Callable<IMagicEffect>> factories = new HashMap<>();

    private SpellRegistry() {
    }

    public void init() {
        registerSpell("shield", SpellShield::new);
    }

    public Optional<IMagicEffect> getSpellFromName(String name) {
        try {
            if (factories.containsKey(name)) {
                return Optional.ofNullable(factories.get(name).call());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void registerSpell(String key, Callable<IMagicEffect> factory) {
        factories.put(key, factory);
    }
}
