package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface AttractionUtils {

    static double getAttractiveForce(double wellMass, Vec3d wellPosition, Entity target) {
        return (wellMass * AttractionUtils.getMass(target)) / MathHelper.square(wellPosition.distanceTo(target.getPos()));
    }

    static double getMass(Entity entity) {
        return entity.getWidth() * entity.getHeight();
    }

    /**
     * Applies a force to the given entity relative to a given center position
     */
    static void applyForce(Vec3d center, Entity target, double force, double yChange, boolean clampVelocity) {
        if (clampVelocity) {
            target.setVelocity(target.getVelocity().multiply(Math.min(1, 1 - force)));
        }
        center = target.getPos().subtract(center).normalize().multiply(force);

        if (target instanceof LivingEntity) {
            center = center.multiply(1 / (1 + EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, (LivingEntity)target)));
        }

        target.addVelocity(
                center.x,
                center.y + yChange,
                center.z
        );
        Living.updateVelocity(target);
    }

    /**
     * Returns a force to apply based on the given player's given race.
     */
    static double getForceAdjustment(Entity entity) {
        return Pony.of(entity).map(pony -> {
            double force = 0.75;

            if (pony.getSpecies().canUseEarth()) {
                force /= 2;

                if (pony.getMaster().isSneaking()) {
                    force /= 6;
                }
            } else if (pony.getSpecies().canFly()) {
                force *= 2;
            }

            return force;
        }).orElse(1D);
    }
}
