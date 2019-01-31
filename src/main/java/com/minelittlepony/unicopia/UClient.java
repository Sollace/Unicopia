package com.minelittlepony.unicopia;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class UClient {

    private static UClient instance;

    public static boolean isClientSide() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    @FUF(reason = "Forced client Separation")
    public static UClient instance() {
        if (instance == null) {
            if (isClientSide()) {
                instance = new UnicopiaClient();
            } else {
                instance = new UClient();
            }
        }

        return instance;
    }

    @FUF(reason = "Forced client Separation")
    public void displayGuiToPlayer(EntityPlayer player, IInteractionObject inventory) {
        player.displayGui(inventory);
    }

    @FUF(reason = "Forced client Separation")
    @Nullable
    public EntityPlayer getPlayer() {
        return null;
    }

    @FUF(reason = "Forced client Separation")
    @Nullable
    public EntityPlayer getPlayerByUUID(UUID playerId) {
        return null;
    }

    @FUF(reason = "Forced client Separation")
    public boolean isClientPlayer(@Nullable EntityPlayer player) {
        return false;
    }

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {}

    public void posInit(FMLPostInitializationEvent event) {}
}
