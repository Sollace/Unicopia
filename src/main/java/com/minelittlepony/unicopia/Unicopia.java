package com.minelittlepony.unicopia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.PowersRegistry;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.recipe.AffineIngredients;
import com.minelittlepony.unicopia.enchanting.recipe.SpecialRecipe;
import com.minelittlepony.unicopia.enchanting.recipe.SpellRecipe;
import com.minelittlepony.unicopia.inventory.gui.SpellBookContainer;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.world.UWorld;

public class Unicopia implements ModInitializer {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger();

    static InteractionManager interactionManager;

    private static IChannel channel;

    public static IChannel getConnection() {
        return channel;
    }

    @Override
    public void onInitialize() {
        Config.init(event.getModConfigurationDirectory());

        channel = JumpingCastle.subscribeTo(MODID, () -> {})
            .listenFor(MsgRequestCapabilities.class)
            .listenFor(MsgPlayerCapabilities.class)
            .listenFor(MsgPlayerAbility.class);

        PowersRegistry.instance().init();

        UAdvancements.init();

        craftingManager.load();

        Pages.instance().load();

        UBlocks.bootstrap();
        UItems.bootstrap();
        Commands.bootstrap();
        UContainers.bootstrap();

        UWorld.instance().init();

        InteractionManager.instance().preInit();
        InteractionManager.instance().init();
        InteractionManager.instance().postInit();

        UItems.fixRecipes();
    }
}
