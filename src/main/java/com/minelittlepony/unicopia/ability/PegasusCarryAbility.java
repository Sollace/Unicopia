package com.minelittlepony.unicopia.ability;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.client.network.packet.EntityPassengersSetS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Pegasi ability to pick up and carry other players
 */
public class PegasusCarryAbility implements Ability<Ability.Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.carry";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_K;
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canFly();
    }

    @Override
    public Hit tryActivate(Pony player) {
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
    public void apply(Pony iplayer, Hit data) {
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
    public void preApply(Pony player) {
    }

    @Override
    public void postApply(Pony player) {
    }

    public interface IPickupImmuned {

    }
}
