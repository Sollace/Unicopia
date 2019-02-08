package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
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
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.JumpingCastle;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.block.ITillable;
import com.minelittlepony.unicopia.command.Commands;
import com.minelittlepony.unicopia.enchanting.Pages;
import com.minelittlepony.unicopia.enchanting.SpellRecipe;
import com.minelittlepony.unicopia.forgebullshit.FBS;
import com.minelittlepony.unicopia.inventory.gui.ContainerSpellBook;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;
import com.minelittlepony.unicopia.network.MsgPlayerAbility;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.PowersRegistry;
import com.minelittlepony.unicopia.spell.SpellRegistry;
import com.minelittlepony.unicopia.util.crafting.CraftingManager;

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

    public static final Logger log = LogManager.getLogger();

    public static IChannel channel;

    private static CraftingManager craftingManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        UConfig.init(event.getModConfigurationDirectory());
        UClient.instance().preInit(event);
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

        UClient.instance().init(event);
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
        UClient.instance().posInit(event);

        UItems.fixRecipes();
    }

    public static CraftingManager getCraftingManager() {
        return craftingManager;
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        UItems.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        UBlocks.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        UEffects.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        USounds.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        UEntities.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();

        int fortuneFactor = 1 + event.getFortuneLevel() * 15;

        if (block == Blocks.STONE) {
            if (event.getWorld().rand.nextInt(500 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + event.getFortuneLevel(); i++) {
                    if (event.getWorld().rand.nextInt(10) > 3) {
                        event.getDrops().add(new ItemStack(UItems.curse));
                    } else {
                        event.getDrops().add(new ItemStack(UItems.spell));
                    }
                }
            }

            if (event.getWorld().rand.nextInt(5000) == 0) {
                event.getDrops().add(SpellRegistry.instance().enchantStack(new ItemStack(UItems.spell), "awkward"));
            }
        } else if (block instanceof BlockTallGrass) {
            if (event.getWorld().rand.nextInt(25 / fortuneFactor) == 0) {
                for (int i = 0; i < 1 + event.getFortuneLevel(); i++) {
                    int chance = event.getWorld().rand.nextInt(3);
                    if (chance == 0) {
                        event.getDrops().add(new ItemStack(UItems.alfalfa_seeds));
                    } else if (chance == 1) {
                        event.getDrops().add(new ItemStack(UItems.apple_seeds));
                    } else {
                        event.getDrops().add(new ItemStack(UItems.tomato_seeds));
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
    public void onServerStart(FMLServerStartingEvent event) {
        Commands.init(event);
    }

    @SubscribeEvent
    public static void onItemUseBegin(LivingEntityUseItemEvent.Start event) {
        Entity e = event.getEntity();

        if (!event.isCanceled() && e instanceof EntityPlayer) {
            PlayerSpeciesList.instance().getPlayer((EntityPlayer)e).getFood().begin(event.getItem());
        }
    }

    @SubscribeEvent
    public static void onItemUseCancel(LivingEntityUseItemEvent.Stop event) {
        Entity e = event.getEntity();

        if (!event.isCanceled() && e instanceof EntityPlayer) {
            PlayerSpeciesList.instance().getPlayer((EntityPlayer)e).getFood().end();
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        Entity e = event.getEntity();

        if (!event.isCanceled() && e instanceof EntityPlayer) {
            PlayerSpeciesList.instance().getPlayer((EntityPlayer)e).getFood().finish();
        }
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
