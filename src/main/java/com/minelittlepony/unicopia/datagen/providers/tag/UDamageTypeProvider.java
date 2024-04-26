package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.DamageTypeTags;

public class UDamageTypeProvider extends FabricTagProvider<DamageType> {
    public UDamageTypeProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(UTags.DamageTypes.SPELLBOOK_IMMUNE_TO).add(
                UDamageTypes.ZAP_APPLE, UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.RAINBOOM, UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.SMASH
        ).forceAddTag(DamageTypeTags.IS_FALL).forceAddTag(DamageTypeTags.IS_FREEZING).forceAddTag(DamageTypeTags.IS_LIGHTNING).forceAddTag(DamageTypeTags.IS_PROJECTILE);
        getOrCreateTagBuilder(UTags.DamageTypes.FROM_ROCKS).add(UDamageTypes.ROCK);
        getOrCreateTagBuilder(UTags.DamageTypes.FROM_HORSESHOES).add(UDamageTypes.HORSESHOE);
        getOrCreateTagBuilder(UTags.DamageTypes.BREAKS_SUNGLASSES).add(UDamageTypes.BAT_SCREECH, UDamageTypes.RAINBOOM);

        getOrCreateTagBuilder(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.ALICORN_AMULET, UDamageTypes.ZAP_APPLE, UDamageTypes.KICK, UDamageTypes.SMASH,
                UDamageTypes.BAT_SCREECH, UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING, UDamageTypes.RAINBOOM, UDamageTypes.STEAMROLLER
        );
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.GAVITY_WELL_RECOIL, UDamageTypes.ALICORN_AMULET,
                UDamageTypes.ZAP_APPLE, UDamageTypes.KICK, UDamageTypes.SMASH, UDamageTypes.BAT_SCREECH,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING, UDamageTypes.STEAMROLLER, UDamageTypes.RAINBOOM,
                UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.PETRIFIED
        );
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY).add(UDamageTypes.TRIBE_SWAP);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.BAT_SCREECH, UDamageTypes.ALICORN_AMULET,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.RAINBOOM, UDamageTypes.TRIBE_SWAP
        );
        getOrCreateTagBuilder(DamageTypeTags.IS_FIRE).add(UDamageTypes.SUN, UDamageTypes.SUNLIGHT, UDamageTypes.PETRIFIED);
        getOrCreateTagBuilder(DamageTypeTags.IS_LIGHTNING).add(UDamageTypes.ZAP_APPLE);
        getOrCreateTagBuilder(DamageTypeTags.WITCH_RESISTANT_TO).add(
                UDamageTypes.EXHAUSTION, UDamageTypes.ALICORN_AMULET, UDamageTypes.ZAP_APPLE,
                UDamageTypes.LOVE_DRAINING, UDamageTypes.LIFE_DRAINING,
                UDamageTypes.KICK, UDamageTypes.SMASH, UDamageTypes.STEAMROLLER
        );
    }
}
