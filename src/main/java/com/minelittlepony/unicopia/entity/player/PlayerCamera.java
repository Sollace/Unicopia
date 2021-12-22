package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import com.minelittlepony.common.util.animation.MotionCompositor;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;

import net.minecraft.util.math.Vec3d;

public class PlayerCamera extends MotionCompositor {

    private final Pony player;

    public PlayerCamera(Pony player) {
        this.player = player;
    }

    public float calculateRoll() {

        double roll = 0;

        if (player.getMotion().isFlying()) {
            Vec3d vel = player.getMaster().getVelocity();

            roll -= calculateRoll(player.getMaster(), vel.x, vel.y, vel.z);
        }

        if (player.getPhysics().isGravityNegative()) {
           roll *= -1;
           roll += 180;
        }

        if (player.getEntity().age > 10) {
            roll = player.getInterpolator().interpolate("roll", (float)roll, 15);
        }

        return (float)roll;
    }

    public float calculatePitch(float pitch) {
        return pitch + getEnergyAddition();
    }

    public float calculateYaw(float yaw) {
        return yaw + getEnergyAddition();
    }

    public Optional<Double> calculateDistance(double distance) {
        return player.getSpellSlot()
            .get(SpellPredicate.IS_DISGUISE, false)
            .map(AbstractDisguiseSpell::getDisguise)
            .flatMap(d -> d.getDistance(player))
            .map(d -> distance * d);
    }

    public double calculateFieldOfView(double fov) {
        fov += player.getMagicalReserves().getExertion().get() / 5F;
        fov += getEnergyAddition();

        return fov;
    }

    protected float getEnergyAddition() {
        int maxE = (int)Math.floor(player.getMagicalReserves().getEnergy().get() * 100);

        if (maxE <= 0) {
            return 0;
        }

        float energyAddition = (player.getWorld().random.nextInt(maxE) - maxE/2) / 100F;

        if (Math.abs(energyAddition) <= 0.001) {
            return 0;
        }

        return energyAddition;
    }
}
