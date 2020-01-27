package com.minelittlepony.unicopia.redux.ability;

import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.ability.IPower;
import com.minelittlepony.unicopia.core.ability.Numeric;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.core.util.VecHelper;

import net.minecraft.entity.Entity;

public class PowerCloudBase implements IPower<Numeric> {

    @Override
    public String getKeyName() {
        return "unicopia.power.cloud";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_J;
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
        return findTarget(player).map(cloud -> {
            Numeric data = new Numeric(player.getOwner().inventory.selectedSlot + 1);
            cloud.handlePegasusInteration(data.type);

            return data;
        }).orElse(null);
    }

    @Override
    public Class<Numeric> getPackageType() {
        return Numeric.class;
    }

    @Override
    public void apply(IPlayer player, Numeric data) {
        findTarget(player).ifPresent(cloud -> {
            cloud.handlePegasusInteration(data.type);
        });
    }

    protected Optional<ICloudEntity> findTarget(IPlayer player) {
        if (player.getOwner().hasVehicle() && player.getOwner().getVehicle() instanceof ICloudEntity) {
            return Optional.ofNullable((ICloudEntity)player.getOwner().getVehicle());
        }

        Entity e = VecHelper.getLookedAtEntity(player.getOwner(), 18);

        if (e instanceof ICloudEntity) {
            return Optional.of((ICloudEntity)e);
        }

        return Optional.empty();
    }

    @Override
    public void preApply(IPlayer player) {
        player.spawnParticles(UParticles.UNICORN_MAGIC, 10);
    }

    @Override
    public void postApply(IPlayer player) {
        player.spawnParticles(UParticles.RAIN_DROPS, 5);
    }

    public interface ICloudEntity {
        void handlePegasusInteration(int interationType);
    }
}
