package com.minelittlepony.unicopia.toxin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface Toxin {
    Predicate ONE_EVERY_30_TICKS = (player, toxicity, stack) -> player.world.random.nextInt(30) == 0;

    Toxin DAMAGE = ONE_EVERY_30_TICKS.then(of(StatusEffects.INSTANT_DAMAGE, 1, 1));
    Toxin RADIOACTIVITY = ONE_EVERY_30_TICKS.then(of(StatusEffects.GLOWING, 10, 1));
    Toxin NAUSEA = of(StatusEffects.NAUSEA, 30, 1);
    Toxin WEAK_NAUSEA = of(StatusEffects.NAUSEA, 3, 1);
    Toxin STRENGTH = of(StatusEffects.STRENGTH, 30, 1);
    Toxin BLINDNESS = of(StatusEffects.BLINDNESS, 30, 1);
    Toxin FOOD = (player, toxicity, stack) -> toxicity.afflict(player, toxicity, stack);

    void afflict(PlayerEntity player, Toxicity toxicity, ItemStack stack);

    default Toxin and(Toxin other) {
        Toxin self = this;
        return (player, toxicity, stack) -> {
            self.afflict(player, toxicity, stack);
            other.afflict(player, toxicity, stack);
        };
    }

    static Toxin of(StatusEffect effect, int duration, int amplifier) {
        return (player, toxicity, stack) -> {
            player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false));
        };
    }

    interface Predicate {
        boolean test(PlayerEntity player, Toxicity toxicity, ItemStack stack);
        default Toxin then(Toxin toxin) {
            return (player, toxicity, stack) -> {
                if (test(player, toxicity, stack)) {
                    toxin.afflict(player, toxicity, stack);
                }
            };
        }
    }
}
