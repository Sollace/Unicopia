package com.minelittlepony.unicopia.item.toxin;

import java.util.Objects;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;

public interface Toxics {
    Toxic FORAGED = register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Items.DANDELION, Items.BLUE_ORCHID, Items.RED_TULIP, Items.ORANGE_TULIP, Items.PINK_TULIP, Items.CORNFLOWER, Items.PEONY, Items.TALL_GRASS);
    Toxic RISKY_FORAGED = register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.FAIR, Items.ALLIUM, Items.WHITE_TULIP);
    Toxic DANGER_FORAGED = register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Items.POPPY);

    Toxic GRASS =        register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.NAUSEA, Items.GRASS);
    Toxic AZUER_BLUET =  register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.RADIOACTIVITY, Items.AZURE_BLUET);
    Toxic ROSE_BUSH =    register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.DAMAGE, Items.ROSE_BUSH);
    Toxic FERN =         register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.STRENGTH, Items.FERN);
    Toxic DEAD_BUSH =    register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.NAUSEA, Items.DEAD_BUSH);
    Toxic OXEYE_DAISY =  register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.BLINDNESS, Items.OXEYE_DAISY);
    Toxic LARGE_FERN =   register(UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.DAMAGE, Items.LARGE_FERN);

    Toxic RAW_MEAT = register(new Toxic(UseAction.EAT, FoodType.RAW_MEAT, Ailment.INNERT, Ailment.of(Toxicity.MILD)), Items.PORKCHOP, Items.BEEF, Items.MUTTON, Items.RABBIT, Items.CHICKEN);
    Toxic COOKED_MEAT = register(new Toxic(UseAction.EAT, FoodType.COOKED_MEAT, Ailment.INNERT, Ailment.of(Toxicity.MILD)), Items.COOKED_PORKCHOP, Items.COOKED_BEEF, Items.COOKED_MUTTON, Items.COOKED_RABBIT, Items.COOKED_CHICKEN);

    Toxic RAW_FISH = register(new Toxic(UseAction.EAT, FoodType.RAW_FISH, Ailment.INNERT, Ailment.of(Toxicity.FAIR)), Items.PUFFERFISH, Items.COD, Items.SALMON, Items.TROPICAL_FISH);
    Toxic COOKED_FISH = register(new Toxic(UseAction.EAT, FoodType.COOKED_FISH, Ailment.INNERT, Ailment.of(Toxicity.FAIR)), Items.COOKED_COD, Items.COOKED_SALMON);

    static void bootstrap() {}

    static Toxic register(FoodComponent food, FoodType type, UseAction action, Toxicity toxicity, Item... items) {
        Toxic toxic = new Toxic(action, type, Ailment.of(toxicity), Ailment.of(Toxicity.LETHAL));
        for (Item i : items) {
            ToxicHolder holder = (ToxicHolder)i;
            holder.setToxic(toxic);
            holder.setFood(Objects.requireNonNull(food, i.getTranslationKey() + " food"));
        }

        return toxic;

    }

    static Toxic register(FoodComponent food, FoodType type, UseAction action, Toxicity toxicity, Toxin toxin, Item target) {
        toxin = Toxin.FOOD.and(toxin);
        return register(target, food, type, action, new Ailment(toxicity, toxin), new Ailment(Toxicity.LETHAL, toxin));
    }

    static Toxic register(Item target, FoodComponent food, FoodType type, UseAction action, Ailment lowerBound, Ailment upperbound) {
        Toxic toxic = new Toxic(action, type, lowerBound, upperbound);
        ToxicHolder holder = (ToxicHolder)target;
        holder.setToxic(toxic);
        holder.setFood(Objects.requireNonNull(food, target.getTranslationKey() + " food"));
        return toxic;
    }

    static Toxic register(Toxic toxic, Item... items) {
        for (Item i : items) {
            ((ToxicHolder)i).setToxic(toxic);
        }
        return toxic;
    }
}
