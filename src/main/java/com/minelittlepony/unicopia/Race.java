package com.minelittlepony.unicopia;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

import net.minecraft.client.resources.I18n;

public enum Race {
    HUMAN(false, false, false),
    EARTH(false, false, true),
    UNICORN(true, false, false),
    PEGASUS(false, true, false),
    ALICORN(true, true, true),
    CHANGELING(false, true, false);

    private final boolean magic;
    private final boolean flight;
    private final boolean earth;

    private final static Map<Integer, Race> raceIdMap = new HashMap<>();
    static {
        for (Race race : values()) {
            raceIdMap.put(race.ordinal(), race);
        }
    }

    Race(boolean magic, boolean flight, boolean earth) {
        this.magic = magic;
        this.flight = flight;
        this.earth = earth;
    }

    public boolean isDefault() {
        return this == HUMAN;
    }

    public boolean canFly() {
        return flight;
    }

    public boolean canCast() {
        return magic;
    }

    public boolean canUseEarth() {
        return earth;
    }

    public boolean canInteractWithClouds() {
        return canFly() && this != CHANGELING;
    }

    public String getDisplayString() {
        return I18n.format(getTranslationString());
    }

    public String getTranslationString() {
        return String.format("unicopia.race.%s", name().toLowerCase());
    }

    public boolean isSameAs(String s) {
        return name().equalsIgnoreCase(s)
                || getTranslationString().equalsIgnoreCase(s)
                || getDisplayString().equalsIgnoreCase(s);
    }

    public static Race fromName(String s, Race def) {
        if (!Strings.isNullOrEmpty(s)) {
            for (Race i : values()) {
                if (i.isSameAs(s)) return i;
            }
        }

        try {
            return fromId(Integer.parseInt(s));
        } catch (NumberFormatException e) { }

        return def;
    }

    public static Race fromId(int id) {
        return raceIdMap.getOrDefault(id, HUMAN);
    }
}
