package com.minelittlepony.unicopia.entity.effect;

import java.util.Objects;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public interface UPotions {
    MorphingPotion MORPH_EARTH_PONY = new MorphingPotion(Race.EARTH).registerBaseRecipes(Potions.STRENGTH, UItems.CURING_JOKE, UItems.PEBBLES);
    MorphingPotion MORPH_UNICORN = new MorphingPotion(Race.UNICORN).registerBaseRecipes(Potions.REGENERATION, UItems.BOTCHED_GEM, UItems.GEMSTONE);
    MorphingPotion MORPH_PEGASUS = new MorphingPotion(Race.PEGASUS).registerBaseRecipes(Potions.SWIFTNESS, UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER, Items.FEATHER);
    MorphingPotion MORPH_BAT = new MorphingPotion(Race.BAT).registerBaseRecipes(Potions.NIGHT_VISION, UItems.BUTTERFLY);
    MorphingPotion MORPH_CHANGELING = new MorphingPotion(Race.CHANGELING).registerBaseRecipes(Potions.HARMING, UItems.CARAPACE, UItems.ROTTEN_APPLE);
    MorphingPotion MORPH_KIRIN = new MorphingPotion(Race.KIRIN).registerBaseRecipes(Potions.FIRE_RESISTANCE, Items.MAGMA_CREAM);
    MorphingPotion MORPH_HIPPOGRIFF = new MorphingPotion(Race.HIPPOGRIFF).registerBaseRecipes(Potions.WATER_BREATHING, UItems.CLAM_SHELL, UItems.TURRET_SHELL, UItems.SCALLOP_SHELL);

    static RegistryEntry<Potion> register(String name, Potion potion) {
        return register(Unicopia.id(name), potion);
    }

    static RegistryEntry<Potion> register(Identifier id, Potion potion) {
        return Registry.registerReference(Registries.POTION, id, potion);
    }

    static void addRecipe(RegistryEntry<Potion> result, RegistryEntry<Potion> basePotion, Item...items) {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(registry -> {
            //Preconditions.checkArgument(BrewingRecipeRegistry.isBrewable(basePotion), "Base potion is not craftable. " + basePotion.getIdAsString() + " required for crafting " + result.getIdAsString());
            for (Item item : items) {
                registry.registerPotionRecipe(basePotion, item, result);
            }
        });

    }

    record MorphingPotion(Identifier id, RegistryEntry<Potion> shortEffect, RegistryEntry<Potion> longEffect, RegistryEntry<Potion> permanentEffect) {
        public MorphingPotion(Race race) {
            this(race.getId(),
                    Objects.requireNonNull(MetamorphosisStatusEffect.forRace(race), "No metamorphosis status effect registered for " + race.getId()),
                    Objects.requireNonNull(RaceChangeStatusEffect.forRace(race), "No race change status effect registered for " + race.getId())
            );
        }

        public MorphingPotion(Identifier id, RegistryEntry<StatusEffect> morphEffect, RegistryEntry<StatusEffect> permanentEffect) {
            this(id,
                    register(id.withPath(p -> "short_morph_" + p), new Potion(id.getNamespace() + ".short_morph_" + id.getPath(), new StatusEffectInstance(morphEffect, MetamorphosisStatusEffect.MAX_DURATION))),
                    register(id.withPath(p -> "long_morph_" + p), new Potion(id.getNamespace() + ".long_morph_" + id.getPath(), new StatusEffectInstance(morphEffect, MetamorphosisStatusEffect.MAX_DURATION * 10))),
                    register(id, new Potion(id.getNamespace() + ".tribe_swap_" + id.getPath(), new StatusEffectInstance(permanentEffect, RaceChangeStatusEffect.MAX_DURATION)))
            );
        }

        public MorphingPotion registerBaseRecipes(RegistryEntry<Potion> basePotion, Item...items) {
            addRecipe(shortEffect, basePotion, items);
            addRecipe(longEffect, shortEffect, Items.REDSTONE);
            addRecipe(permanentEffect, longEffect, UItems.CURING_JOKE);
            return this;
        }
    }

    static void bootstrap() {
        UEffects.bootstrap();
    }
}
