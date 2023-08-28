package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registry;

import static com.minelittlepony.unicopia.item.toxin.Toxicity.*;
import static com.minelittlepony.unicopia.item.toxin.Ailment.*;
import static com.minelittlepony.unicopia.item.toxin.Toxin.*;

public interface Toxics {
    Registry<ToxicRegistryEntry> REGISTRY = RegistryUtils.createSimple(Unicopia.id("toxic"));

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

    Toxic FORAGE_RISKY = register("forage_risky", new Toxic.Builder(of(FAIR, WEAK_NAUSEA.withChance(20)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_MODERATE = register("forage_moderate", new Toxic.Builder(of(MILD, POISON.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_DANGEROUS = register("forage_dangerous", new Toxic.Builder(of(SEVERE, FOOD_POISONING))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic FORAGE_NAUSEATING = register("forage_nauseating", new Toxic.Builder(of(SAFE, NAUSEA.and(WEAKNESS.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic FORAGE_RADIOACTIVE = register("forage_radioactive", new Toxic.Builder(of(SAFE, NAUSEA.and(RADIOACTIVITY.withChance(30))))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic FORAGE_PRICKLY = register("forage_prickly", new Toxic.Builder(of(SAFE, PRICKLING.withChance(30)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(NAUSEA)))
    );
    Toxic FORAGE_STRENGHTENING = register("forage_strengthening", new Toxic.Builder(of(SEVERE, STRENGTH.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
    );
    Toxic FORAGE_SEVERELY_NAUSEATING = register("forage_severely_nauseating", new Toxic.Builder(of(SEVERE, STRONG_NAUSEA.and(WEAKNESS)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING.and(WEAKNESS)))
    );
    Toxic FORAGE_BLINDING = register("forage_blinding", new Toxic.Builder(of(SEVERE, BLINDNESS.and(WEAK_NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic FORAGE_SEVERELY_PRICKLY = register("forage_severely_prickly", new Toxic.Builder(of(SEVERE, PRICKLING.and(NAUSEA)))
            .food(UFoodComponents.RANDOM_FOLIAGE)
            .with(Race.HUMAN, of(LETHAL, FOOD_POISONING))
    );
    Toxic RAW_MEAT = register("raw_meat", new Toxic.Builder(of(SEVERE, FOOD_POISONING.withChance(5).and(POISON.withChance(20))))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.BAT, of(MILD, WEAK_NAUSEA))
    );
    Toxic ROTTEN_MEAT = register("rotten_meat", new Toxic.Builder(of(SEVERE, FOOD_POISONING.and(POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.BAT, of(MILD, STRONG_NAUSEA))
            .with(Race.CHANGELING, Ailment.INNERT)
    );
    Toxic COOKED_MEAT = register("cooked_meat", new Toxic.Builder(of(FAIR, FOOD_POISONING))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.BAT, Ailment.INNERT)
    );

    Toxic RAW_FISH = register("raw_fish", new Toxic.Builder(of(FAIR, FOOD_POISONING.and(POISON)))
            .with(Race.HUMAN, Ailment.INNERT)
            .with(Race.PEGASUS, of(MILD, POISON.and(WEAK_NAUSEA)))
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
            .with(Race.BAT, of(MILD, WEAK_NAUSEA))
            .with(Race.CHANGELING, Ailment.INNERT)
    );

    Toxic COOKED_INSECT = register("cooked_insect", new Toxic.Builder(of(LETHAL, FOOD_POISONING))
            .food(UFoodComponents.INSECTS)
            .with(Race.CHANGELING, Ailment.INNERT)
            .with(Race.BAT, Ailment.INNERT)
    );

    Toxic LOVE = register("love", new Toxic.Builder(Ailment.INNERT)
            .with(Race.CHANGELING, of(Toxicity.SAFE, Toxin.LOVE_CONSUMPTION))
    );

    Toxic PINECONE = register("pinecone", new Toxic.Builder(of(Toxicity.SAFE, Toxin.healing(1)))
            .with(Race.HUMAN, Ailment.INNERT)
    );

    Toxic BAT_PONYS_DELIGHT = register("bat_ponys_delight", new Toxic.Builder(Ailment.INNERT)
            .with(Race.BAT, Ailment.of(Toxicity.SAFE,
                    Toxin.of(StatusEffects.HEALTH_BOOST, 30, 60, 2, 6)
                        .and(Toxin.of(StatusEffects.JUMP_BOOST, 30, 60, 1, 6))
                        .and(Toxin.of(StatusEffects.SPEED, 30, 30, 1, 6))
                        .and(Toxin.of(StatusEffects.REGENERATION, 3, 30, 3, 6))
            ))
    );

    static void bootstrap() {}

    static Toxic register(String name, Toxic.Builder builder) {
        return Registry.register(REGISTRY, Unicopia.id(name), new ToxicRegistryEntry(builder.build(), UTags.item("food_types/" + name))).value();
    }
}
