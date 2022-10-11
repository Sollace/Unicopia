package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.common.util.settings.JsonConfig;
import com.minelittlepony.common.util.settings.Setting;

public class Config extends JsonConfig {
    /*private final String speciesWhiteListComment =
            "A whitelist of races permitted on the server. " +
            "Races added to this list can be used by anyone, whilst any ones left off are not permitted. " +
            "An empty list disables whitelisting entirely.";*/

    public final Setting<Set<Race>> speciesWhiteList = value("server", "speciesWhiteList", new HashSet<>());

    public final Setting<Boolean> enableCheats = value("server", "enableCheats", false);

    /*private final String preferredRaceComment =
            "The default preferred race. " +
            "This is the race a client requests when first joining a game. " +
            "It is the default used both when Mine Little Pony is not installed and when they respond with a human race.";*/

    public final Setting<Race> preferredRace = value("client", "preferredRace", Race.EARTH);

    /*private final String ignoreMineLPComment =
            "If true Mine Little Pony will not be considered when determining the race to use. " +
            "The result will always be what is set by this config file.";*/
    public final Setting<Boolean> ignoreMineLP = value("client", "ignoreMineLP", false);

    public final Setting<Boolean> disableWaterPlantsFix = value("compatibility", "disableWaterPlantsFix", false);

    public Config() {
        super(GamePaths.getConfigDirectory().resolve("unicopia.json"));
    }
}
