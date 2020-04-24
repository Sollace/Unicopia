package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.ability.HeightPredicate;
import com.minelittlepony.unicopia.magic.MagicEffect;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;

public final class PlayerDimensionsDelegate {

    private float defaultEyeHeight;
    private float defaultBodyHeight;

    private float lastTargetEyeHeight;
    private float lastTargetBodyHeight;

    private final GravityDelegate gravity;

    public PlayerDimensionsDelegate(GravityDelegate gravity) {
        this.gravity = gravity;
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

        if (gravity.getGravitationConstant() < 0 && gravity.player.getOwner().isSneaking()) {
            height += 0.2F;
        }

        if (gravity.getGravitationConstant() < 0) {
            height = targetBodyHeight - height;
        }

        return height;
    }

    private float calculateTargetEyeHeight() {
        if (gravity.player.hasEffect()) {
            MagicEffect effect = gravity.player.getEffect();
            if (!effect.isDead() && effect instanceof HeightPredicate) {
                float val = ((HeightPredicate)effect).getTargetEyeHeight(gravity.player);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (gravity.isFlying && gravity.isRainboom()) {
            return 0.5F;
        }

        return defaultEyeHeight;
    }

    private float calculateTargetBodyHeight() {
        if (gravity.player.hasEffect()) {
            MagicEffect effect = gravity.player.getEffect();
            if (!effect.isDead() && effect instanceof HeightPredicate) {
                float val = ((HeightPredicate)effect).getTargetBodyHeight(gravity.player);
                if (val > 0) {
                    return val;
                }
            }
        }

        if (gravity.isFlying && gravity.isRainboom()) {
            return defaultBodyHeight / 2;
        }

        return defaultBodyHeight;
    }

}
