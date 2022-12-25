package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.*;
import net.minecraft.text.Text;

@Mixin(DamageSource.class)
abstract class MixinDamageSource {
    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void onGetDeathMessage(LivingEntity entity, CallbackInfoReturnable<Text> info) {
        final DamageSource self = (DamageSource)(Object)this;

        if (self.isFromFalling()) {
            info.setReturnValue(new DamageSource(self.name + ".pegasus").getDeathMessage(entity));
        } else {
            Living.getOrEmpty(entity).map(Living::getAttacker).ifPresent(attacker -> {
                Entity prime = entity.getPrimeAdversary();
                if (prime != null && !attacker.isOwnedBy(prime)) {
                    info.setReturnValue(Text.translatable("death.attack.generic.and_also", info.getReturnValue(), attacker.asEntity().getDisplayName()));
                    return;
                }

                info.setReturnValue(Text.translatable("death.attack." + self.getName() + ".player", entity.getDisplayName(), attacker.asEntity().getDisplayName()));
            });


            Pony.of(entity).filter(e -> e.getSpecies().canFly()).ifPresent(pony -> {
                if (pony.getPhysics().isFlying()) {
                    info.setReturnValue(Text.translatable("death.attack.generic.whilst_flying", info.getReturnValue()));
                }
            });
        }
    }
}

@Mixin(DamageTracker.class)
abstract class MixinDamageTracker {
    @Shadow
    private @Final LivingEntity entity;
    @Nullable
    private String fallDeathSuffix;

    @Inject(method = "setFallDeathSuffix", at = @At("RETURN"))
    private void onSetFallDeathSuffix(CallbackInfo info) {
        Pony.of(entity).ifPresent(pony -> {
            if (pony.getSpecies().canFly()) {
                fallDeathSuffix = (fallDeathSuffix == null ? "" : fallDeathSuffix + ".") + "pegasus";
            }
        });
    }
}
