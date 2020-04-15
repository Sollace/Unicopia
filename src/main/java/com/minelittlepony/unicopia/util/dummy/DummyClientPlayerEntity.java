package com.minelittlepony.unicopia.util.dummy;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.Owned;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class DummyClientPlayerEntity extends AbstractClientPlayerEntity implements Owned<PlayerEntity> {

    private PlayerListEntry playerInfo;

    private PlayerEntity owner;

    public DummyClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public boolean isSpectator() {
        return getPlayerListEntry().getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return getPlayerListEntry().getGameMode() == GameMode.CREATIVE;
    }

    @Override
    @Nonnull
    protected PlayerListEntry getPlayerListEntry() {
        if (playerInfo == null) {
            ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();

            playerInfo = connection.getPlayerListEntry(getGameProfile().getId());

            if (playerInfo == null) {
                playerInfo = new PlayerListEntry(getGameProfile());
            }
        }

        return playerInfo;
    }

    @Override
    protected void onEquipStack(ItemStack stack) {
        /*noop*/
    }

    @Override
    public boolean shouldRenderName() {
        return !InteractionManager.instance().isClientPlayer(getOwner());
    }

    @Override
    public PlayerEntity getOwner() {
        return owner;
    }

    @Override
    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public Text getDisplayName() {
        Text name = super.getDisplayName();
        name.getStyle().setItalic(true);
        return name;
    }
}
