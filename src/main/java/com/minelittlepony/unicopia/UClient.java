package com.minelittlepony.unicopia;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntityFakeServerPlayer;
import com.minelittlepony.unicopia.forgebullshit.FUF;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;

public class UClient {

    private static UClient instance;

    public static boolean isClientSide() {
        return net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient();
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

    @Nullable
    public IPlayer getIPlayer() {
        return PlayerSpeciesList.instance().getPlayer(getPlayer());
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

    public int getViewMode() {
        return 0;
    }

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of EntityPlayer appropriate to the side being called on.
     */
    @Nonnull
    public EntityPlayer createPlayer(Entity observer, GameProfile profile) {
        return new EntityFakeServerPlayer((WorldServer)observer.world, profile);
    }

    public void postRenderEntity(Entity entity) {

    }

    public boolean renderEntity(Entity entity, float renderPartialTicks) {
        return false;
    }

    public void tick() {}

    public void preInit() {}

    public void init() {}

    public void postInit() {}
}
