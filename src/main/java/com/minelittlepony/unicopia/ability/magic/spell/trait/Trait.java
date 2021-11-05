package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.util.Identifier;

public enum Trait {
    LIFE(TraitGroup.NATURE),
    ENERGY(TraitGroup.NATURE),
    REBIRTH(TraitGroup.NATURE),
    GROWTH(TraitGroup.NATURE),

    WATER(TraitGroup.ELEMENTAL),
    EARTH(TraitGroup.ELEMENTAL),
    FIRE(TraitGroup.ELEMENTAL),
    AIR(TraitGroup.ELEMENTAL),

    CORRUPTION(TraitGroup.DARKNESS),
    DEATH(TraitGroup.DARKNESS),
    FAMINE(TraitGroup.DARKNESS),
    PESTILENCE(TraitGroup.DARKNESS);

    public static final Map<String, Trait> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Trait::name, Function.identity()));

    private final Identifier id;
    private final TraitGroup group;

    Trait(TraitGroup group) {
        this.id = new Identifier("unicopia", "spell/trait/" + name().toLowerCase());
        this.group = group;
    }

    public Identifier getId() {
        return id;
    }

    public TraitGroup getGroup() {
        return group;
    }
}
