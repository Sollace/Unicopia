package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.block.ITillable;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.SpellRecipe;
import com.minelittlepony.unicopia.forgebullshit.FBS;
import com.minelittlepony.unicopia.hud.UHud;
import com.minelittlepony.unicopia.input.Keyboard;
import com.minelittlepony.unicopia.inventory.gui.ContainerSpellBook;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IView;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.PowersRegistry;
import com.minelittlepony.unicopia.util.crafting.CraftingManager;
import com.minelittlepony.pony.data.IPony;

@Mod(
    modid = Unicopia.MODID,
    name = Unicopia.NAME,
    version = Unicopia.VERSION,
    dependencies = "required-after:jumpingcastle"
)
@EventBusSubscriber
public class Unicopia implements IGuiHandler {
    public static final String MODID = "unicopia";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";

    public static IChannel channel;

    /**
     * The race preferred by the client - as determined by mine little pony.
     * Human if minelp was not installed.
     *
     * This is not neccessarily the _actual_ race used for the player,
     * as the server may not allow certain race types, or the player may override
     * this option in-game themselves.
     */
    private static Race clientPlayerRace = getclientPlayerRace();

    private static CraftingManager craftingManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        UConfig.init(event.getModConfigurationDirectory());

        if (UClient.isClientSide()) {
            UEntities.preInit();
            UParticles.init();
        }
    }

    @SideOnly(Side.CLIENT)
    private static Race getclientPlayerRace() {
        if (!UConfig.getInstance().ignoresMineLittlePony()
                && Minecraft.getMinecraft().player != null
                && MineLP.modIsActive()) {
            Race race = Race.fromPonyRace(IPony.forPlayer(Minecraft.getMinecraft().player).getRace(false));

            if (!race.isDefault()) {
                return race;
            }
        }

        return UConfig.getInstance().getPrefferedRace();
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
        clientPlayerRace = getclientPlayerRace();
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
    }

    public static CraftingManager getCraftingManager() {
        return craftingManager;
    }

    @SubscribeEvent
    public static void registerItemsStatic(RegistryEvent.Register<Item> event) {
        UItems.registerItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItemColoursStatic(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
        UBlocks.registerColors(event.getItemColors(), event.getBlockColors());
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
        if (event.phase == Phase.END) {
            if (Minecraft.getMinecraft().player != null) {
                Race newRace = getclientPlayerRace();

                if (newRace != clientPlayerRace) {
                    clientPlayerRace = newRace;

                    channel.send(new MsgRequestCapabilities(Minecraft.getMinecraft().player, clientPlayerRace), Target.SERVER);
                }
            }

            Keyboard.getKeyHandler().onKeyInput();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void setupPlayerCamera(EntityViewRenderEvent.CameraSetup event) {

        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player != null) {
            IView view = PlayerSpeciesList.instance().getPlayer(player).getCamera();

            event.setRoll(view.calculateRoll());
            event.setPitch(view.calculatePitch(event.getPitch()));
            event.setYaw(view.calculateYaw(event.getYaw()));
        }
    }

    @SubscribeEvent
    public static void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();

        int fortuneFactor = 1 + event.getFortuneLevel() * 15;

        if (block == Blocks.STONE) {
            if (event.getWorld().rand.nextInt(500 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + event.getFortuneLevel(); i++) {
                    if (event.getWorld().rand.nextInt(10) > 3) {
                        event.getDrops().add(new ItemStack(UItems.curse, 1));
                    } else {
                        event.getDrops().add(new ItemStack(UItems.spell, 1));
                    }
                }
            }
        } else if (block instanceof BlockTallGrass) {
            if (event.getWorld().rand.nextInt(25 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + event.getFortuneLevel(); i++) {
                    int chance = event.getWorld().rand.nextInt(3);
                    if (chance == 0) {
                        event.getDrops().add(new ItemStack(UItems.alfalfa_seeds, 1));
                    } else if (chance == 1) {
                        event.getDrops().add(new ItemStack(UItems.apple_seeds, 1));
                    } else {
                        event.getDrops().add(new ItemStack(UItems.tomato_seeds, 1));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockTilled(UseHoeEvent event) {
        BlockPos pos = event.getPos();
        World world = event.getWorld();

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof ITillable) {
            ITillable farm = ((ITillable)state.getBlock());

            if (farm.canBeTilled(event.getCurrent(), event.getEntityPlayer(), world, state, pos)) {

                world.setBlockState(pos, farm.getFarmlandState(event.getCurrent(), event.getEntityPlayer(), world, state, pos));

                event.setResult(Result.ALLOW);
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

        event.getDrops().stream()
            .map(PlayerSpeciesList.instance()::getEntity)
            .forEach(item -> item.setPlayerSpecies(race));
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
        event.setNewfov(PlayerSpeciesList.instance().getPlayer(event.getEntity()).getCamera().calculateFieldOfView(event.getFov()));
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
