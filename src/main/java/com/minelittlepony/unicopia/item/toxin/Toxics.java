package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.registry.Registry;

import static com.minelittlepony.unicopia.item.toxin.Toxicity.*;
import static com.minelittlepony.unicopia.item.toxin.Ailment.*;
import static com.minelittlepony.unicopia.item.toxin.Toxin.*;

import org.jetbrains.annotations.Nullable;

@Deprecated
public interface Toxics {
    Registry<ToxicRegistryEntry> REGISTRY = RegistryUtils.createSimple(Unicopia.id("toxic"));
    Toxic FORAGE_EDIBLE = register("forage_edible", new Toxic.Builder(Ailment.INNERT).food(UFoodComponents.RANDOM_FOLIAGE).with(Race.HUMAN, of(LETHAL, FOOD_POISONING)).with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS)).with(Race.SEAPONY, of(FAIR, FOOD_POISONING)));

    static void bootstrap() {
        register("forage_edible_filling", new Toxic.Builder(Ailment.INNERT).food(UFoodComponents.RANDOM_FOLIAGE_FILLING).with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS)).with(Race.SEAPONY, of(FAIR, FOOD_POISONING)));
        register("forage_risky", new Toxic.Builder(of(FAIR, FOOD_POISONING.withChance(20))).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_moderate", new Toxic.Builder(of(MILD, FOOD_POISONING)).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_dangerous", new Toxic.Builder(of(SEVERE, FOOD_POISONING)).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_nauseating", new Toxic.Builder(of(SAFE, FOOD_POISONING.and(WEAKNESS.withChance(30)))).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_radioactive", new Toxic.Builder(of(SAFE, FOOD_POISONING.and(GLOWING.withChance(30)))).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_prickly", new Toxic.Builder(of(SAFE, INSTANT_DAMAGE.withChance(30))).food(UFoodComponents.RANDOM_FOLIAGE).with(Ailment.INNERT, Race.HIPPOGRIFF, Race.KIRIN));
        register("forage_strengthening", new Toxic.Builder(of(SEVERE, STRENGTH.and(FOOD_POISONING))).food(UFoodComponents.RANDOM_FOLIAGE).with(Race.KIRIN, Ailment.INNERT));
        register("forage_severely_nauseating", new Toxic.Builder(of(SEVERE, FOOD_POISONING.and(WEAKNESS))).food(UFoodComponents.RANDOM_FOLIAGE));
        register("forage_blinding", new Toxic.Builder(of(SEVERE, BLINDNESS.and(FOOD_POISONING))).food(UFoodComponents.RANDOM_FOLIAGE).with(Race.KIRIN, Ailment.INNERT));
        register("forage_severely_prickly", new Toxic.Builder(of(SEVERE, FOOD_POISONING.and(INSTANT_DAMAGE))).food(UFoodComponents.RANDOM_FOLIAGE).with(Race.KIRIN, Ailment.INNERT));
        register("raw_meat", new Toxic.Builder(of(SEVERE, FOOD_POISONING.withChance(5).and(CHANCE_OF_POISON))).with(Ailment.INNERT, Race.HUMAN, Race.CHANGELING, Race.KIRIN).with(of(MILD, FOOD_POISONING), Race.BAT));
        register("rotten_meat", new Toxic.Builder(of(SEVERE, STRONG_FOOD_POISONING)).with(Ailment.INNERT, Race.HUMAN, Race.CHANGELING).with(of(MILD, FOOD_POISONING), Race.BAT));
        register("cooked_meat", new Toxic.Builder(of(FAIR, FOOD_POISONING)).with(Ailment.INNERT, Race.HUMAN, Race.CHANGELING, Race.BAT, Race.KIRIN).with(of(MILD, FOOD_POISONING), Race.HIPPOGRIFF));
        register("raw_fish", new Toxic.Builder(of(FAIR, FOOD_POISONING.and(CHANCE_OF_POISON))).with(Ailment.INNERT, Race.HUMAN, Race.HIPPOGRIFF, Race.SEAPONY, Race.ALICORN).with(of(MILD, FOOD_POISONING), Race.PEGASUS).with(of(FAIR, LOVE_SICKNESS), Race.CHANGELING));
        register("cooked_fish", new Toxic.Builder(of(MILD, FOOD_POISONING)).with(Ailment.INNERT, Race.HUMAN, Race.PEGASUS, Race.HIPPOGRIFF, Race.SEAPONY, Race.ALICORN).with(of(FAIR, LOVE_SICKNESS), Race.CHANGELING));
        register("raw_insect", new Toxic.Builder(of(LETHAL, FOOD_POISONING)).food(UFoodComponents.INSECTS).with(Ailment.INNERT, Race.CHANGELING).with(of(MILD, WEAK_FOOD_POISONING), Race.BAT));
        register("cooked_insect", new Toxic.Builder(of(LETHAL, FOOD_POISONING)).food(UFoodComponents.INSECTS).with(Ailment.INNERT, Race.CHANGELING, Race.KIRIN, Race.BAT));
        register("love", new Toxic.Builder(Ailment.INNERT).with(of(Toxicity.SAFE, Toxin.LOVE_CONSUMPTION), Race.CHANGELING));
        register("bat_ponys_delight", new Toxic.Builder(Ailment.INNERT).with(of(Toxicity.SAFE, Toxin.BAT_PONY_INTOXICATION), Race.BAT));
        register("raw_sea_vegitable", new Toxic.Builder(Ailment.INNERT).food(Race.SEAPONY, UFoodComponents.RANDOM_FOLIAGE));
        register("cooked_sea_vegitable", new Toxic.Builder(Ailment.INNERT).food(Race.SEAPONY, UFoodComponents.RANDOM_FOLIAGE_FILLING));
        register("shells", new Toxic.Builder(Ailment.INNERT).food(Race.SEAPONY, UFoodComponents.SHELL));
        register("shelly", new Toxic.Builder(Ailment.INNERT).food(Race.SEAPONY, UFoodComponents.SHELLY));
        register("pinecone", new Toxic.Builder(of(Toxicity.SAFE, Toxin.healing(1))).with(Ailment.INNERT, Race.HUMAN).with(of(Toxicity.SAFE, Toxin.healing(3)), Race.HIPPOGRIFF));
    }

    static Toxic register(String name, Toxic.Builder builder) {
        return Registry.register(REGISTRY, Unicopia.id(name), new ToxicRegistryEntry(builder.build(), UTags.item("food_types/" + name))).value();
    }

    static Toxic lookup(ItemDuck item, @Nullable LivingEntity entity) {
        @Nullable FoodComponent food = item.asItem().getFoodComponent();
        return REGISTRY.stream()
                .filter(i -> i.matches(item.asItem()))
                .map(ToxicRegistryEntry::value)
                .map(t -> {
            if (food == null) {
                t.food().apply(entity).ifPresent(item::setFoodComponent);
            }
            return t;
        }).findFirst().orElse(food == null ? Toxic.EMPTY : Toxic.DEFAULT);
    }
}
