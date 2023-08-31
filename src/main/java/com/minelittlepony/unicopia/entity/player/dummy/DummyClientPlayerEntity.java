package com.minelittlepony.unicopia.entity.player.dummy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

public class DummyClientPlayerEntity extends AbstractClientPlayerEntity implements Owned<PlayerEntity>, Owned.Mutable<PlayerEntity> {

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
    @NotNull
    protected PlayerListEntry getPlayerListEntry() {
        if (playerInfo == null) {
            ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();

            playerInfo = connection.getPlayerListEntry(getGameProfile().getId());

            if (playerInfo == null) {
                playerInfo = new PlayerListEntry(getGameProfile(), false);
            }
        }

        return playerInfo;
    }

    @Override
    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        /*noop*/
    }

    @Override
    public boolean shouldRenderName() {
        return !InteractionManager.instance().isClientPlayer(getMaster());
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return owner == null ? super.isPartVisible(modelPart) : owner.isPartVisible(modelPart);
    }

    @Override
    @Nullable
    public PlayerEntity getMaster() {
        return owner;
    }

    @Override
    public void setMaster(PlayerEntity owner) {
        this.owner = owner;
    }
}
