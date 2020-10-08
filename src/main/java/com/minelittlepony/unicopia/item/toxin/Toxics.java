package com.minelittlepony.unicopia.item.toxin;

import java.util.Objects;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;

public interface Toxics {

    Toxic GRASS = register(Items.GRASS, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.NAUSEA);
    Toxic FERN = register(Items.FERN, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.STRENGTH);
    Toxic DEAD_BUSH = register(Items.DEAD_BUSH, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.NAUSEA);

    Toxic DANDELION = register(Items.DANDELION, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic POPPY = register(Items.POPPY, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE);
    Toxic BLUE_ORCHID = register(Items.BLUE_ORCHID, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic ALLIUM = register(Items.ALLIUM, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.FAIR);
    Toxic AZUER_BLUET = register(Items.AZURE_BLUET, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.RADIOACTIVITY);
    Toxic RED_TULIP = register(Items.RED_TULIP, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic ORANGE_TULIP = register(Items.ORANGE_TULIP, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic WHITE_TULIP = register(Items.WHITE_TULIP, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.FAIR);
    Toxic PINK_TULIP = register(Items.PINK_TULIP, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic OXEYE_DAISY = register(Items.OXEYE_DAISY, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.BLINDNESS);
    Toxic CORNFLOWER = register(Items.CORNFLOWER, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);

    Toxic ROSE_BUSH = register(Items.ROSE_BUSH, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE, Toxin.DAMAGE);
    Toxic PEONY = register(Items.PEONY, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic TALL_GRASS = register(Items.TALL_GRASS, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SAFE);
    Toxic LARGE_FERN = register(Items.LARGE_FERN, UFoodComponents.RANDOM_FOLIAGE, FoodType.VEGAN, UseAction.EAT, Toxicity.SEVERE, Toxin.DAMAGE);

    Toxic RAW_MEAT = register(new Toxic(UseAction.EAT, FoodType.RAW_MEAT, Ailment.INNERT, Ailment.of(Toxicity.MILD)), Items.PORKCHOP, Items.BEEF, Items.MUTTON, Items.RABBIT);
    Toxic COOKED_MEAT = register(new Toxic(UseAction.EAT, FoodType.COOKED_MEAT, Ailment.INNERT, Ailment.of(Toxicity.MILD)), Items.COOKED_PORKCHOP, Items.COOKED_BEEF, Items.COOKED_MUTTON, Items.COOKED_RABBIT);

    Toxic RAW_FISH = register(new Toxic(UseAction.EAT, FoodType.RAW_FISH, Ailment.INNERT, Ailment.of(Toxicity.FAIR)), Items.PUFFERFISH, Items.COD, Items.SALMON);
    Toxic COOKED_FISH = register(new Toxic(UseAction.EAT, FoodType.COOKED_FISH, Ailment.INNERT, Ailment.of(Toxicity.FAIR)), Items.COOKED_COD, Items.COOKED_SALMON);

    static void bootstrap() {}

    static Toxic register(Item target, FoodComponent food, FoodType type, UseAction action, Toxicity toxicity) {
        return register(target, food, type, action, Ailment.of(toxicity), Ailment.of(Toxicity.LETHAL));
    }

    static Toxic register(Item target, FoodComponent food, FoodType type, UseAction action, Toxicity toxicity, Toxin toxin) {
        toxin = Toxin.FOOD.and(toxin);
        return register(target, food, type, action, new Ailment(toxicity, toxin), new Ailment(Toxicity.LETHAL, toxin));
    }

    static Toxic register(Item target, FoodComponent food, FoodType type, UseAction action, Ailment lowerBound, Ailment upperbound) {
        Toxic toxic = new Toxic(action, type, lowerBound, upperbound);
        ToxicHolder holder = (ToxicHolder)target;
        holder.setToxic(toxic);
        holder.setFood(Objects.requireNonNull(food, target.getName().toString() + " food"));
        return toxic;
    }

    static Toxic register(Toxic toxic, Item... items) {
        for (Item i : items) {
            ((ToxicHolder)i).setToxic(toxic);
        }
        return toxic;
    }
}
