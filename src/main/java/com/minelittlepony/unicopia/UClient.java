package com.minelittlepony.unicopia;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntityFakeServerPlayer;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class UClient {

    private static UClient instance;

    public static boolean isClientSide() {
        return false;
    }

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

    public void displayGuiToPlayer(PlayerEntity player, InteractionObject inventory) {
        player.displayGui(inventory);
    }

    @Nullable
    public PlayerEntity getPlayer() {
        return null;
    }

    @Nullable
    public IPlayer getIPlayer() {
        return SpeciesList.instance().getPlayer(getPlayer());
    }

    @Nullable
    public PlayerEntity getPlayerByUUID(UUID playerId) {
        return null;
    }

    public boolean isClientPlayer(@Nullable PlayerEntity player) {
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
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
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
