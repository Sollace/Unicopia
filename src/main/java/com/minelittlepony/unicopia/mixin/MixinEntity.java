package com.minelittlepony.unicopia.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.PositionUpdater;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck {
    @Override
    @Accessor("submergedFluidTag")
    public abstract Set<TagKey<Fluid>> getSubmergedFluidTags();

    @Override
    @Accessor
    public abstract void setRemovalReason(RemovalReason reason);

    @Override
    @Accessor
    public abstract void setVehicle(Entity vehicle);

    @Override
    @Accessor
    public abstract float getNextStepSoundDistance();

    @Override
    public boolean isLavaAffine() {
        Entity self = (Entity)(Object)this;
        return self.hasVehicle() && self.getVehicle() instanceof LavaAffine affine && affine.isLavaAffine();
    }

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void onIsFireImmune(CallbackInfoReturnable<Boolean> info) {
        if (isLavaAffine() || (this instanceof Equine.Container c) && c.get().getCompositeRace().includes(Race.KIRIN)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isSneaky", at = @At("HEAD"), cancellable = true)
    private void onIsSneaky(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this) && !EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    private void onGetMaxAir(CallbackInfoReturnable<Integer> info) {
        if (EquinePredicates.PLAYER_KIRIN.test((Entity)(Object)this)) {
            info.setReturnValue(150);
        }
    }

    @Inject(method = "doesRenderOnFire", at = @At("HEAD"), cancellable = true)
    private void onDoesRenderOnFire(CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.RAGING.test((Entity)(Object)this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater, CallbackInfo info) {
        if (Living.getOrEmpty((Entity)(Object)this).filter(l -> l.onUpdatePassengerPosition(passenger, positionUpdater)).isPresent()) {
            info.cancel();
        }
    }
}
