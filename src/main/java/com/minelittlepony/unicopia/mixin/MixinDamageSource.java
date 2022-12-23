package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

@Mixin(DamageSource.class)
abstract class MixinDamageSource {
    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void onGetDeathMessage(LivingEntity entity, CallbackInfoReturnable<Text> info) {
        Equine.of(entity).map(Equine::getAttacker).ifPresent(attacker -> {
            DamageSource self = (DamageSource)(Object)this;

            Entity prime = entity.getPrimeAdversary();
            if (prime != null && !attacker.isOwnedBy(prime)) {
                info.setReturnValue(Text.translatable("death.attack.generic.and_also", info.getReturnValue(), attacker.asEntity().getDisplayName()));
                return;
            }

            info.setReturnValue(Text.translatable("death.attack." + self.getName() + ".player", entity.getDisplayName(), attacker.asEntity().getDisplayName()));
        });

        DamageSource self = (DamageSource)(Object)this;

        Pony.of(entity).filter(e -> e.getSpecies().canFly()).ifPresent(pony -> {
            if (pony.getPhysics().isFlying()) {
                info.setReturnValue(Text.translatable("death.attack.generic.whilst_flying", info.getReturnValue()));
            }
        });
    }
}
