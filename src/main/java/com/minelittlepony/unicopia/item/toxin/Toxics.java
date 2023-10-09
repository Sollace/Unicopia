package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.item.FoodComponent;
import net.minecraft.registry.Registry;

import static com.minelittlepony.unicopia.item.toxin.Toxicity.*;
import static com.minelittlepony.unicopia.item.toxin.Ailment.*;
import static com.minelittlepony.unicopia.item.toxin.Toxin.*;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public interface Toxics {
    Registry<ToxicRegistryEntry> REGISTRY = RegistryUtils.createSimple(Unicopia.id("toxic"));

    Toxic EMPTY = new Toxic(Optional.empty(), Optional.empty(), Ailment.Set.EMPTY);

    Toxic SEVERE_INNERT = Toxic.innert(Toxicity.SEVERE);

    Toxic EDIBLE = register("edible", new Toxic.Builder(Ailment.INNERT)
            .with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS))
    );

    Toxic FORAGE_EDIBLE = register("forage_edible", new Toxic.Builder(Ailment.INNERT)
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
            .with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS))
    );

    Toxic FORAGE_EDIBLE_FILLING = register("forage_edible_filling", new Toxic.Builder(Ailment.INNERT)
            .food(UFoodComponents.RANDOM_FOLIAGE_FILLING)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
            .with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS))
    );

    Toxic FORAGE_RISKY = register("forage_risky", new Toxic.Builder(of(FAIR, FOOD_POISONING.withChance(20)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_MODERATE = register("forage_moderate", new Toxic.Builder(of(MILD, FOOD_POISONING))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, STRONG_FOOD_POISONING))
    );
    Toxic FORAGE_DANGEROUS = register("forage_dangerous", new Toxic.Builder(of(SEVERE, FOOD_POISONING))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_NAUSEATING = register("forage_nauseating", new Toxic.Builder(of(SAFE, FOOD_POISONING.and(WEAKNESS.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_RADIOACTIVE = register("forage_radioactive", new Toxic.Builder(of(SAFE, FOOD_POISONING.and(GLOWING.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_PRICKLY = register("forage_prickly", new Toxic.Builder(of(SAFE, INSTANT_DAMAGE.withChance(30)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
            .with(Race.KIRIN, Ailment.INNERT)
    );
    Toxic FORAGE_STRENGHTENING = register("forage_strengthening", new Toxic.Builder(of(SEVERE, STRENGTH.and(FOOD_POISONING)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
            .with(Race.KIRIN, Ailment.INNERT)
    );
    Toxic FORAGE_SEVERELY_NAUSEATING = register("forage_severely_nauseating", new Toxic.Builder(of(SEVERE, FOOD_POISONING.and(WEAKNESS)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
    );
    Toxic FORAGE_BLINDING = register("forage_blinding", new Toxic.Builder(of(SEVERE, BLINDNESS.and(FOOD_POISONING)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
            .with(Race.KIRIN, Ailment.INNERT)
    );
    Toxic FORAGE_SEVERELY_PRICKLY = register("forage_severely_prickly", new Toxic.Builder(of(SEVERE, FOOD_POISONING.and(INSTANT_DAMAGE)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
            .with(Race.KIRIN, Ailment.INNERT)
    );
    Toxic RAW_MEAT = register("raw_meat", new Toxic.Builder(of(SEVERE, FOOD_POISONING.withChance(5).and(CHANCE_OF_POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.BAT, of(MILD, FOOD_POISONING))
            .with(Race.KIRIN, Ailment.INNERT)
    );
    Toxic ROTTEN_MEAT = register("rotten_meat", new Toxic.Builder(of(SEVERE, STRONG_FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.BAT, of(MILD, FOOD_POISONING))
            .with(Race.CHANGELING, Ailment.INNERT)
    );
    Toxic COOKED_MEAT = register("cooked_meat", new Toxic.Builder(of(FAIR, FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.BAT, Ailment.INNERT)
            .with(Race.KIRIN, Ailment.INNERT)
    );

    Toxic RAW_FISH = register("raw_fish", new Toxic.Builder(of(FAIR, FOOD_POISONING.and(CHANCE_OF_POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.PEGASUS, of(MILD, FOOD_POISONING))
            .with(Race.ALICORN, Ailment.INNERT)
            .with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS))
    );
    Toxic COOKED_FISH = register("cooked_fish", new Toxic.Builder(of(MILD, FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.PEGASUS, Ailment.INNERT)
            .with(Race.ALICORN, Ailment.INNERT)
            .with(Race.CHANGELING, of(FAIR, LOVE_SICKNESS))
    );

    Toxic RAW_INSECT = register("raw_insect", new Toxic.Builder(of(LETHAL, FOOD_POISONING))
            .with(Race.BAT, of(MILD, WEAK_FOOD_POISONING))
            .with(Race.CHANGELING, Ailment.INNERT)
    );

    Toxic COOKED_INSECT = register("cooked_insect", new Toxic.Builder(of(LETHAL, FOOD_POISONING))
            .food(UFoodComponents.INSECTS)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.KIRIN, Ailment.INNERT)
            .with(Race.BAT, Ailment.INNERT)
    );

    Toxic LOVE = register("love", new Toxic.Builder(Ailment.INNERT)
            .with(Race.CHANGELING, of(Toxicity.SAFE, Toxin.LOVE_CONSUMPTION))
    );

    Toxic PINECONE = register("pinecone", new Toxic.Builder(of(Toxicity.SAFE, Toxin.healing(1)))
            .with(Race.HUMAN, Ailment.INNERT)
    );

    Toxic BAT_PONYS_DELIGHT = register("bat_ponys_delight", new Toxic.Builder(Ailment.INNERT)
            .with(Race.BAT, of(Toxicity.SAFE, Toxin.BAT_PONY_INTOXICATION))
    );

    static void bootstrap() {}

    static Toxic register(String name, Toxic.Builder builder) {
        return Registry.register(REGISTRY, Unicopia.id(name), new ToxicRegistryEntry(builder.build(), UTags.item("food_types/" + name))).value();
    }

    static Toxic lookup(ItemDuck item) {
        @Nullable FoodComponent food = item.asItem().getFoodComponent();
        return REGISTRY.stream()
                .filter(i -> i.matches(item.asItem()))
                .map(ToxicRegistryEntry::value)
                .map(t -> {
            if (food == null) {
                t.component().ifPresent(item::setFoodComponent);
            }
            return t;
        }).findFirst().orElse(food == null ? Toxics.EMPTY : Toxics.EDIBLE);
    }
}
