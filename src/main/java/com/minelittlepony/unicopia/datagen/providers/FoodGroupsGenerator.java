package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.function.BiConsumer;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.diet.FoodGroupEffects;
import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.diet.affliction.CompoundAffliction;
import com.minelittlepony.unicopia.diet.affliction.HealingAffliction;
import com.minelittlepony.unicopia.diet.affliction.LoseHungerAffliction;
import com.minelittlepony.unicopia.diet.affliction.Range;
import com.minelittlepony.unicopia.diet.affliction.StatusEffectAffliction;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.UFoodComponents;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class FoodGroupsGenerator {
    public void generate(BiConsumer<Identifier, FoodGroupEffects.Builder> exporter) {
        exporter.accept(Unicopia.id("baked_goods"), new FoodGroupEffects.Builder().tag(UTags.Items.BAKED_GOODS).food(FoodComponents.BREAD));
        exporter.accept(Unicopia.id("bat_ponys_delight"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.MANGOES).food(UFoodComponents.MANGO));
        exporter.accept(Unicopia.id("candy"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.CANDY).food(UFoodComponents.CANDY));
        exporter.accept(Unicopia.id("desserts"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.DESSERTS).food(FoodComponents.COOKIE));
        exporter.accept(Unicopia.id("fruit"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.FRUITS).food(UFoodComponents.BANANA));
        exporter.accept(Unicopia.id("rocks"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.ROCKS).tag(UTags.Items.ROCK_STEWS).food(FoodComponents.MUSHROOM_STEW));
        exporter.accept(Unicopia.id("shells"), new FoodGroupEffects.Builder().tag(UTags.Items.SHELLS).food(UFoodComponents.SHELL));
        exporter.accept(Unicopia.id("special_shells"), new FoodGroupEffects.Builder().tag(UTags.Items.SPECIAL_SHELLS).food(UFoodComponents.SHELLY));
        exporter.accept(Unicopia.id("love"), new FoodGroupEffects.Builder().tag(UTags.Items.CONTAINER_WITH_LOVE).food(UFoodComponents.LOVE_MUG).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(50), Range.of(2), 0),
                new LoseHungerAffliction(0.5F)
        ))));
        exporter.accept(Unicopia.id("nuts_and_seeds"), new FoodGroupEffects.Builder()
                .tag(UConventionalTags.Items.GRAIN).tag(UConventionalTags.Items.NUTS).tag(UConventionalTags.Items.SEEDS)
                .food(UFoodComponents.BANANA)
        );
        exporter.accept(Unicopia.id("pinecone"), new FoodGroupEffects.Builder().tag(UConventionalTags.Items.PINECONES).food(UFoodComponents.PINECONE).ailment(new CompoundAffliction(List.<Affliction>of(
                new HealingAffliction(1)
        ))));

        provideMeatCategory("fish",
                UConventionalTags.Items.COOKED_FISH, UConventionalTags.Items.RAW_FISH, UConventionalTags.Items.ROTTEN_FISH,
                FoodComponents.COOKED_COD, FoodComponents.COD, FoodComponents.ROTTEN_FLESH, exporter);
        provideMeatCategory("meat",
                UConventionalTags.Items.COOKED_MEAT, UConventionalTags.Items.RAW_MEAT, UConventionalTags.Items.ROTTEN_MEAT,
                FoodComponents.COOKED_BEEF, FoodComponents.BEEF, FoodComponents.ROTTEN_FLESH, exporter);
        provideMeatCategory("insect",
                UConventionalTags.Items.COOKED_INSECT, UConventionalTags.Items.RAW_INSECT, UConventionalTags.Items.ROTTEN_INSECT,
                FoodComponents.COOKED_BEEF, FoodComponents.BEEF, FoodComponents.ROTTEN_FLESH, exporter);
        provideVegetableCategory("sea_vegetable",
                UTags.Items.HIGH_QUALITY_SEA_VEGETABLES, UTags.Items.LOW_QUALITY_SEA_VEGETABLES,
                FoodComponents.COOKED_BEEF, FoodComponents.BEEF, exporter);

        exporter.accept(Unicopia.id("foraging/blinding"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_BLINDING).food(4, 0.2F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.BLINDNESS, Range.of(30), Range.of(0), 50),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 12)
        ))));
        exporter.accept(Unicopia.id("foraging/dangerous"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_DANGEROUS).food(3, 0.3F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(250), Range.of(2), 4)
        ))));
        exporter.accept(Unicopia.id("foraging/edible_filling"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_FILLING).food(17, 0.6F));
        exporter.accept(Unicopia.id("foraging/edible"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_SAFE).food(2, 1));
        exporter.accept(Unicopia.id("foraging/leafy_greens"), new FoodGroupEffects.Builder().tag(ItemTags.LEAVES).food(1, 1.4F));
        exporter.accept(Unicopia.id("foraging/nauseating"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_NAUSEATING).food(5, 0.5F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.WEAKNESS, Range.of(200), Range.of(1), 30),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(200), Range.of(2), 0)
        ))));
        exporter.accept(Unicopia.id("foraging/prickly"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_PRICKLY).food(0, 1.5F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.INSTANT_DAMAGE, Range.of(1), Range.of(0), 30)
        ))));
        exporter.accept(Unicopia.id("foraging/glowing"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_GLOWING).food(1, 1.6F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.GLOWING, Range.of(30), Range.of(0), 30),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 0)
        ))));
        exporter.accept(Unicopia.id("foraging/risky"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_RISKY).food(9, 1.1F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 80)
        ))));
        exporter.accept(Unicopia.id("foraging/severely_nauseating"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_SEVERE_NAUSEATING).food(3, 0.9F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.WEAKNESS, Range.of(200), Range.of(1), 0),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 80)
        ))));
        exporter.accept(Unicopia.id("foraging/severely_prickly"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_SEVERE_PRICKLY).food(2, 0.9F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.INSTANT_DAMAGE, Range.of(1), Range.of(0), 0),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 80)
        ))));
        exporter.accept(Unicopia.id("foraging/strengthening"), new FoodGroupEffects.Builder().tag(UTags.Items.FORAGE_STRENGHENING).food(4, 0.2F).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.STRENGTH, Range.of(1300), Range.of(0), 0),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 70)
        ))));
    }

    private void provideMeatCategory(String name,
            TagKey<Item> cookedTag, TagKey<Item> rawTag, TagKey<Item> rottenTag,
            FoodComponent cooked, FoodComponent raw, FoodComponent rotten,
            BiConsumer<Identifier, FoodGroupEffects.Builder> exporter) {
        exporter.accept(Unicopia.id(name + "/cooked"), new FoodGroupEffects.Builder().tag(cookedTag).food(cooked).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 25)
        ))));
        exporter.accept(Unicopia.id(name + "/raw"), new FoodGroupEffects.Builder().tag(rawTag).food(raw).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.POISON, Range.of(45), Range.of(2), 80),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 65)
        ))));
        exporter.accept(Unicopia.id(name + "/rotten"), new FoodGroupEffects.Builder().tag(rottenTag).food(rotten).ailment(new CompoundAffliction(List.<Affliction>of(
                new StatusEffectAffliction(StatusEffects.POISON, Range.of(45), Range.of(2), 80),
                new StatusEffectAffliction(UEffects.FOOD_POISONING, Range.of(100), Range.of(2), 95)
        ))));
    }

    private void provideVegetableCategory(String name,
            TagKey<Item> cookedTag, TagKey<Item> rawTag,
            FoodComponent cooked, FoodComponent raw,
            BiConsumer<Identifier, FoodGroupEffects.Builder> exporter) {
        exporter.accept(Unicopia.id(name + "/cooked"), new FoodGroupEffects.Builder().tag(cookedTag).food(cooked));
        exporter.accept(Unicopia.id(name + "/raw"), new FoodGroupEffects.Builder().tag(rawTag).food(raw));
    }
}
