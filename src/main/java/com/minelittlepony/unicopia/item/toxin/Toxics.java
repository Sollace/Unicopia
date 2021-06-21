package com.minelittlepony.unicopia.item.toxin;

import java.util.Optional;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.item.FoodComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;

import static com.minelittlepony.unicopia.item.toxin.Toxicity.*;
import static com.minelittlepony.unicopia.item.toxin.Toxin.*;

public interface Toxics {
    Registry<Toxic> REGISTRY = Registries.createSimple(new Identifier("unicopia:toxic"));

    Toxic EDIBLE =              forage("edible", SAFE, FOOD);
    Toxic RISKY =               forage("risky", FAIR, FOOD);
    Toxic MODERATE =            forage("moderate", MILD, FOOD);
    Toxic DANGEROUS =           forage("dangerous", SEVERE, FOOD);
    Toxic NAUSEATING =          forage("nauseating", SAFE, NAUSEA);
    Toxic RADIOACTIVE =         forage("radioactive", SAFE, RADIOACTIVITY);
    Toxic PRICKLY =             forage("prickly", SAFE, DAMAGE);
    Toxic STRENGHTENING =       forage("strengthening", SEVERE, STRENGTH);
    Toxic SEVERELY_NAUSEATING = forage("severely_nauseating", SEVERE, NAUSEA);
    Toxic BLINDING =            forage("blinding", SEVERE, BLINDNESS);
    Toxic SEVERELY_PRICKLY =    forage("severely_prickly", SEVERE, DAMAGE);

    Toxic RAW_MEAT =    meat(FoodType.RAW_MEAT, MILD);
    Toxic COOKED_MEAT = meat(FoodType.COOKED_MEAT, MILD);

    Toxic RAW_FISH =    meat(FoodType.RAW_FISH, FAIR);
    Toxic COOKED_FISH = meat(FoodType.COOKED_FISH, FAIR);

    static void bootstrap() {}

    static Toxic forage(String name, Toxicity toxicity, Toxin toxin) {
        if (toxin != FOOD) {
            toxin = FOOD.and(toxin);
        }
        return register("forage_" + name, UseAction.EAT, FoodType.FORAGE,
                Optional.of(UFoodComponents.RANDOM_FOLIAGE),
                new Ailment(toxicity, toxin),
                new Ailment(Toxicity.LETHAL, toxin));
    }

    static Toxic meat(FoodType type, Toxicity toxicity) {
        return register(type.name().toLowerCase(), UseAction.EAT, type,
                Optional.empty(),
                Ailment.INNERT,
                Ailment.of(toxicity));
    }

    static Toxic register(String name, UseAction action, FoodType type, Optional<FoodComponent> component,
            Ailment lower,
            Ailment upper) {
        return Registry.register(REGISTRY, name, new Toxic(action, type, component, UTags.item(name), lower, upper));
    }
}
