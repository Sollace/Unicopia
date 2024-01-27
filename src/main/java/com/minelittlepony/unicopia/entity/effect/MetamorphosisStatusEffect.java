package com.minelittlepony.unicopia.entity.effect;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class MetamorphosisStatusEffect extends StatusEffect {
    public static final int MAX_DURATION = 20 * 60;

    private static final Map<Race, StatusEffect> REGISTRY = new HashMap<>();

    public static final StatusEffect EARTH = register(0x886F0F, Race.EARTH);
    public static final StatusEffect UNICORN = register(0x88FFFF, Race.UNICORN);
    public static final StatusEffect PEGASUS = register(0x00C0ff, Race.PEGASUS);
    public static final StatusEffect BAT = register(0x152F13, Race.BAT);
    public static final StatusEffect CHANGELING = register(0xFFFF00, Race.CHANGELING);
    public static final StatusEffect KIRIN = register(0xFF8800, Race.KIRIN);
    public static final StatusEffect HIPPOGRIFF = register(0xE04F77, Race.HIPPOGRIFF);

    @Nullable
    public static StatusEffect forRace(Race race) {
        return REGISTRY.get(race);
    }

    public static StatusEffect register(int color, Race race) {
        Identifier id = Race.REGISTRY.getId(race);
        StatusEffect effect = new MetamorphosisStatusEffect(color, race);
        REGISTRY.put(race, effect);
        return Registry.register(Registries.STATUS_EFFECT,
                id.withPath(p -> "morph_race_" + p),
                effect
        );
    }

    public static Race getEffectiveRace(LivingEntity entity, Race fallback) {
        return entity.getStatusEffects().stream().filter(effect -> effect.getEffectType() instanceof MetamorphosisStatusEffect).map(effect -> {
            return ((MetamorphosisStatusEffect)effect.getEffectType()).getRace();
        }).findFirst().orElse(fallback);
    }

    private final Race race;

    private MetamorphosisStatusEffect(int color, Race race) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.race = race;
    }

    public Race getRace() {
        return race;
    }
}
