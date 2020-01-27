package com.minelittlepony.unicopia.core.command;

import net.fabricmc.fabric.api.registry.CommandRegistry;

public class Commands {
    public static void bootstrap() {
        CommandRegistry.INSTANCE.register(false, SpeciesCommand::register);
        CommandRegistry.INSTANCE.register(false, RacelistCommand::register);
        CommandRegistry.INSTANCE.register(false, GravityCommand::register);

        // TODO:
        //event.getServer().setAllowFlight(true);
    }
}
