package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Difficulty;

@FunctionalInterface
public interface Toxin {
    Predicate ONE_EVERY_30_TICKS = (player, type, toxicity, stack) -> player.world.random.nextInt(30) == 0;

    Toxin INNERT = (player, type, toxicity, stack) -> {};
    Toxin DAMAGE = ONE_EVERY_30_TICKS.then(of(StatusEffects.INSTANT_DAMAGE, 1, 1));
    Toxin RADIOACTIVITY = ONE_EVERY_30_TICKS.then(of(StatusEffects.GLOWING, 10, 1));
    Toxin NAUSEA = of(StatusEffects.NAUSEA, 30, 1);
    Toxin WEAK_NAUSEA = of(StatusEffects.NAUSEA, 3, 1);
    Toxin STRENGTH = of(StatusEffects.STRENGTH, 30, 1);
    Toxin BLINDNESS = of(StatusEffects.BLINDNESS, 30, 1);
    Toxin POISON = (player, type, toxicity, stack) -> {
        FoodComponent food = stack.getItem().getFoodComponent();

        player.getHungerManager().add(-food.getHunger()/2, -food.getSaturationModifier()/2);
        afflict(player, StatusEffects.NAUSEA, 1700, 10);

        if (player.world.getDifficulty() != Difficulty.PEACEFUL && player.world.random.nextInt(20) == 0) {
            afflict(player, UEffects.FOOD_POISONING, 150, 2);
        }

        afflict(player, StatusEffects.WEAKNESS, 2000, 20);
    };
    Toxin FOOD = (player, type, toxicity, stack) -> {
        if (toxicity.toxicWhenRaw() && type.isRaw()) {
            player.addStatusEffect(new StatusEffectInstance(toxicity.isMild() ? StatusEffects.NAUSEA : StatusEffects.POISON, toxicity.getDuration(), toxicity.getLevel()));
        }

        if (toxicity.isLethal()) {
            player.addStatusEffect(new StatusEffectInstance(UEffects.FOOD_POISONING, 300, 7, false, false));
        } else if (toxicity.toxicWhenCooked()) {
            WEAK_NAUSEA.afflict(player, type, toxicity, stack);
        }
    };

    void afflict(PlayerEntity player, FoodType type, Toxicity toxicity, ItemStack stack);

    default Toxin and(Toxin other) {
        Toxin self = this;
        return (player, type, toxicity, stack) -> {
            self.afflict(player, type, toxicity, stack);
            other.afflict(player, type, toxicity, stack);
        };
    }

    static Toxin of(StatusEffect effect, int duration, int amplifier) {
        return (player, type, toxicity, stack) -> afflict(player, effect, duration, amplifier);
    }

    static void afflict(PlayerEntity player, StatusEffect effect, int duration, int amplifier) {
        player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false));
    }

    interface Predicate {
        boolean test(PlayerEntity player, FoodType type, Toxicity toxicity, ItemStack stack);
        default Toxin then(Toxin toxin) {
            return (player, type, toxicity, stack) -> {
                if (test(player, type, toxicity, stack)) {
                    toxin.afflict(player, type, toxicity, stack);
                }
            };
        }
    }
}
