package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.entity.Equine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(DamageSource.class)
abstract class MixinDamageSource {
    @SuppressWarnings("unchecked")
    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void onGetDeathMessage(LivingEntity entity, CallbackInfoReturnable<Text> info) {
        Equine.of(entity).map(Equine::getAttacker).ifPresent(attacker -> {
            DamageSource self = (DamageSource)(Object)this;

            Entity prime = entity.getPrimeAdversary();
            if (prime != null && !(attacker instanceof Owned<?> && ((Owned<Entity>)attacker).isOwnedBy(prime))) {
                info.setReturnValue(new TranslatableText("death.attack.generic.and_also", info.getReturnValue(), attacker.getDisplayName()));
                return;
            }

            info.setReturnValue(new TranslatableText("death.attack." + self.getName() + ".player", entity.getDisplayName(), attacker.getDisplayName()));
        });
    }
}
