package com.minelittlepony.unicopia.core.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.minelittlepony.unicopia.core.Race;

public class PowersRegistry {

    private static final PowersRegistry INSTANCE = new PowersRegistry();

    public static PowersRegistry instance() {
        return INSTANCE;
    }

    private final Map<Integer, List<IPower<? extends IPower.IData>>> keyToPowerMap = new HashMap<>();

    private final Map<String, IPower<? extends IPower.IData>> powerNamesMap = new HashMap<>();

    private PowersRegistry() {
    }

    public void init() {
        registerPower(new PowerTeleport());
        registerPower(new PowerMagic());
        registerPower(new PowerGrow());
        registerPower(new PowerFeed());
        registerPower(new PowerCarry());
    }

    public boolean hasRegisteredPower(int keyCode) {
        return keyToPowerMap.containsKey(keyCode);
    }

    public Optional<IPower<? extends IPower.IData>> getCapablePowerFromKey(int keyCode, Race race) {
        return getKeyCodePool(keyCode).stream()
                .filter(power -> power.canUse(race))
                .findFirst();
    }

    public Optional<IPower<? extends IPower.IData>> getPowerFromName(String name) {
        return Optional.ofNullable(powerNamesMap.get(name));
    }

    private List<IPower<? extends IPower.IData>> getKeyCodePool(int keyCode) {
        return keyToPowerMap.computeIfAbsent(keyCode, ArrayList::new);
    }

    public void registerPower(IPower<? extends IPower.IData> power) {
        getKeyCodePool(power.getKeyCode()).add(power);
        powerNamesMap.put(power.getKeyName(), power);
    }

    public Collection<IPower<? extends IPower.IData>> getValues() {
        return powerNamesMap.values();
    }
}
