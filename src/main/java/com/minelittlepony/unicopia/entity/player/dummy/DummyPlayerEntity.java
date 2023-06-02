package com.minelittlepony.unicopia.entity.player.dummy;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DummyPlayerEntity extends PlayerEntity implements Owned<PlayerEntity>, Owned.Mutable<PlayerEntity> {

    private PlayerEntity owner;

    public DummyPlayerEntity(World world, GameProfile profile) {
        super(world, BlockPos.ORIGIN, 0, profile);
    }

    @Override
    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        /*noop*/
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        /*noop*/
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

    @Override
    public boolean shouldRenderName() {
        return !InteractionManager.instance().isClientPlayer(getMaster());
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
