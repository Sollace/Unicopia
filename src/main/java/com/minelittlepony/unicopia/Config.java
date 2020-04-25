package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.common.util.settings.JsonConfig;

public class Config extends JsonConfig {

    @Deprecated
    public static Config getInstance() {
        return Unicopia.getConfig();
    }

    public Config() {
        super(GamePaths.getConfigDirectory().resolve("unicopia.json"));
    }

    @Expose(deserialize = false)
    private final String speciesWhiteListComment =
            "A whitelist of races permitted on the server. " +
            "Races added to this list can be used by anyone, whilst any ones left off are not permitted. " +
            "An empty list disables whitelisting entirely.";
    @Expose
    private final Set<Race> speciesWhiteList = new HashSet<>();

    @Expose(deserialize = false)
    private final String preferredRaceComment =
            "The default preferred race. " +
            "This is the race a client requests when first joining a game. " +
            "It is the default used both when Mine Little Pony is not installed and when they respond with a human race.";
    @Expose
    private Race preferredRace = Race.EARTH;

    @Expose(deserialize = false)
    private final String ignoreMineLPComment =
            "If true Mine Little Pony will not be considered when determining the race to use. " +
            "The result will always be what is set by this config file.";
    @Expose
    private boolean ignoreMineLP = false;

    public Set<Race> getSpeciesWhiteList() {
        return speciesWhiteList;
    }

    public boolean ignoresMineLittlePony() {
        return ignoreMineLP;
    }

    public void setIgnoreMineLittlePony(boolean value) {
        if (value != ignoreMineLP) {
            ignoreMineLP = value;
            save();
        }
    }

    public void setPreferredRace(Race race) {
        if (preferredRace != race) {
            preferredRace = race;
            save();
        }
    }

    public Race getPrefferedRace() {
        if (preferredRace == null) {
            setPreferredRace(Race.EARTH);
        }

        return preferredRace;
    }
}
