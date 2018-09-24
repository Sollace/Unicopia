package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.client.particle.EntityMagicFX;
import com.minelittlepony.unicopia.client.particle.EntityRaindropFX;
import com.minelittlepony.unicopia.client.particle.Particles;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.hud.UHud;
import com.minelittlepony.unicopia.input.Keyboard;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.PowersRegistry;
import com.minelittlepony.pony.data.IPony;

import come.minelittlepony.unicopia.forgebullshit.FBS;

@Mod(modid = Unicopia.MODID, name = Unicopia.NAME, version = Unicopia.VERSION)
@EventBusSubscriber
public class Unicopia {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static IChannel channel;

    public static int MAGIC_PARTICLE;
    public static int RAIN_PARTICLE;

    /**
     * The race preferred by the client - as determined by mine little pony.
     * Human if minelp was not installed.
     *
     * This is not neccessarily the _actual_ race used for the player,
     * as the server may not allow certain race types, or the player may override
     * this option in-game themselves.
     */
    private static Race clientPlayerRace = getclientPlayerRace();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (UClient.isClientSide()) {
            UEntities.preInit();
        }
    }

    @SideOnly(Side.CLIENT)
    private static Race getclientPlayerRace() {
        if (Minecraft.getMinecraft().player != null && MineLP.modIsActive()) {
            return Race.fromPonyRace(IPony.forPlayer(Minecraft.getMinecraft().player).getRace(false));
        }

        return Race.HUMAN;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        channel = JumpingCastle.subscribeTo(MODID, () -> {})
            .listenFor(MsgRequestCapabilities.class)
            .listenFor(MsgPlayerCapabilities.class)
            .listenFor(MsgPlayerAbility.class);

        MAGIC_PARTICLE = Particles.instance().registerParticle(new EntityMagicFX.Factory());
        RAIN_PARTICLE = Particles.instance().registerParticle(new EntityRaindropFX.Factory());

        PowersRegistry.instance().init();

        UAdvancements.init();

        FBS.init();
    }

    @SubscribeEvent
    public static void registerItemsStatic(RegistryEvent.Register<Item> event) {
        UItems.registerItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItemColoursStatic(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
    }

    @SubscribeEvent
    public static void registerBlocksStatic(RegistryEvent.Register<Block> event) {
        UBlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<IRecipe> event) {
        UItems.registerRecipes(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipesStatic(RegistryEvent.Register<SoundEvent> event) {
        USounds.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntitiesStatic(RegistryEvent.Register<EntityEntry> event) {
        UEntities.init(event.getRegistry());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGameTick(TickEvent.ClientTickEvent event) {
        Race newRace = getclientPlayerRace();

        if (newRace != clientPlayerRace && Minecraft.getMinecraft().player != null) {
            clientPlayerRace = newRace;

            channel.send(new MsgRequestCapabilities(Minecraft.getMinecraft().player, clientPlayerRace), Target.SERVER);
        }

        if (event.phase == Phase.END) {
            Keyboard.getKeyHandler().onKeyInput();
        }
    }

    @SubscribeEvent
    public static void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        if (event.getState().getBlock() == Blocks.STONE) {
            int fortuneFactor = 1 + event.getFortuneLevel() * 15;
            System.out.println(event.getFortuneLevel());

            if (event.getWorld().rand.nextInt(500 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + event.getFortuneLevel(); i++) {
                    if (event.getWorld().rand.nextInt(10) > 3) {
                        event.getDrops().add(new ItemStack(UItems.curse, 1));
                    } else {
                        event.getDrops().add(new ItemStack(UItems.spell, 1));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == Phase.END) {
            PlayerSpeciesList.instance().getPlayer(event.player).onUpdate(event.player);
        } else {
            PlayerSpeciesList.instance().getPlayer(event.player).beforeUpdate(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTossItem(ItemTossEvent event) {
        Race race = PlayerSpeciesList.instance().getPlayer(event.getPlayer()).getPlayerSpecies();

        PlayerSpeciesList.instance().getEntity(event.getEntityItem()).setPlayerSpecies(race);
    }

    @SubscribeEvent
    public static void onPlayerDropItems(PlayerDropsEvent event) {

        Race race = PlayerSpeciesList.instance().getPlayer(event.getEntityPlayer()).getPlayerSpecies();

        event.getDrops().stream().map(PlayerSpeciesList.instance()::getEntity).forEach(item -> {
            item.setPlayerSpecies(race);
        });
    }

    @SubscribeEvent
    public static void onPlayerFall(PlayerFlyableFallEvent event) {
        PlayerSpeciesList.instance()
            .getPlayer(event.getEntityPlayer())
            .onFall(event.getDistance(), event.getMultiplier());
    }

    @EventHandler
    public void onServerStarted(FMLServerStartingEvent event) {
        Commands.init(event);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.ALL) {
            return;
        }

        if (UClient.isClientSide()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null && mc.world != null) {
                IPlayer player = PlayerSpeciesList.instance().getPlayer(mc.player);

                UHud.instance.renderHud(player, event.getResolution());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        // Why won't you run!?
        if (!event.isCanceled() && event.getItemStack().getItemUseAction() == EnumAction.EAT) {
            PlayerSpeciesList.instance()
                .getPlayer(event.getEntityPlayer())
                .onEntityEat();
        }
    }

    @SubscribeEvent
    public static void modifyFOV(FOVUpdateEvent event) {
        float fov = event.getFov();

        fov += PlayerSpeciesList.instance().getPlayer(event.getEntity()).getExertion() / 5;

        event.setNewfov(fov);
    }
}
