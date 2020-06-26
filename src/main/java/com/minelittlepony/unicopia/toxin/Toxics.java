package com.minelittlepony.unicopia.toxin;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;

public interface Toxics {

    Toxic GRASS = register(Items.GRASS, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.NAUSEA));
    Toxic FERN = register(Items.FERN, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.STRENGTH));
    Toxic DEAD_BUSH = register(Items.DEAD_BUSH, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.NAUSEA));

    Toxic DANDELION = register(Items.DANDELION, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic POPPY = register(Items.POPPY, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD);
    Toxic BLUE_ORCHID = register(Items.BLUE_ORCHID, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic ALLIUM = register(Items.ALLIUM, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.FAIR, Toxin.FOOD);
    Toxic AZUER_BLUET = register(Items.AZURE_BLUET, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.RADIOACTIVITY));
    Toxic RED_TULIP = register(Items.RED_TULIP, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic ORANGE_TULIP = register(Items.ORANGE_TULIP, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic WHITE_TULIP = register(Items.WHITE_TULIP, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.FAIR, Toxin.FOOD);
    Toxic PINK_TULIP = register(Items.PINK_TULIP, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic OXEYE_DAISY = register(Items.OXEYE_DAISY, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.BLINDNESS));
    Toxic CORNFLOWER = register(Items.CORNFLOWER, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);

    Toxic ROSE_BUSH = register(Items.ROSE_BUSH, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD.and(Toxin.DAMAGE));
    Toxic PEONY = register(Items.PEONY, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic TALL_GRASS = register(Items.TALL_GRASS, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SAFE, Toxin.FOOD);
    Toxic LARGE_FERN = register(Items.LARGE_FERN, UFoodComponents.RANDOM_FOLIAGE, UseAction.EAT, Toxicity.SEVERE, Toxin.FOOD.and(Toxin.DAMAGE));

    static void bootstrap() {}

    static Toxic register(Item target, FoodComponent food, UseAction action, Toxicity toxicity, Toxin toxin) {
        Toxic toxic = new Toxic(target, action, toxin, toxicity);
        ToxicHolder holder = (ToxicHolder)target;
        holder.setFood(food);
        holder.setToxic(toxic);
        return toxic;
    }
}
