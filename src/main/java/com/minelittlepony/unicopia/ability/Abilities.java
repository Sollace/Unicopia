package com.minelittlepony.unicopia.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.minelittlepony.unicopia.Race;

public final class Abilities {

    private static final Abilities INSTANCE = new Abilities();

    public static Abilities getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, List<Ability<? extends Ability.IData>>> keyToPowerMap = new HashMap<>();

    private final Map<String, Ability<? extends Ability.IData>> powerNamesMap = new HashMap<>();

    private Abilities() {
    }

    public void init() {
        register(new UnicornTeleportAbility());
        register(new UnicornCastingAbility());
        register(new EarthPonyGrowAbility());
        register(new ChangelingFeedAbility());
        register(new PegasusCarryAbility());
        register(new PegasusCloudInteractionAbility());
        register(new ChangelingTrapAbility());
        register(new EarthPonyStompAbility());
        register(new ChangelingDisguiseAbility());
    }

    public boolean hasRegisteredPower(int keyCode) {
        return keyToPowerMap.containsKey(keyCode);
    }

    public Optional<Ability<? extends Ability.IData>> getCapablePowerFromKey(int keyCode, Race race) {
        return getKeyCodePool(keyCode).stream()
                .filter(power -> power.canUse(race))
                .findFirst();
    }

    public Optional<Ability<? extends Ability.IData>> getPowerFromName(String name) {
        return Optional.ofNullable(powerNamesMap.get(name));
    }

    private List<Ability<? extends Ability.IData>> getKeyCodePool(int keyCode) {
        return keyToPowerMap.computeIfAbsent(keyCode, ArrayList::new);
    }

    public void register(Ability<? extends Ability.IData> power) {
        getKeyCodePool(power.getKeyCode()).add(power);
        powerNamesMap.put(power.getKeyName(), power);
    }

    public Collection<Ability<? extends Ability.IData>> getValues() {
        return powerNamesMap.values();
    }
}
