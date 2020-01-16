package com.minelittlepony.unicopia.ability.powers;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.Hit;
import com.minelittlepony.unicopia.ability.IPower;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.util.VecHelper;

import net.minecraft.client.network.packet.EntityPassengersSetS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class PowerCarry implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.carry";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_K;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 0;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 10;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canFly();
    }

    @Override
    public Hit tryActivate(IPlayer player) {
        return new Hit();
    }

    protected LivingEntity findRider(PlayerEntity player, World w) {
        Entity hit = VecHelper.getLookedAtEntity(player, 10);

        if (hit instanceof LivingEntity && !player.isConnectedThroughVehicle(hit)) {
            if (!(hit instanceof IPickupImmuned)) {
                return (LivingEntity)hit;
            }
        }

        return null;
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(IPlayer iplayer, Hit data) {
        PlayerEntity player = iplayer.getOwner();
        LivingEntity rider = findRider(player, iplayer.getWorld());

        if (rider != null) {
            rider.startRiding(player, true);
        } else {
            player.removeAllPassengers();
        }

        if (player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)player).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
        }
    }

    @Override
    public void preApply(IPlayer player) {
    }

    @Override
    public void postApply(IPlayer player) {
    }

    public interface IPickupImmuned {

    }
}
