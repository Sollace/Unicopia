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
import net.minecraft.nbt.CompoundTag;

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

    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void onCanSee(Entity other, CallbackInfoReturnable<Boolean> info) {
        if (get().isInvisible()) {
            info.setReturnValue(false);
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

    @Inject(method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        tag.put("unicopia_caster", get().toNBT());
    }

    @Inject(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void onReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("unicopia_caster")) {
            get().fromNBT(tag.getCompound("unicopia_caster"));
        }
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
