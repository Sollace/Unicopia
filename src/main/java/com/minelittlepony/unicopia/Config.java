package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.GsonBuilder;
import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.common.util.registry.RegistryTypeAdapter;
import com.minelittlepony.common.util.settings.*;

public class Config extends com.minelittlepony.common.util.settings.Config {
    public final Setting<Set<String>> speciesWhiteList = value("server", "speciesWhiteList", (Set<String>)new HashSet<String>())
            .addComment("A whitelist of races permitted on the server")
            .addComment("Races added to this list can be used by anyone,")
            .addComment("whilst any ones left off are not permitted")
            .addComment("An empty list disables whitelisting entirely.");

    public final Setting<Set<String>> wantItNeedItEntityExcludelist = value("server", "wantItNeedItEntityExcludelist", (Set<String>)new HashSet<>(Set.of("minecraft:creeper")))
            .addComment("A list of entity types that are immune to the want it need it spell's effects");

    public final Setting<Set<String>> dimensionsWithoutAtmosphere = value("server", "dimensionsWithoutAtmosphere", (Set<String>)new HashSet<String>())
            .addComment("A list of dimensions ids that do not have an atmosphere, and thus shouldn't allow pegasi to fly.");

    public final Setting<Boolean> enableCheats = value("server", "enableCheats", false)
            .addComment("Allows use of the /tribe, /unicopia disguise, and /unicopia gravity commands");

    public final Setting<Race> preferredRace = value("client", "preferredRace", Race.EARTH)
            .addComment("The default preferred race")
            .addComment("This is the race a client requests when first joining a game")
            .addComment("It is the default used both when Mine Little Pony is not installed")
            .addComment("and when they respond with a human race.");

    public final Setting<Boolean> ignoreMineLP = value("client", "ignoreMineLP", false)
            .addComment("If true Mine Little Pony will not be considered when determining the race to use")
            .addComment("The result will always be what is set by this config file.");

    public final Setting<Boolean> toggleAbilityKeys = value("client", "toggleAbilityKeys", false)
            .addComment("If true the ability keybinds will function as toggle keys rather than hold keys");

    public final Setting<Integer> hudPage = value("client", "hudActivePage", 0)
            .addComment("The page of abilities currently visible in the HUD. You can change this in-game using the PG_UP and PG_DWN keys (configurable)");

    public final Setting<Boolean> disableWaterPlantsFix = value("compatibility", "disableWaterPlantsFix", false)
            .addComment("Disables this mod's built in fix for making sea plants waterlogged")
            .addComment("Turn this ON if you're using another mod that does something similar of if you encounter copatibility issues with other mods.");

    public final Setting<Boolean> disableButterflySpawning = value("compatibility", "disableButterflySpawning", false)
            .addComment("Removes butterflies from spawning in your world")
            .addComment("Turn this ON if you have another mod that adds butterflies.");

    public final Setting<Boolean> simplifiedPortals = value("compatibility", "simplifiedPortals", false)
            .addComment("Disables dynamic portal rendering");

    public final Setting<Boolean> disableShaders = value("compatibility", "disableShaders", false)
            .addComment("Disables post-effect shaders used by the corruption mechanic");

    public final Setting<Long> fancyPortalRefreshRate = value("client", "fancyPortalRefreshRate", -1L)
            .addComment("Sets the refresh rate of portals when using fancy portal rendering")
            .addComment("Set to -1 (default) for unlimited");

    public final Setting<Integer> maxPortalRecursion = value("client", "maxPortalRecursion", 2)
            .addComment("Sets the maximum depth to reach when rendering portals through portals");

    public Config() {
        super(new HeirarchicalJsonConfigAdapter(new GsonBuilder()
                .registerTypeAdapter(Race.class, RegistryTypeAdapter.of(Race.REGISTRY))
        ), GamePaths.getConfigDirectory().resolve("unicopia.json"));
    }
}
