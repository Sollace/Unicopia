package com.minelittlepony.unicopia.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.player.IOwned;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class EntityFakeClientPlayer extends AbstractClientPlayer implements IOwned<UUID> {

    private final GameProfile profile;

    private NetworkPlayerInfo playerInfo;

    private UUID owner;

    public EntityFakeClientPlayer(World world, GameProfile profile) {
        super(world, profile);

        this.profile = profile;
    }

    @Nullable
    protected NetworkPlayerInfo getPlayerInfo() {
        if (playerInfo == null) {
            playerInfo = new NetworkPlayerInfo(profile);
        }

        return playerInfo;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
        /*noop*/
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return !(Minecraft.getMinecraft().player != null
                && Minecraft.getMinecraft().player.getGameProfile().getId().equals(getOwner()));
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public ITextComponent getDisplayName() {
        ITextComponent name = super.getDisplayName();
        name.getStyle().setItalic(true);
        return name;
    }
}
