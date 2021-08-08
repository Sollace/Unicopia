package com.minelittlepony.unicopia.entity.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UPotions {
    Potion TRIBE_SWAP_EARTH_PONY = registerRacePotion(RaceChangeStatusEffect.CHANGE_RACE_EARTH);
    Potion TRIBE_SWAP_UNICORN = registerRacePotion(RaceChangeStatusEffect.CHANGE_RACE_UNICORN);
    Potion TRIBE_SWAP_PEGASUS = registerRacePotion(RaceChangeStatusEffect.CHANGE_RACE_PEGASUS);
    Potion TRIBE_SWAP_BAT = registerRacePotion(RaceChangeStatusEffect.CHANGE_RACE_BAT);
    Potion TRIBE_SWAP_CHANGELING = registerRacePotion(RaceChangeStatusEffect.CHANGE_RACE_CHANGELING);

    static Potion registerRacePotion(RaceChangeStatusEffect effect) {
        String name = "tribe_swap_" + effect.getSpecies().name().toLowerCase();
        return register(name, new Potion("unicopia." + name,
                new StatusEffectInstance(effect, RaceChangeStatusEffect.MAX_DURATION)));
    }

    static Potion register(String name, Potion potion) {
        return Registry.register(Registry.POTION, new Identifier("unicopia", name), potion);
    }

    static void bootstrap() {
        @SuppressWarnings("unused")
        StatusEffect e = SunBlindnessStatusEffect.INSTANCE;
    }
}
