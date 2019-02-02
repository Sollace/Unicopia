package com.minelittlepony.unicopia.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.input.Keyboard;

public class PowersRegistry {

    private static PowersRegistry instance = new PowersRegistry();

    public static PowersRegistry instance() {
        return instance;
    }

    private final Map<Integer, List<IPower<? extends IData>>> keyToPowerMap = new HashMap<>();

    private final Map<String, IPower<? extends IData>> powerNamesMap = new HashMap<>();

    private PowersRegistry() {
    }

    public void init() {
        registerPower(new PowerTeleport());
        registerPower(new PowerMagic());
        registerPower(new PowerStomp());
        registerPower(new PowerGrow());
        registerPower(new PowerFeed());
        registerPower(new PowerCarry());
        registerPower(new PowerDisguise());
    }

    public boolean hasRegisteredPower(int keyCode) {
        return keyToPowerMap.containsKey(keyCode);
    }

    public Optional<IPower<? extends IData>> getCapablePowerFromKey(int keyCode, Race race) {
        return getKeyCodePool(keyCode).stream()
                .filter(power -> power.canUse(race))
                .findFirst();
    }

    public Optional<IPower<? extends IData>> getPowerFromName(String name) {
        return Optional.ofNullable(powerNamesMap.get(name));
    }

    private List<IPower<? extends IData>> getKeyCodePool(int keyCode) {
        return keyToPowerMap.computeIfAbsent(keyCode, ArrayList::new);
    }

    public void registerPower(IPower<? extends IData> power) {
        getKeyCodePool(power.getKeyCode()).add(power);
        powerNamesMap.put(power.getKeyName(), power);
        Keyboard.getKeyHandler().addKeybind(power);
    }
}
