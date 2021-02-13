package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.entity.effect.UPotions;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;

public class Unicopia implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    private static Config CONFIG;

    public static Config getConfig() {
        if (CONFIG == null) {
            CONFIG = new Config();
        }
        return CONFIG;
    }

    public Unicopia() {
        getConfig();
    }

    @Override
    public void onInitialize() {
        Channel.bootstrap();
        UTags.bootstrap();
        Commands.bootstrap();

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            AwaitTickQueue.tick(w);
            ((BlockDestructionManager.Source)w).getDestructionManager().tick();
        });
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TreeTypeLoader.INSTANCE);

        UItems.bootstrap();
        UPotions.bootstrap();
    }
}
