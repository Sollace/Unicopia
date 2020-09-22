package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.ability.HeightPredicate;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;

public final class PlayerDimensions {

    private float defaultEyeHeight;
    private float defaultBodyHeight;

    private float lastTargetEyeHeight;
    private float lastTargetBodyHeight;

    private final PlayerPhysics physics;

    private final Pony pony;

    public PlayerDimensions(Pony pony, PlayerPhysics gravity) {
        this.pony = pony;
        this.physics = gravity;
    }

    public float getActiveEyeHeight(float original) {
        defaultEyeHeight = original;
        return calculateTargetEyeHeightWithGravity(calculateTargetBodyHeight());
    }

    public EntityDimensions getDimensions(EntityPose pos, EntityDimensions dimensions) {
        defaultBodyHeight = dimensions.height;
        return EntityDimensions.changing(dimensions.width, calculateTargetBodyHeight());
    }

    boolean update() {
        float targetBodyHeight = calculateTargetBodyHeight();
        float targetEyeHeight = calculateTargetEyeHeightWithGravity(targetBodyHeight);

        if (targetEyeHeight != lastTargetEyeHeight || targetBodyHeight != lastTargetBodyHeight) {
            lastTargetBodyHeight = targetBodyHeight;
            lastTargetEyeHeight = targetEyeHeight;
            return true;
        }

        return false;
    }

    private float calculateTargetEyeHeightWithGravity(float targetBodyHeight) {
        float height = calculateTargetEyeHeight();

        if (physics.isGravityNegative() && pony.getOwner().isSneaking()) {
            height += 0.2F;
        }

        if (physics.isGravityNegative()) {
            height = targetBodyHeight - height;
        }

        return height;
    }

    private float calculateTargetEyeHeight() {
        if (pony.hasSpell()) {
            Spell effect = pony.getSpell();
            if (!effect.isDead() && effect instanceof HeightPredicate) {
                float val = ((HeightPredicate)effect).getTargetEyeHeight(pony);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (physics.isFlying && physics.isRainboom()) {
            return 0.5F;
        }

        return defaultEyeHeight;
    }

    private float calculateTargetBodyHeight() {
        if (pony.hasSpell()) {
            Spell effect = pony.getSpell();
            if (!effect.isDead() && effect instanceof HeightPredicate) {
                float val = ((HeightPredicate)effect).getTargetBodyHeight(pony);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (physics.isFlying && physics.isRainboom()) {
            return defaultBodyHeight / 2;
        }

        return defaultBodyHeight;
    }

}
