package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.MapCodec;

import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface UEnchantmentEffects {
    static void bootstrap() {
        register("group_based_attribute", GroupBasedAttributeEnchantmentEffect.CODEC);
        register("poisoned_joke_sound", AmbientSoundsEnchantmentEffect.CODEC);
        register("danger_sensor", DangerSensingEnchantmentEffect.CODEC);
        register("particle_trail", ParticleTrailEnchantmentEntityEffect.CODEC);
    }

    private static void register(String name, MapCodec<? extends EnchantmentEntityEffect> codec) {
        Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Unicopia.id("name"), codec);
    }
}
