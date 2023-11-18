package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public interface UFoodComponents {
    FoodComponent OATS = builder(1, 0.7F).build();
    FoodComponent IMPORTED_OATS = builder(3, 1.3F).build();
    FoodComponent OATMEAL = builder(0, 1.3F)
            .statusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 1), 1)
            .statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1), 0.3F)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1), 0.2F)
            .build();
    FoodComponent DAFODIL_DAISY_SANDWICH = builder(3, 2).build();
    FoodComponent HAY_BURGER = builder(7, 1.4F).build();
    FoodComponent HAY_FRIES = builder(4, 2).build();
    FoodComponent CRISPY_HAY_FRIES = builder(6, 7).build();

    FoodComponent PIE = builder(3, 1.26F).build();
    FoodComponent CIDER = builder(2, 1.7F).alwaysEdible().build();

    FoodComponent JUICE = builder(2, 1.8F).alwaysEdible().build();
    FoodComponent BURNED_JUICE = builder(3, 1).build();

    FoodComponent RANDOM_FOLIAGE = builder(2, 1).build();
    FoodComponent RANDOM_FOLIAGE_FILLING = builder(18, 1).build();
    FoodComponent WORMS = builder(1, 1.5F).alwaysEdible().meat().build();
    FoodComponent INSECTS = builder(1, 0).alwaysEdible().build();

    FoodComponent TOAST = builder(1, 0.6F).alwaysEdible().snack().build();
    FoodComponent BURNED_TOAST = builder(1, -0.8F).alwaysEdible().snack().build();
    FoodComponent JAM_TOAST = builder(4, 0.6F).alwaysEdible().snack().build();
    FoodComponent ZAP_APPLE = builder(4, 0.3F).alwaysEdible().snack().build();
    FoodComponent ZAP_BULB = builder(-2, -0.8f)
            .alwaysEdible()
            .statusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 0.6F)
            .statusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0), 0.6F)
            .statusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 100, 0), 0.6F)
            .build();

    FoodComponent LOVE_BOTTLE = builder(2, 0.125F).alwaysEdible().snack().build();
    FoodComponent LOVE_MUG = builder(4, 0.125F).snack().build();
    FoodComponent LOVE_BUCKET = builder(8, 0.125F).build();

    FoodComponent PINECONE = builder(0, 0.01F).snack().alwaysEdible().build();
    FoodComponent ACORN = builder(1, 0.01F).snack().alwaysEdible().build();
    FoodComponent MANGO = builder(8, 0.8F).alwaysEdible().build();
    FoodComponent BANANA = builder(6, 0.9F).build();

    FoodComponent CANDY = builder(7, 0.9F).alwaysEdible().build();
    FoodComponent SALT_CUBE = builder(0, 2.9F).alwaysEdible().build();

    FoodComponent SHELL = builder(3, 5).build();
    FoodComponent SHELLY = builder(6, 7).build();

    static FoodComponent.Builder builder(int hunger, float saturation) {
        return new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation);
    }
}
