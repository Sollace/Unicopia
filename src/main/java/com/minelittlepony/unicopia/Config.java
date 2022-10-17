package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.common.util.settings.*;

public class Config extends com.minelittlepony.common.util.settings.Config {
    public final Setting<Set<String>> speciesWhiteList = value("server", "speciesWhiteList", (Set<String>)new HashSet<String>())
            .addComment("A whitelist of races permitted on the server")
            .addComment("Races added to this list can be used by anyone,")
            .addComment("whilst any ones left off are not permitted")
            .addComment("An empty list disables whitelisting entirely.");

    public final Setting<Boolean> enableCheats = value("server", "enableCheats", false)
            .addComment("Allows use of the /race, /disguise, and /gravity commands");

    public final Setting<Race> preferredRace = value("client", "preferredRace", Race.EARTH)
            .addComment("The default preferred race")
            .addComment("This is the race a client requests when first joining a game")
            .addComment("It is the default used both when Mine Little Pony is not installed")
            .addComment("and when they respond with a human race.");

    public final Setting<Boolean> ignoreMineLP = value("client", "ignoreMineLP", false)
            .addComment("If true Mine Little Pony will not be considered when determining the race to use")
            .addComment("The result will always be what is set by this config file.");

    public final Setting<Boolean> disableWaterPlantsFix = value("compatibility", "disableWaterPlantsFix", false)
            .addComment("Disables this mod's built in fix for making sea plants waterlogged")
            .addComment("Turn this ON if you're using another mod that does something similar of if you encounter copatibility issues with other mods.");

    public Config() {
        super(HEIRARCHICAL_JSON_ADAPTER, GamePaths.getConfigDirectory().resolve("unicopia.json"));
    }
}
