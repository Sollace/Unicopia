package com.minelittlepony.unicopia.core;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.jumpingcastle.api.Channel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.unicopia.core.ability.PowersRegistry;
import com.minelittlepony.unicopia.core.command.Commands;
import com.minelittlepony.unicopia.core.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.core.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.core.network.MsgRequestCapabilities;

public class UnicopiaCore implements ModInitializer {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger();

    public static InteractionManager interactionManager = new InteractionManager();

    private static Channel channel;

    public static Channel getConnection() {
        return channel;
    }

    @Override
    public void onInitialize() {
        Config.init(GamePaths.getConfigDirectory());

        channel = JumpingCastle.subscribeTo(MODID, () -> {})
            .listenFor(MsgRequestCapabilities.class)
            .listenFor(MsgPlayerCapabilities.class)
            .listenFor(MsgPlayerAbility.class);

        UTags.bootstrap();
        Commands.bootstrap();
        PowersRegistry.instance().init();
    }
}
