package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.diet.DietProfile.Multiplier;
import com.minelittlepony.unicopia.diet.FoodGroupEffects;
import com.minelittlepony.unicopia.diet.affliction.ClearLoveSicknessAffliction;
import com.minelittlepony.unicopia.diet.affliction.CompoundAffliction;
import com.minelittlepony.unicopia.diet.affliction.HealingAffliction;
import com.minelittlepony.unicopia.diet.affliction.LoseHungerAffliction;
import com.minelittlepony.unicopia.diet.affliction.Range;
import com.minelittlepony.unicopia.diet.affliction.StatusEffectAffliction;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.UFoodComponents;

import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.effect.StatusEffects;

public class DietProfileGenerator {

    public void generate(BiConsumer<Race, DietProfile> exporter) {
        // Pinecones are for everypony
        var pineconeMultiplier = new Multiplier.Builder().tag(Unicopia.id("pinecone")).hunger(0.9F).saturation(0.9F).build();
        var bakedGoodPreference = new Multiplier.Builder().tag(Unicopia.id("baked_goods")).build();
        var bakedGoodExtremePreference = new Multiplier.Builder().tag(Unicopia.id("baked_goods")).hunger(1.2F).saturation(2).build();
        var bakedGoodNonPreference = new Multiplier.Builder().tag(Unicopia.id("baked_goods")).hunger(0.4F).saturation(0.2F).build();
        var properMeatStandards = new Multiplier.Builder().tag(Unicopia.id("love"))
                .tag(Unicopia.id("meat/raw")).tag(Unicopia.id("insect/raw")).tag(Unicopia.id("fish/raw"))
                .tag(Unicopia.id("meat/rotten")).tag(Unicopia.id("insect/rotten")).tag(Unicopia.id("fish/rotten"))
                .hunger(0).saturation(0).build();
        var avianMeatStandards = new Multiplier.Builder().tag(Unicopia.id("love"))
                .tag(Unicopia.id("meat/raw")).tag(Unicopia.id("insect/raw"))
                .tag(Unicopia.id("meat/rotten")).tag(Unicopia.id("insect/rotten"))
                .hunger(0).saturation(0).build();
        var loveSicknessEffects = CompoundAffliction.of(
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 95),
                new StatusEffectAffliction(StatusEffects.WEAKNESS, Range.of(200), Range.of(1), 0),
                new LoseHungerAffliction(0.5F));
        var seaFoodExclusions = new Multiplier.Builder()
                .tag(Unicopia.id("sea_vegetable/raw")).tag(Unicopia.id("sea_vegetable/cooked"))
                .tag(Unicopia.id("shells")).tag(Unicopia.id("special_shells"))
                .hunger(0).saturation(0).build();

        exporter.accept(Race.HUMAN, new DietProfile(1, 0, List.of(), List.of(
                new FoodGroupEffects.Builder()
                .tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("fish/raw")).tag(Unicopia.id("fish/rotten"))
                .tag(Unicopia.id("meat/cooked")).tag(Unicopia.id("meat/raw")).tag(Unicopia.id("meat/rotten"))
                .tag(Unicopia.id("sea_vegetable/cooked")).tag(Unicopia.id("sea_vegetable/raw"))
                .tag(Unicopia.id("pinecone"))
                .build()
        ), Optional.empty()));
        // Alicorns are a mashup of unicorn, pegasus, and earth pony eating habits
        exporter.accept(Race.ALICORN, new DietProfile(0.9F, 1, List.of(
                // Pastries are their passion
                bakedGoodExtremePreference, pineconeMultiplier, avianMeatStandards, seaFoodExclusions,
                // They have a more of a sweet tooth than earth ponies
                new Multiplier.Builder().tag(Unicopia.id("desserts")).hunger(2.5F).saturation(1.7F).build(),
                new Multiplier.Builder().tag(Unicopia.id("candy")).tag(Unicopia.id("rocks")).hunger(1.5F).saturation(1.3F).build(),

                // Cannot eat love, or raw/rotten meats and fish
                // Can eat raw and rotten fish but still prefers if they are cooked
                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(1.5F).saturation(1.5F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(0.25F).saturation(0.16F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(0.1F).saturation(0.7F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.5F).saturation(0.8F).build(),
                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.25F).saturation(0.5F).build()
        ), List.of(
                // Can safely eat fresh and cooked fish with no ill effects
                new FoodGroupEffects.Builder().tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("fish/raw")).build(),
                // Is less affected when eating rotten fish
                new FoodGroupEffects.Builder().tag(Unicopia.id("fish/rotten")).ailment(new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(50), Range.of(2), 95)).build()
        ), Optional.empty()));
        // Unicorns have a general even preference of foods
        exporter.accept(Race.UNICORN, new DietProfile(1.1F, 1, List.of(
                bakedGoodPreference, pineconeMultiplier, seaFoodExclusions,
                new Multiplier.Builder().tag(Unicopia.id("love")).hunger(0).saturation(0).build(),

                // Improved benefits from cooking their food
                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(0.3F).saturation(0.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(0.4F).saturation(0.4F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(0.1F).saturation(0.1F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.25F).saturation(0.1F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/raw")).hunger(0.3F).saturation(0.1F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/raw")).hunger(0.15F).saturation(0.1F).build(),

                // Can still eat raw and rotten but at a reduced yield
                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.1F).saturation(0.1F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/rotten")).hunger(0.1F).saturation(0.1F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/rotten")).hunger(0).saturation(0.1F).build()
        ), List.of(), Optional.empty()));
        // Bats prefer cooked foods over raw, and meat/insects over fish
        exporter.accept(Race.BAT, new DietProfile(0.7F, 0.9F, List.of(
                pineconeMultiplier, seaFoodExclusions,
                // Doesn't like baked goods but really likes meats, fish, and insects
                bakedGoodNonPreference,

                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(0.75F).saturation(0.75F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(1.15F).saturation(1.16F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(1.75F).saturation(1.74F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.5F).saturation(0.6F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/raw")).hunger(0.25F).saturation(0.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/raw")).hunger(1).saturation(1).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.24F).saturation(0.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/rotten")).hunger(0.2F).saturation(0.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/rotten")).hunger(0.9F).saturation(0.9F).build()
        ), List.of(
                // Gets food poisoning from eating rotten and raw meat
                new FoodGroupEffects.Builder().tag(Unicopia.id("fish/rotten")).tag(Unicopia.id("meat/raw")).tag(Unicopia.id("meat/rotten")).ailment(new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 5)).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("insect/rotten")).ailment(new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(50), Range.of(1), 15)).build(),
                // Can eat cooked meat and insects without negative effects
                new FoodGroupEffects.Builder().tag(Unicopia.id("insect/cooked")).tag(Unicopia.id("meat/cooked")).build(),
                // Becomes hyper when eating mangoes
                new FoodGroupEffects.Builder().tag(Unicopia.id("bat_ponys_delight")).ailment(CompoundAffliction.of(
                        new StatusEffectAffliction(StatusEffects.HEALTH_BOOST, Range.of(30, 60), Range.of(2, 6), 0),
                        new StatusEffectAffliction(StatusEffects.JUMP_BOOST, Range.of(30, 60), Range.of(1, 6), 0),
                        new StatusEffectAffliction(StatusEffects.REGENERATION, Range.of(3, 30), Range.of(3, 6), 0)
                )).build()
        ), Optional.empty()));
        // Much like Earth Ponies, Kirins must cook their meat before they eat it
        exporter.accept(Race.KIRIN, new DietProfile(0.6F, 0.9F, List.of(
                // Cannot eat love, or raw/rotten meats and fish
                bakedGoodPreference, properMeatStandards, pineconeMultiplier, seaFoodExclusions,

                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(0.75F).saturation(0.35F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(1.5F).saturation(1.6F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(0.25F).saturation(0.74F).build()
        ), List.of(
                // can eat these without negative effects
                new FoodGroupEffects.Builder().tag(Unicopia.id("insect/cooked")).tag(Unicopia.id("meat/cooked")).food(FoodComponents.COOKED_BEEF).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("foraging/blinding")).food(4, 0.2F).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("foraging/prickly")).food(0, 1.5F).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("foraging/severely_prickly")).food(2, 0.9F).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("foraging/strengthening")).food(4, 0.2F).ailment(new StatusEffectAffliction(StatusEffects.STRENGTH, Range.of(1300), Range.of(0), 0)).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("foraging/glowing")).food(1, 1.6F).ailment(new StatusEffectAffliction(StatusEffects.GLOWING, Range.of(30), Range.of(0), 30)).build()
        ), Optional.empty()));
        // Earth Ponies are vegans. They get the most from foraging
        exporter.accept(Race.EARTH, new DietProfile(0.7F, 1.2F, List.of(
                // Pastries are their passion
                // If they must eat meat, they have to cook it and not let it spoil.
                bakedGoodExtremePreference, pineconeMultiplier, properMeatStandards, seaFoodExclusions,
                // They have a sweet tooth
                new Multiplier.Builder().tag(Unicopia.id("candy")).tag(Unicopia.id("desserts")).tag(Unicopia.id("rocks")).hunger(2.5F).saturation(1.7F).build(),
                new Multiplier.Builder().tag(Unicopia.id("gems")).hunger(0.5F).saturation(0.7F).build(),
                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(0.2F).saturation(0.3F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(0.1F).saturation(0.1F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(0.1F).saturation(0.1F).build()
        ), List.of(
                // Candy and rocks gives them a massive saturation boost. Maybe too much?
                new FoodGroupEffects.Builder().tag(Unicopia.id("candy")).tag(Unicopia.id("rocks")).food(UFoodComponents.builder(5, 12).alwaysEdible()).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("gems")).food(UFoodComponents.builder(2, 1.5F).snack().alwaysEdible()).build(),
                new FoodGroupEffects.Builder().tag(Unicopia.id("desserts")).food(UFoodComponents.builder(12, 32).snack().alwaysEdible()).build()
        ), Optional.empty()));
        // Pegasi prefer fish over other food sources
        exporter.accept(Race.PEGASUS, new DietProfile(0.9F, 1, List.of(
                bakedGoodPreference, pineconeMultiplier, avianMeatStandards, seaFoodExclusions,
                // Cannot eat love, or raw/rotten meat
                // Can eat raw and rotten fish but still prefers if they are cooked
                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(1.5F).saturation(1.5F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(0.25F).saturation(0.16F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(0.1F).saturation(0.7F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.5F).saturation(0.8F).build(),
                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.25F).saturation(0.5F).build()
        ), List.of(
                // Can safely eat fresh and cooked fish with no ill effects
                new FoodGroupEffects.Builder().tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("fish/raw")).build(),
                // Is less affected when eating rotten fish
                new FoodGroupEffects.Builder().tag(Unicopia.id("fish/rotten")).ailment(new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(50), Range.of(2), 95)).build()
        ), Optional.empty()));
        // Changelings like meat and fish but really prefer feasting on ponies' love directly from the tap
        exporter.accept(Race.CHANGELING, new DietProfile(0.15F, 0.1F, List.of(
                // Doesn't like baked goods but really likes meats, fish, and insects
                bakedGoodNonPreference, pineconeMultiplier, seaFoodExclusions,

                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(0.5F).saturation(1.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(0.9F).saturation(1.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/cooked")).hunger(1.2F).saturation(1.3F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.15F).saturation(0.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/raw")).hunger(1.25F).saturation(1.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/raw")).hunger(1).saturation(1).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.24F).saturation(0.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/rotten")).hunger(0.2F).saturation(0.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("insect/rotten")).hunger(0.9F).saturation(0.9F).build(),

                new Multiplier.Builder().tag(Unicopia.id("love")).hunger(1).saturation(1.5F).build()
        ), List.of(
                // Can eat fish, meat, insects, and love without negative effects
                new FoodGroupEffects.Builder()
                    .tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("meat/cooked")).tag(Unicopia.id("insect/cooked"))
                    .tag(Unicopia.id("fish/rotten")).tag(Unicopia.id("meat/rotten")).tag(Unicopia.id("insect/rotten"))
                    .tag(Unicopia.id("fish/raw")).tag(Unicopia.id("meat/raw")).tag(Unicopia.id("insect/raw"))
                    .tag(Unicopia.id("baked_goods")).tag(Unicopia.id("pinecone"))
                    .tag(Unicopia.id("love")).ailment(ClearLoveSicknessAffliction.INSTANCE).build(),

                new FoodGroupEffects.Builder()
                    .tag(Unicopia.id("foraging/blinding")).tag(Unicopia.id("foraging/dangerous")).tag(Unicopia.id("foraging/edible_filling"))
                    .tag(Unicopia.id("foraging/edible")).tag(Unicopia.id("foraging/leafy_greens")).tag(Unicopia.id("foraging/nauseating"))
                    .tag(Unicopia.id("foraging/prickly")).tag(Unicopia.id("foraging/glowing")).tag(Unicopia.id("foraging/risky"))
                    .tag(Unicopia.id("foraging/severely_nauseating")).tag(Unicopia.id("foraging/severely_prickly"))
                    .tag(Unicopia.id("foraging/strengthening"))
                    .ailment(loveSicknessEffects)
                    .build()
        ), Optional.empty()));
        // Hippogriffs like fish, nuts, and seeds
        exporter.accept(Race.HIPPOGRIFF, new DietProfile(0.5F, 0.8F, List.of(
                bakedGoodPreference, pineconeMultiplier, seaFoodExclusions,

                new Multiplier.Builder().tag(Unicopia.id("love"))
                    .tag(Unicopia.id("insect/cooked")).tag(Unicopia.id("insect/raw")).tag(Unicopia.id("insect/rotten"))
                    .hunger(0).saturation(0).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(1.5F).saturation(1.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/cooked")).hunger(1.9F).saturation(1.2F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.85F).saturation(0.95F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/raw")).hunger(0.75F).saturation(0.75F).build(),

                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.24F).saturation(0.25F).build(),
                new Multiplier.Builder().tag(Unicopia.id("meat/rotten")).hunger(0.3F).saturation(0.5F).build(),

                new Multiplier.Builder().tag(Unicopia.id("nuts_and_seeds")).hunger(1.4F).saturation(1.4F).build()
        ), List.of(
                // Can eat fish and prickly foods without negative effect
                new FoodGroupEffects.Builder()
                    .tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("fish/raw")).tag(Unicopia.id("fish/rotten"))
                    .tag(Unicopia.id("foraging/prickly")).tag(Unicopia.id("foraging/severely_prickly"))
                    .build(),
                // Gains more health from pinecones
                new FoodGroupEffects.Builder().tag(Unicopia.id("pinecone")).ailment(new HealingAffliction(3)).build()
        ), Optional.empty()));
        // Seaponies can eat seaweed, kelp, shells, and other undersea foods
        exporter.accept(Race.SEAPONY, new DietProfile(0.5F, 0.8F, List.of(
                new Multiplier.Builder().tag(Unicopia.id("fish/cooked")).hunger(1.5F).saturation(1.2F).build(),
                new Multiplier.Builder().tag(Unicopia.id("fish/raw")).hunger(0.85F).saturation(0.95F).build(),
                new Multiplier.Builder().tag(Unicopia.id("fish/rotten")).hunger(0.24F).saturation(0.25F).build(),
                new Multiplier.Builder()
                    .tag(Unicopia.id("sea_vegetable/raw"))
                    .tag(Unicopia.id("sea_vegetable/cooked"))
                    .tag(Unicopia.id("shells")).tag(Unicopia.id("special_shells"))
                    .hunger(1).saturation(1).build()
        ), List.of(
                // Can eat fish without negative effect
                new FoodGroupEffects.Builder()
                    .tag(Unicopia.id("fish/cooked")).tag(Unicopia.id("fish/raw")).tag(Unicopia.id("fish/rotten"))
                    .build(),
                // Gains more health from pinecones
                new FoodGroupEffects.Builder().tag(Unicopia.id("pinecone")).ailment(new HealingAffliction(3)).build()
        ), Optional.empty()));
    }
}

















