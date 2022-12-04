package com.minelittlepony.unicopia;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;

public final class Debug {
    public static final boolean DEBUG_SPELLBOOK_CHAPTERS;
    public static final boolean DEBUG_COMMANDS;

    static {
        Map<String, String> args = parseArguments(FabricLoader.getInstance().getLaunchArguments(true));
        DEBUG_SPELLBOOK_CHAPTERS = "true".equalsIgnoreCase(args.getOrDefault("unicopia.debug.spellbookChapters", "false"));
        DEBUG_COMMANDS = "true".equalsIgnoreCase(args.getOrDefault("unicopia.debug.commands", "false"));
    }

    static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String next = i < args.length - 1 ? args[i + 1] : null;

            if (arg.startsWith("--") && next != null && !next.startsWith("--")) {
                arguments.put(arg, next);
                i++;
            }
        }

        return arguments;
    }
}
