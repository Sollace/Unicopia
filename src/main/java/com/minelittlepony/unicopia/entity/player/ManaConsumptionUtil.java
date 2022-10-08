package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

public interface ManaConsumptionUtil {
    float FOOD_PER_MANA = 0.1F;
    float HEARTS_PER_FOOD = 0.5F;
    float SATURATION_PER_FOOD = 0.8F;

    static float consumeMana(MagicReserves.Bar mana, double foodSubtract) {

        if (foodSubtract <= 0) {
            return 0;
        }

        float availableMana = mana.get();
        float consumedMana = (float)foodSubtract / FOOD_PER_MANA;

        if (consumedMana <= availableMana) {
            mana.set(availableMana - consumedMana);
            return 0;
        }

        mana.set(0);
        return (float)foodSubtract - (availableMana * FOOD_PER_MANA);
    }

    static float burnFood(PlayerEntity entity, float foodSubtract) {
        HungerManager hunger = entity.getHungerManager();

        if (foodSubtract <= 0) {
            return 0;
        }

        float availableSaturation = hunger.getSaturationLevel();

        if (availableSaturation > 0 && foodSubtract > 0) {
            float consumedSaturation = Math.min(foodSubtract * SATURATION_PER_FOOD, availableSaturation);
            foodSubtract = addExhaustion(hunger, foodSubtract);
            if (consumedSaturation > 0) {
                foodSubtract -= (consumedSaturation / SATURATION_PER_FOOD);
                hunger.setSaturationLevel(availableSaturation - consumedSaturation);
            }
        }

        int availableFood = hunger.getFoodLevel();
        if (availableFood > 0 && foodSubtract > 0) {
            int consumedFood = Math.min((int)Math.floor(foodSubtract), availableFood);
            foodSubtract = addExhaustion(hunger, foodSubtract);
            if (consumedFood > 0) {
                foodSubtract -= consumedFood;
                hunger.add(-consumedFood, 0.3F);
            }
        }

        float availableHearts = entity.getHealth();
        if (foodSubtract > 0) {
            float consumedHearts = Math.max(0.001F, Math.min(availableHearts - 1, foodSubtract * HEARTS_PER_FOOD));
            foodSubtract = addExhaustion(hunger, foodSubtract);
            foodSubtract -= (consumedHearts / HEARTS_PER_FOOD);
            entity.damage(MagicalDamageSource.EXHAUSTION, consumedHearts);
        }

        return Math.max(0, foodSubtract);
    }

    static float addExhaustion(HungerManager hunger, float foodSubtract) {
        hunger.addExhaustion(0.1F);
        return Math.max(0, foodSubtract - 0.1F);
    }

    interface FloatSupplier {
        float get();
    }

    interface FloatConsumer {
        void accept(float f);
    }
}
