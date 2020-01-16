package com.minelittlepony.unicopia;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.powers.PowersRegistry;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.AffineIngredients;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.SpecialRecipe;
import com.minelittlepony.unicopia.enchanting.SpellRecipe;
import com.minelittlepony.unicopia.inventory.gui.SpellBookContainer;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.util.crafting.CraftingManager;
import com.minelittlepony.unicopia.world.Hooks;
import com.minelittlepony.unicopia.world.UWorld;

public class Unicopia implements IGuiHandler {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static final Logger log = LogManager.getLogger();

    private static IChannel channel;

    private static CraftingManager craftingManager = new CraftingManager(MODID, "enchanting") {
        @Override
        protected void registerRecipeTypes(Map<String, Function<JsonObject, IRecipe>> types) {
            super.registerRecipeTypes(types);

            types.put("unicopia:crafting_spell", SpellRecipe::deserialize);
            types.put("unicopia:crafting_special", SpecialRecipe::deserialize);

            AffineIngredients.instance().load();
        }
    };

    public static CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public static IChannel getConnection() {
        return channel;
    }

    public void preInit(FMLPreInitializationEvent event) {
        UConfig.init(event.getModConfigurationDirectory());
        UClient.instance().preInit();
        UWorld.instance().init();
    }

    public void onServerCreated(FMLServerAboutToStartEvent event) {
        Fixes.init(event.getServer().getDataFixer());
    }

    public void onServerStart(FMLServerStartingEvent event) {
        Commands.init(event);
    }

    public void init(FMLInitializationEvent event) {
        channel = JumpingCastle.subscribeTo(MODID, () -> {})
            .listenFor(MsgRequestCapabilities.class)
            .listenFor(MsgPlayerCapabilities.class)
            .listenFor(MsgPlayerAbility.class);

        PowersRegistry.instance().init();

        UAdvancements.init();

        FBS.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);

        UClient.instance().init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        craftingManager.load();

        Pages.instance().load();

        Biome.REGISTRY.forEach(UEntities::registerSpawnEntries);
        UClient.instance().postInit();

        UItems.fixRecipes();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0: return new SpellBookContainer(player.inventory, world, new BlockPos(x, y, z));
            default: return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0: return new GuiSpellBook(player);
            default: return null;
        }
    }
}
