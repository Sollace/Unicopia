package com.minelittlepony.unicopia.entity.damage;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public interface UDamageTypes {
    RegistryKey<DamageType> EXHAUSTION = register("magical_exhaustion");
    RegistryKey<DamageType> ALICORN_AMULET = register("alicorn_amulet");
    RegistryKey<DamageType> FOOD_POISONING = register("food_poisoning");
    RegistryKey<DamageType> TRIBE_SWAP = register("tribe_swap");
    RegistryKey<DamageType> ZAP_APPLE = register("zap");
    RegistryKey<DamageType> KICK = register("kick");
    RegistryKey<DamageType> SMASH = register("smash");
    RegistryKey<DamageType> BAT_SCREECH = register("bat_screech");
    RegistryKey<DamageType> LOVE_DRAINING = register("love_draining");
    RegistryKey<DamageType> LIFE_DRAINING = register("life_draining");
    RegistryKey<DamageType> RAINBOOM = register("rainboom");
    RegistryKey<DamageType> STEAMROLLER = register("steamroller");
    RegistryKey<DamageType> GAVITY_WELL_RECOIL = register("gravity_well_recoil");
    RegistryKey<DamageType> SUN = register("sun");
    RegistryKey<DamageType> SUNLIGHT = register("sunlight");
    RegistryKey<DamageType> PETRIFIED = register("petrified");

    private static RegistryKey<DamageType> register(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Unicopia.id(name));
    }

    static void bootstrap() {

    }
}
