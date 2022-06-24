package com.minelittlepony.unicopia.entity.player.dummy;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class DummyServerPlayerEntity extends ServerPlayerEntity implements Owned<PlayerEntity> {

    private PlayerEntity owner;

    public DummyServerPlayerEntity(ServerWorld world, GameProfile profile) {
        super(world.getServer(), world, profile, null);
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
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
}
