package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Numeric;
import com.minelittlepony.util.vector.VecHelper;

public class PowerCloudBase implements IPower<Numeric> {

    @Override
    public String getKeyName() {
        return "unicopia.power.cloud";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_J;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 10;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 5;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canInteractWithClouds();
    }

    @Override
    public Numeric tryActivate(IPlayer player) {
        EntityCloud cloud = findTarget(player);

        if (cloud != null) {
            Numeric data = new Numeric(player.getOwner().inventory.currentItem + 1);
            cloud.handlePegasusInteration(data.type);

            return data;
        }

        return null;
    }

    @Override
    public Class<Numeric> getPackageType() {
        return Numeric.class;
    }

    @Override
    public void apply(IPlayer player, Numeric data) {
        EntityCloud cloud = findTarget(player);

        if (cloud != null) {
            cloud.handlePegasusInteration(data.type);
        }
    }

    protected EntityCloud findTarget(IPlayer player) {
        if (player.getOwner().isRiding() && player.getOwner().getRidingEntity() instanceof EntityCloud) {
            return (EntityCloud)player.getOwner().getRidingEntity();
        }

        Object e = VecHelper.getLookedAtEntity(player.getOwner(), 18);

        if (e instanceof EntityCloud) {
            return (EntityCloud)e;
        }

        return null;
    }

    @Override
    public void preApply(IPlayer player) {
        IPower.spawnParticles(UParticles.UNICORN_MAGIC, player, 10);
    }

    @Override
    public void postApply(IPlayer player) {
        IPower.spawnParticles(UParticles.RAIN_DROPS, player, 5);
    }
}
