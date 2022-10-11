package com.minelittlepony.unicopia;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.Arguments;

public final class Debug {
    public static final boolean DEBUG_SPELLBOOK_CHAPTERS;
    public static final boolean DEBUG_COMMANDS;

    static {
        Arguments args = new Arguments();
        args.parse(FabricLoader.getInstance().getLaunchArguments(true));
        DEBUG_SPELLBOOK_CHAPTERS = "true".equalsIgnoreCase(args.getOrDefault("unicopia.debug.spellbookChapters", "false"));
        DEBUG_COMMANDS = "true".equalsIgnoreCase(args.getOrDefault("unicopia.debug.commands", "false"));
    }
}
