package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.world.World;

public class PowerCarry implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.carry";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_K;
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
    public Hit tryActivate(EntityPlayer player, World w) {
        return new Hit();
    }

    protected EntityLivingBase findRider(EntityPlayer player, World w) {
        Entity hit = VecHelper.getLookedAtEntity(player, 10);

        if (hit instanceof EntityLivingBase && !player.isRidingOrBeingRiddenBy(hit)) {
            if (!(hit instanceof EntityCloud)) {
                return (EntityLivingBase)hit;
            }
        }

        return null;
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(EntityPlayer player, Hit data) {
        EntityLivingBase rider = findRider(player, player.world);

        if (rider != null) {
            rider.startRiding(player, true);
        } else {
            player.removePassengers();
        }

        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP)player).getServerWorld().getEntityTracker().sendToTrackingAndSelf(player, new SPacketSetPassengers(player));
        }
    }

    @Override
    public void preApply(IPlayer player) {
    }

    @Override
    public void postApply(IPlayer player) {
    }
}
