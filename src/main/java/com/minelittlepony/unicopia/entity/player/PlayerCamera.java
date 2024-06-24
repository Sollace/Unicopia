package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import com.minelittlepony.common.util.animation.MotionCompositor;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.client.render.spell.DarkVortexSpellRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class PlayerCamera extends MotionCompositor {

    private final Pony player;

    public PlayerCamera(Pony player) {
        this.player = player;
    }

    public float calculateRoll() {
        return player.getInterpolator().interpolate("roll", (float)applyModifiers(-getMotionRoll()), 15);
    }

    public float calculateFirstPersonRoll() {
        return player.getInterpolator().interpolate("roll_fp", (float)applyModifiers(-getMotionRoll() * getFovScale() * 0.25F), 25);
    }

    private double getMotionRoll() {
        if (!player.getMotion().isFlying() || player.asEntity().hasVehicle() || player.asEntity().isOnGround()) {
            return 0;
        }

        Vec3d vel = player.asEntity().getVelocity();
        return calculateRoll(player.asEntity(), vel.x, vel.y, vel.z);
    }

    private double applyModifiers(double motionRoll) {
        if (player.getAcrobatics().isFloppy()) {
            motionRoll += 90;
        }

        return player.getPhysics().isGravityNegative() ? 180 - motionRoll : motionRoll;
    }

    public float calculatePitch(float pitch) {
        return pitch + getEnergyAddition();
    }

    public float calculateYaw(float yaw) {
        return yaw + getEnergyAddition();
    }

    public Optional<Double> calculateDistance(double distance) {
        return player.getSpellSlot()
            .get(SpellPredicate.IS_DISGUISE)
            .map(AbstractDisguiseSpell::getDisguise)
            .flatMap(d -> d.getDistance(player))
            .map(d -> distance * d);
    }

    public double calculateFieldOfView(double fov) {
        fov += (player.getMagicalReserves().getExertion().get() / 5F) * getFovScale();
        fov += getEnergyAddition() * getFovScale();
        fov += DarkVortexSpellRenderer.getCameraDistortion() * 2.5F;
        return fov;
    }

    private float getFovScale() {
        return MinecraftClient.getInstance().options.getFovEffectScale().getValue().floatValue();
    }

    protected float getEnergyAddition() {
        int maxE = (int)Math.floor(player.getMagicalReserves().getEnergy().get() * 100);

        if (maxE <= 0) {
            return 0;
        }

        float energyAddition = (player.asWorld().random.nextInt(maxE) - maxE/2) / 100F;

        if (Math.abs(energyAddition) <= 0.001) {
            return 0;
        }

        return energyAddition;
    }
}
