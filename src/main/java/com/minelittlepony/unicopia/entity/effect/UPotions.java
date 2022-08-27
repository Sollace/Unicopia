package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

public interface UPotions {
    Potion TRIBE_SWAP_EARTH_PONY = register("tribe_swap_earth", new Potion("unicopia.tribe_swap_earth", new StatusEffectInstance(RaceChangeStatusEffect.CHANGE_RACE_EARTH, RaceChangeStatusEffect.MAX_DURATION)));
    Potion TRIBE_SWAP_UNICORN = register("tribe_swap_unicorn", new Potion("unicopia.tribe_swap_unicorn", new StatusEffectInstance(RaceChangeStatusEffect.CHANGE_RACE_UNICORN, RaceChangeStatusEffect.MAX_DURATION)));
    Potion TRIBE_SWAP_PEGASUS = register("tribe_swap_pegasus", new Potion("unicopia.tribe_swap_pegasus", new StatusEffectInstance(RaceChangeStatusEffect.CHANGE_RACE_PEGASUS, RaceChangeStatusEffect.MAX_DURATION)));
    Potion TRIBE_SWAP_BAT = register("tribe_swap_bat", new Potion("unicopia.tribe_swap_bat", new StatusEffectInstance(RaceChangeStatusEffect.CHANGE_RACE_BAT, RaceChangeStatusEffect.MAX_DURATION)));
    Potion TRIBE_SWAP_CHANGELING = register("tribe_swap_changeling", new Potion("unicopia.tribe_swap_changeling", new StatusEffectInstance(RaceChangeStatusEffect.CHANGE_RACE_CHANGELING, RaceChangeStatusEffect.MAX_DURATION)));

    static Potion register(String name, Potion potion) {
        return Registry.register(Registry.POTION, Unicopia.id(name), potion);
    }

    static void bootstrap() {
        UEffects.bootstrap();
    }
}
