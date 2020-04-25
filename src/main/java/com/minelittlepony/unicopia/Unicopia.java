package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.advancement.BOHDeathCriterion;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.container.UContainers;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.recipe.AffineIngredients;
import com.minelittlepony.unicopia.enchanting.recipe.URecipes;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.mixin.CriterionsRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.structure.UStructures;

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
        UBlocks.bootstrap();
        UItems.bootstrap();
        UContainers.bootstrap();
        UStructures.bootstrap();
        URecipes.bootstrap();
        Abilities.getInstance().init();

        CriterionsRegistry.register(BOHDeathCriterion.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Pages.instance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AffineIngredients.getInstance());
    }
}
