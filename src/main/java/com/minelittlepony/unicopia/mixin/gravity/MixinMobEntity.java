package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.duck.RotatedView;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(MobEntity.class)
abstract class MixinMobEntity extends LivingEntity implements Equine.Container<Creature> {
    private MixinMobEntity() { super(null, null); }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    public void beforeTickAi(CallbackInfo into) {
        if (get().getPhysics().isGravityNegative()) {
            ((RotatedView)getWorld()).pushRotation((int)(getY() + getHeight() * 0.5F));
        }
    }

    @Inject(method = "tickNewAi", at = @At("RETURN"))
    public void afterTickAi(CallbackInfo into) {
        ((RotatedView)getWorld()).popRotation();
    }
}
