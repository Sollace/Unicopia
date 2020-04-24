package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ducks.PonyContainer;
import com.minelittlepony.unicopia.entity.Ponylike;
import com.minelittlepony.unicopia.entity.LivingEntityCapabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements PonyContainer<Ponylike> {

    private Ponylike caster;

    private MixinLivingEntity() { super(null, null); }

    @Override
    public Ponylike create() {
        return new LivingEntityCapabilities((LivingEntity)(Object)this);
    }

    @Override
    public Ponylike get() {
        if (caster == null) {
            caster = create();
        }
        return caster;
    }

    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    private void onCanSee(Entity other, CallbackInfoReturnable<Boolean> info) {
        if (get().isInvisible()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "tickActiveItemStack()V", at = @At("HEAD"))
    private void onFinishUsing(CallbackInfo info) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!self.getActiveItem().isEmpty() && self.isUsingItem()) {
            get().onUse(self.getActiveItem());
        }
    }

    @Inject(method = "jump()V", at = @At("RETURN"))
    private void onJump(CallbackInfo info) {
        get().onJump();
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        if (get().beforeUpdate()) {
            info.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        get().onUpdate();
    }

    @Inject(method = "<clinit>()V", at = @At("RETURN"), remap = false)
    private static void clinit(CallbackInfo info) {
        LivingEntityCapabilities.boostrap();
    }

    // ---------- temporary
    @SuppressWarnings("deprecation")
    @Inject(method = "isClimbing()Z", at = @At("HEAD"), cancellable = true)
    public void onIsClimbing(CallbackInfoReturnable<Boolean> info) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!self.isSpectator() && self.getBlockState().getBlock() instanceof com.minelittlepony.unicopia.ducks.Climbable) {
            info.setReturnValue(true);
        }
     }
}
