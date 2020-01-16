package com.minelittlepony.unicopia.entity;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UClient;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class EntityFakeClientPlayer extends AbstractClientPlayer implements IOwned<EntityPlayer> {

    private NetworkPlayerInfo playerInfo;

    private EntityPlayer owner;

    public EntityFakeClientPlayer(World world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    @Nullable
    protected NetworkPlayerInfo getPlayerInfo() {
        if (playerInfo == null) {
            NetHandlerPlayClient connection = MinecraftClient.getInstance().getConnection();

            playerInfo = connection.getPlayerInfo(getGameProfile().getId());

            if (playerInfo == null) {
                playerInfo = new NetworkPlayerInfo(getGameProfile());
            }
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
    public boolean getAlwaysRenderNameTag() {
        return !UClient.instance().isClientPlayer(getOwner());
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return getAlwaysRenderNameTag();
    }

    @Override
    public EntityPlayer getOwner() {
        return owner;
    }

    @Override
    public void setOwner(EntityPlayer owner) {
        this.owner = owner;
    }

    @Override
    public ITextComponent getDisplayName() {
        ITextComponent name = super.getDisplayName();
        name.getStyle().setItalic(true);
        return name;
    }
}
