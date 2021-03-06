package com.minelittlepony.unicopia.mixin;

import javax.annotation.Nullable;

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
    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void onGetDeathMessage(LivingEntity entity, CallbackInfoReturnable<Text> info) {
        Equine<?> eq = Equine.of(entity);
        @Nullable
        Entity attacker = eq == null ? null : eq.getAttacker();
        DamageSource self = (DamageSource)(Object)this;

        if (attacker != null) {
            Entity prime = entity.getPrimeAdversary();
            if (prime != null && !(attacker instanceof Owned<?> && ((Owned<?>)attacker).getMaster() == prime)) {
                info.setReturnValue(new TranslatableText("death.attack.generic.and_also", info.getReturnValue(), attacker.getDisplayName()));
                return;
            }

            info.setReturnValue(new TranslatableText("death.attack." + self.getName() + ".player", entity.getDisplayName(), attacker.getDisplayName()));
        }
    }
}
