package com.minelittlepony.unicopia.command;

public class Commands {
    public static void bootstrap() {
        event.registerServerCommand(new CommandOverrideGameMode());
        event.registerServerCommand(new CommandSpecies());
        event.registerServerCommand(new CommandRacelist());
        event.registerServerCommand(new CommandDisguise());
        event.registerServerCommand(new CommandGravity());

        event.getServer().setAllowFlight(true);
    }
}
