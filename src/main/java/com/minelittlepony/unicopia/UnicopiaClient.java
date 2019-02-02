package com.minelittlepony.unicopia;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.hud.UHud;
import com.minelittlepony.unicopia.input.Keyboard;
import com.minelittlepony.unicopia.inventory.gui.GuiOfHolding;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IView;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.gui.ButtonGridLayout;
import com.minelittlepony.util.gui.UButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.minelittlepony.util.gui.ButtonGridLayout.*;

@EventBusSubscriber(Side.CLIENT)
public class UnicopiaClient extends UClient {

    /**
     * The race preferred by the client - as determined by mine little pony.
     * Human if minelp was not installed.
     *
     * This is not neccessarily the _actual_ race used for the player,
     * as the server may not allow certain race types, or the player may override
     * this option in-game themselves.
     */
    private static Race clientPlayerRace = getclientPlayerRace();

    private static Race getclientPlayerRace() {
        if (!UConfig.getInstance().ignoresMineLittlePony()
                && Minecraft.getMinecraft().player != null) {
            Race race = MineLP.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }


        return UConfig.getInstance().getPrefferedRace();
    }


    @Override
    public void displayGuiToPlayer(EntityPlayer player, IInteractionObject inventory) {
        if (player instanceof EntityPlayerSP) {
            if ("unicopia:itemofholding".equals(inventory.getGuiID())) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiOfHolding(inventory));
            }
        } else {
            super.displayGuiToPlayer(player, inventory);
        }
    }

    @Override
    @Nullable
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerByUUID(UUID playerId) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.getUniqueID().equals(playerId)) {
            return mc.player;
        }

        return mc.world.getPlayerEntityByUUID(playerId);
    }

    @Override
    public boolean isClientPlayer(@Nullable EntityPlayer player) {
        if (getPlayer() == player) {
            return true;
        }

        if (getPlayer() == null || player == null) {
            return false;
        }

        return getPlayer().getGameProfile().getId().equals(player.getGameProfile().getId());
    }

    @Override
    public int getViewMode() {
        return Minecraft.getMinecraft().gameSettings.thirdPersonView;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onDisplayGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiOptions) {
            addUniButton(event);
        }
    }

    static void addUniButton(GuiScreenEvent.InitGuiEvent.Post event) {
        ButtonGridLayout layout = new ButtonGridLayout(event.getButtonList());

        GuiButton uni = new UButton(layout.getNextButtonId(), 0, 0, 150, 20, I18n.format("gui.unicopia"), b -> {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_ANVIL_USE, 1));
            b.displayString = "<< WIP >>";

            return false;
        });

        List<Integer> possibleXCandidates = list(layout.getColumns());
        List<Integer> possibleYCandidates = list(layout.getRows());

        uni.y = last(possibleYCandidates, 1);

        if (layout.getRows()
                .filter(y -> layout.getRow(y).size() == 1).count() < 2) {
            uni.y += 25;
            uni.x = first(possibleXCandidates, 0);

            layout.getRow(last(possibleYCandidates, 0)).forEach(button -> {
                button.y = Math.max(button.y, uni.y + uni.height + 13);
            });
        } else {
            uni.x = first(possibleXCandidates, 2);
        }

        layout.getElements().add(uni);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void preEntityRender(RenderLivingEvent.Pre<?> event) {
        if (event.getEntity() instanceof EntityPlayer) {
            if (PlayerSpeciesList.instance().getPlayer((EntityPlayer)event.getEntity()).isInvisible()) {
                event.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        UEntities.preInit();
        UParticles.init();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent event) {
        clientPlayerRace = getclientPlayerRace();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemColours(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
        UBlocks.registerColors(event.getItemColors(), event.getBlockColors());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.world != null) {
            IPlayer player = PlayerSpeciesList.instance().getPlayer(mc.player);

            UHud.instance.renderHud(player, event.getResolution());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void modifyFOV(FOVUpdateEvent event) {
        event.setNewfov(PlayerSpeciesList.instance().getPlayer(event.getEntity()).getCamera().calculateFieldOfView(event.getFov()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            EntityPlayer player = UClient.instance().getPlayer();

            if (player != null) {
                Race newRace = getclientPlayerRace();

                if (newRace != clientPlayerRace) {
                    clientPlayerRace = newRace;

                    Unicopia.channel.send(new MsgRequestCapabilities(player, clientPlayerRace), Target.SERVER);
                }
            }

            Keyboard.getKeyHandler().onKeyInput();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void setupPlayerCamera(EntityViewRenderEvent.CameraSetup event) {

        EntityPlayer player = UClient.instance().getPlayer();

        if (player != null) {
            IView view = PlayerSpeciesList.instance().getPlayer(player).getCamera();

            event.setRoll(view.calculateRoll());
            event.setPitch(view.calculatePitch(event.getPitch()));
            event.setYaw(view.calculateYaw(event.getYaw()));
        }
    }
}
