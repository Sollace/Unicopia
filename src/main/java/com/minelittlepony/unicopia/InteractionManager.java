package com.minelittlepony.unicopia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.client.ClientInteractionManager;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface InteractionManager {

    static InteractionManager instance() {
        if (Unicopia.interactionManager == null) {
            if (ServerInteractionManager.isClientSide()) {
                Unicopia.interactionManager = new ClientInteractionManager();
            } else {
                Unicopia.interactionManager = new ServerInteractionManager();
            }
        }

        return Unicopia.interactionManager;
    }

    @Nullable
    PlayerEntity getClientPlayer();

    @Nullable
    default IPlayer getIPlayer() {
        return SpeciesList.instance().getPlayer(getClientPlayer());
    }

    boolean isClientPlayer(@Nullable PlayerEntity player);

    int getViewMode();

    /**
     * Side-independent method to create a new player.
     *
     * Returns an implementation of PlayerEntity appropriate to the side being called on.
     */
    @Nonnull
    PlayerEntity createPlayer(Entity observer, GameProfile profile);

    void postRenderEntity(Entity entity);

    boolean renderEntity(Entity entity, float renderPartialTicks);

}
