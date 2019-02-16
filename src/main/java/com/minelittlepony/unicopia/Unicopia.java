package com.minelittlepony.unicopia;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.SpellRecipe;
import com.minelittlepony.unicopia.forgebullshit.FBS;
import com.minelittlepony.unicopia.init.UEntities;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.inventory.gui.ContainerSpellBook;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.power.PowersRegistry;
import com.minelittlepony.unicopia.util.crafting.CraftingManager;
import com.minelittlepony.unicopia.world.UWorld;

@Mod(
    modid = Unicopia.MODID,
    name = Unicopia.NAME,
    version = Unicopia.VERSION,
    dependencies = "required-after:jumpingcastle"
)
public class Unicopia implements IGuiHandler {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static final Logger log = LogManager.getLogger();

    private static IChannel channel;

    private static CraftingManager craftingManager;

    public static CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public static IChannel getConnection() {
        return channel;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        UConfig.init(event.getModConfigurationDirectory());
        UClient.instance().preInit();
        UWorld.instance().init();

        MinecraftForge.TERRAIN_GEN_BUS.register(Unicopia.class);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        Commands.init(event);
    }

    @EventHandler
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

    @EventHandler
    public void posInit(FMLPostInitializationEvent event) {
        craftingManager = new CraftingManager(MODID, "enchanting") {
            @Override
            protected void registerRecipeTypes(Map<String, Function<JsonObject, IRecipe>> types) {
                super.registerRecipeTypes(types);

                types.put("unicopia:crafting_spell", SpellRecipe::deserialize);
            }
        };

        Pages.instance().load();

        Biome.REGISTRY.forEach(UEntities::registerSpawnEntries);
        UClient.instance().posInit();

        UItems.fixRecipes();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0: return new ContainerSpellBook(player.inventory, world, new BlockPos(x, y, z));
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
