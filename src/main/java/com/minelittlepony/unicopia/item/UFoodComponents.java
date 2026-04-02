package com.minelittlepony.unicopia.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.FoodComponents;

public interface UFoodComponents {
    FoodComponent OATS = builder(1, 0.7F).build();
    FoodComponent IMPORTED_OATS = builder(3, 1.3F).build();
    FoodComponent OATMEAL = builder(0, 1.3F).build();
    FoodComponent DAFODIL_DAISY_SANDWICH = builder(3, 1.4F).build();
    FoodComponent BURGER = builder(7, 1.4F).build();
    FoodComponent HAY_FRIES = builder(4, 2).build();
    FoodComponent CRISPY_HAY_FRIES = builder(6, 7).build();

    FoodComponent PIE = builder(3, 1.26F).build();
    FoodComponent CIDER = builder(2, 1.7F).alwaysEdible().build();

    FoodComponent JUICE = builder(2, 1.8F).alwaysEdible().build();
    FoodComponent BURNED_JUICE = builder(3, 1).build();

    FoodComponent NUT_BOWL = builder(5, 0.6F).build();//FoodComponents.BAKED_POTATO;

    FoodComponent OATMEAL_COOKIE = FoodComponents.COOKIE; //builder(2, 0.1F).build();
    FoodComponent CHOCOLATE_OATMEAL_COOKIE = builder(3, 0.4F).build();
    FoodComponent SCONE = builder(2, 0.2F).build();
    FoodComponent FRIED_EGG = builder(4, 0.4F).build();

    FoodComponent ROTTEN_PUFFERFISH = new FoodComponent.Builder()
            .nutrition(4)
            .saturationModifier(0.1F)
            .build();
    FoodComponent COOKED_PUFFERFISH = builder(5, 0.6F).build();

    FoodComponent WORMS = builder(1, 1.5F).alwaysEdible().build();
    FoodComponent INSECTS = builder(1, 0).alwaysEdible().build();

    FoodComponent TOAST = builder(1, 0.6F).alwaysEdible().build();
    FoodComponent BURNED_TOAST = builder(1, -0.8F).alwaysEdible().build();
    FoodComponent JAM_TOAST = builder(4, 0.6F).alwaysEdible().build();
    FoodComponent ZAP_APPLE = builder(4, 0.3F).alwaysEdible().build();
    FoodComponent ZAP_BULB = builder(-2, -0.8f).alwaysEdible().build();

    FoodComponent LOVE_BOTTLE = builder(2, 0.125F).alwaysEdible().build();
    FoodComponent LOVE_MUG = builder(4, 0.125F).build();
    FoodComponent LOVE_BUCKET = builder(8, 0.125F).build();

    FoodComponent PINECONE = builder(0, 0.01F).alwaysEdible().build();
    FoodComponent ACORN = builder(1, 0.01F).alwaysEdible().build();
    FoodComponent MANGO = builder(8, 0.8F).alwaysEdible().build();
    FoodComponent BANANA = builder(6, 0.9F).build();
    FoodComponent SEEDS = builder(1, 0.2F).build();

    FoodComponent CANDY = builder(7, 0.9F).alwaysEdible().build();
    FoodComponent SALT_CUBE = builder(0, 2.9F).alwaysEdible().build();

    FoodComponent POISON_JOKE = builder(0, 0F).alwaysEdible().build();

    FoodComponent SHELL = builder(3, 1.5F).build();
    FoodComponent SHELLY = builder(6, 0.7F).build();
    FoodComponent ROCK = builder(3, 0.5F).build();
    FoodComponent ROCK_STEW = builder(6, 0.6F).usingConvertsTo(Items.BOWL).build();

    static FoodComponent.Builder builder(int hunger, float saturation) {
        return new FoodComponent.Builder()
                .nutrition(hunger)
                .saturationModifier(saturation);
    }
}
