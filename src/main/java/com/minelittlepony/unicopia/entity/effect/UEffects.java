package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;

public interface UEffects {
    RegistryEntry<StatusEffect> FOOD_POISONING = register("food_poisoning", new FoodPoisoningStatusEffect(3484199));
    RegistryEntry<StatusEffect> SUN_BLINDNESS = register("sun_blindness", new SunBlindnessStatusEffect(0x886F0F));
    /**
     * Status effect emitted by players with a high level of corruption.
     * When affecting an entity, will give them a random chance to reproduce or duplicate themselves when they die.
     */
    RegistryEntry<StatusEffect> CORRUPT_INFLUENCE = register("corrupt_influence", new CorruptInfluenceStatusEffect(0x00FF00));
    RegistryEntry<StatusEffect> PARALYSIS = register("paralysis", new SimpleStatusEffect(StatusEffectCategory.HARMFUL, 0, false));
    RegistryEntry<StatusEffect> FORTIFICATION = register("fortification", new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x000077, false));
    RegistryEntry<StatusEffect> BROKEN_WINGS = register("broken_wings", new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xEEAA00, false));
    /**
     * Side-effect of wearing the alicorn amulet.
     * Causes the player to lose grip on whatever item they're holding.
     */
    RegistryEntry<StatusEffect> BUTTER_FINGERS = register("butter_fingers", new ButterfingersStatusEffect(0x888800));

    RegistryEntry<StatusEffect> SEAPONYS_GRACE = register("seaponys_grace", new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x0000EE, false));
    RegistryEntry<StatusEffect> SEAPONYS_IRE = register("seaponys_ire", new SimpleStatusEffect(StatusEffectCategory.HARMFUL, 0xEE00EE, false));

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Unicopia.id(name), effect);
    }

    static void bootstrap() {}
}
