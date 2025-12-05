package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.*;
import net.minecraft.text.Text;

@Mixin(DamageSource.class)
abstract class MixinDamageSource {
    @ModifyReturnValue(method = "getDeathMessage", at = @At("RETURN"))
    private Text onGetDeathMessage(Text message, LivingEntity entity) {
        final DamageSource self = (DamageSource)(Object)this;

        return Pony.of(entity).filter(e -> e.getCompositeRace().canFly()).map(pony -> {
            if (pony.getPhysics().isFlying()) {
                return Text.translatable("death.attack.unicopia.generic.whilst_flying", message);
            }
            return (Text)null;
        }).or(() -> Living.getOrEmpty(entity).map(Living::getAttacker).map(attacker -> {
            Entity prime = entity.getPrimeAdversary();
            if (prime != null && !attacker.isOwnedBy(prime)) {
                return Text.translatable("death.attack.unicopia.generic.and_also", message, attacker.asEntity().getDisplayName());
            }

            String name = self.getName();
            if (!name.endsWith(".player")) {
                return Text.translatable("death.attack." + name + ".player", entity.getDisplayName(), attacker.asEntity().getDisplayName());
            }

            return (Text)null;
        })).orElse(message);
    }
}

@Mixin(FallLocation.class)
abstract class MixinFallLocation {
    @ModifyReturnValue(method = "fromEntity", at = @At("RETURN"))
    private static FallLocation onFromEntity(FallLocation location, LivingEntity entity) {
        return location == null ? null : Pony.of(entity).map(pony -> {
            if (pony.getCompositeRace().canFly()) {
                return new FallLocation(location.id() + ".pegasus");
            }
            return null;
        }).orElse(location);
    }
}
