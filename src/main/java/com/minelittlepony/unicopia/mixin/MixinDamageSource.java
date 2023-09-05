package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

        Living.getOrEmpty(entity).map(Living::getAttacker).ifPresent(attacker -> {
            Entity prime = entity.getPrimeAdversary();
            if (prime != null && !attacker.isOwnedBy(prime)) {
                info.setReturnValue(Text.translatable("death.attack.unicopia.generic.and_also", info.getReturnValue(), attacker.asEntity().getDisplayName()));
                return;
            }

            String name = self.getName();
            if (!name.endsWith(".player")) {
                info.setReturnValue(Text.translatable("death.attack." + name + ".player", entity.getDisplayName(), attacker.asEntity().getDisplayName()));
            }
        });

        Pony.of(entity).filter(e -> e.getCompositeRace().canFly()).ifPresent(pony -> {
            if (pony.getPhysics().isFlying()) {
                info.setReturnValue(Text.translatable("death.attack.unicopia.generic.whilst_flying", info.getReturnValue()));
            }
        });
    }
}

@Mixin(FallLocation.class)
abstract class MixinFallLocation {
    @Inject(method = "fromEntity", at = @At("RETURN"), cancellable = true)
    private static void onFromEntity(LivingEntity entity, CallbackInfoReturnable<FallLocation> info) {
        FallLocation location = info.getReturnValue();
        if (location == null) {
            return;
        }
        Pony.of(entity).ifPresent(pony -> {
            if (pony.getCompositeRace().canFly()) {
                info.setReturnValue(new FallLocation(location.id() + ".pegasus"));
            }
        });
    }
}
