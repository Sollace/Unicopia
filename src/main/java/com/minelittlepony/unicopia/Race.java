package com.minelittlepony.unicopia;

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

    public String getDisplayString() {
        return I18n.format(getTranslationString());
    }

    public String getTranslationString() {
        return String.format("unicopia.race.%s", name());
    }

    public boolean isSameAs(String s) {
        return name().equalsIgnoreCase(s)
                || getTranslationString().equalsIgnoreCase(s)
                || getDisplayString().equalsIgnoreCase(s);
    }

    public static Race fromName(String s) {
        if (!Strings.isNullOrEmpty(s)) {
            for (Race i : values()) {
                if (i.isSameAs(s)) return i;
            }
        }

        return fromId(s);
    }

    public static Race fromId(String s) {
        try {
            int id = Integer.parseInt(s);
            Race[] values = values();
            if (id >= 0 || id < values.length) {
                return values[id];
            }
        } catch (NumberFormatException e) { }

        return HUMAN;
    }

}
