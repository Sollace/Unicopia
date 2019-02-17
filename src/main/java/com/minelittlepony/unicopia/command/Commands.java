package com.minelittlepony.unicopia.command;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class Commands {

    public static void init(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandOverrideGameMode());
        event.registerServerCommand(new CommandSpecies());
        event.registerServerCommand(new CommandRacelist());
        event.registerServerCommand(new CommandDisguise());

        event.getServer().setAllowFlight(true);
    }
}
