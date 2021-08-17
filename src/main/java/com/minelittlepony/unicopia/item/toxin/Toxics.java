package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.minelittlepony.unicopia.item.toxin.Toxicity.*;
import static com.minelittlepony.unicopia.item.toxin.Toxin.*;

public interface Toxics {
    Registry<Toxic> REGISTRY = Registries.createSimple(new Identifier("unicopia:toxic"));

    Toxic EDIBLE = register("forage_edible", new Toxic.Builder(Ailment.of(SAFE, INNERT))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING))
    );
    Toxic RISKY = register("forage_risky", new Toxic.Builder(Ailment.of(FAIR, WEAK_NAUSEA.withChance(20)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING))
    );
    Toxic MODERATE = register("forage_moderate", new Toxic.Builder(Ailment.of(MILD, POISON.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING))
    );
    Toxic DANGEROUS = register("forage_dangerous", new Toxic.Builder(Ailment.of(SEVERE, FOOD_POISONING))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic NAUSEATING = register("forage_nauseating", new Toxic.Builder(Ailment.of(SAFE, NAUSEA.and(WEAKNESS.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic RADIOACTIVE = register("forage_radioactive", new Toxic.Builder(Ailment.of(SAFE, NAUSEA.and(RADIOACTIVITY.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic PRICKLY = register("forage_prickly", new Toxic.Builder(Ailment.of(SAFE, PRICKLING.withChance(30)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic STRENGHTENING = register("forage_strengthening", new Toxic.Builder(Ailment.of(SEVERE, STRENGTH.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
    );
    Toxic SEVERELY_NAUSEATING = register("forage_severely_nauseating", new Toxic.Builder(Ailment.of(SEVERE, STRONG_NAUSEA.and(WEAKNESS)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
    );
    Toxic BLINDING = register("forage_blinding", new Toxic.Builder(Ailment.of(SEVERE, BLINDNESS.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING))
    );
    Toxic SEVERELY_PRICKLY = register("forage_severely_prickly", new Toxic.Builder(Ailment.of(SEVERE, PRICKLING.and(NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, Ailment.of(LETHAL, FOOD_POISONING))
    );
    Toxic RAW_MEAT = register("raw_meat", new Toxic.Builder(Ailment.of(SEVERE, FOOD_POISONING.withChance(5).and(POISON.withChance(20))))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.BAT, Ailment.of(MILD, WEAK_NAUSEA))
    );
    Toxic ROTTEN_MEAT = register("rotten_meat", new Toxic.Builder(Ailment.of(SEVERE, FOOD_POISONING.and(POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.BAT, Ailment.of(MILD, STRONG_NAUSEA))
    );
    Toxic COOKED_MEAT = register("cooked_meat", new Toxic.Builder(Ailment.of(FAIR, FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.BAT, Ailment.INNERT)
    );

    Toxic RAW_FISH = register("raw_fish", new Toxic.Builder(Ailment.of(FAIR, FOOD_POISONING.and(POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.PEGASUS, Ailment.of(MILD, POISON.and(WEAK_NAUSEA)))
            .with(Race.ALICORN, Ailment.INNERT)
    );
    Toxic COOKED_FISH = register("cooked_fish", new Toxic.Builder(Ailment.of(MILD, FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.PEGASUS, Ailment.INNERT)
            .with(Race.ALICORN, Ailment.INNERT)
    );

    Toxic RAW_INSECT = register("raw_insect", new Toxic.Builder(Ailment.of(LETHAL, FOOD_POISONING))
            .with(Race.BAT, Ailment.of(MILD, WEAK_NAUSEA))
    );

    Toxic COOKED_INSECT = register("cooked_insect", new Toxic.Builder(Ailment.of(LETHAL, FOOD_POISONING))
            .food(UFoodComponents.INSECTS)
            .with(Race.BAT, Ailment.INNERT)
    );

    static void bootstrap() {}

    static Toxic register(String name, Toxic.Builder builder) {
        name = "food_types/" + name;
        return Registry.register(REGISTRY, name, builder.build(name));
    }
}
