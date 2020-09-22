package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.item.FoodComponent;

public interface UFoodComponents {

    FoodComponent BAD_TOMATO = builder(3, 14).build();
    FoodComponent TOMATO = builder(4, 13).build();
    FoodComponent GOOD_TOMATO = builder(6, 14).build();

    FoodComponent ALFALFA_SEEDS = builder(1, 4).build();
    FoodComponent ALFALFA_LEAVES = builder(1, 3).build();
    FoodComponent DAFODIL_DAISY_SANDWICH = builder(3, 2).build();
    FoodComponent HAY_BURGER = builder(3, 4).build();
    FoodComponent HAY_FRIES = builder(1, 5).build();
    FoodComponent SALAD = builder(4, 2).build();
    FoodComponent RANDOM_FOLIAGE = builder(2, 1).build();
    FoodComponent JUICE = builder(2, 2).build();
    FoodComponent BURNED_JUICE = builder(3, 1).build();
    FoodComponent WORMS = builder(1, 0).alwaysEdible().build();

    FoodComponent CEREAL = builder(9, 0.8F).build();
    FoodComponent SUGAR = builder(20, -2).build();

    FoodComponent ZAP_APPLE = builder(4, 0.3F).alwaysEdible().snack().build();

    static FoodComponent.Builder builder(int hunger, float saturation) {
        return new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation);
    }
}
