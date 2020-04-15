package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.common.util.GamePaths;
import com.minelittlepony.jumpingcastle.api.Channel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.unicopia.ability.PowersRegistry;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.recipe.AffineIngredients;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.structure.UStructures;

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
        UBlocks.bootstrap();
        UItems.bootstrap();
        UContainers.bootstrap();
        UStructures.bootstrap();
        PowersRegistry.instance().init();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Pages.instance());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(AffineIngredients.instance());
    }
}
