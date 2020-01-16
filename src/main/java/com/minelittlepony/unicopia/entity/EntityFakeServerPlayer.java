package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.UClient;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class EntityFakeServerPlayer extends FakePlayer implements IOwned<EntityPlayer> {

    private EntityPlayer owner;

    public EntityFakeServerPlayer(WorldServer world, GameProfile profile) {
        super(world, profile);
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
    public EntityPlayer getOwner() {
        return owner;
    }

    @Override
    public void setOwner(EntityPlayer owner) {
        this.owner = owner;
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
    public ITextComponent getDisplayName() {
        ITextComponent name = super.getDisplayName();
        name.getStyle().setItalic(true);
        return name;
    }
}
