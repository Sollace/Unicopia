package com.minelittlepony.unicopia.util.dummy;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.Owned;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class DummyPlayerEntity extends PlayerEntity implements Owned<PlayerEntity> {

    private PlayerEntity owner;

    public DummyPlayerEntity(World world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected void onEquipStack(ItemStack stack) {
        /*noop*/
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
    public boolean shouldRenderName() {
        return !InteractionManager.instance().isClientPlayer(getOwner());
    }

    @Override
    public Text getDisplayName() {
        Text name = super.getDisplayName();
        name.getStyle().setItalic(true);
        return name;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
