package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.util.Identifier;

public enum Trait {
    STRENGTH(TraitGroup.NATURAL),
    FOCUS(TraitGroup.NATURAL),
    KNOWLEDGE(TraitGroup.NATURAL),
    LIFE(TraitGroup.NATURAL),

    POWER(TraitGroup.ELEMENTAL),
    EARTH(TraitGroup.ELEMENTAL),
    FIRE(TraitGroup.ELEMENTAL),
    ICE(TraitGroup.ELEMENTAL),
    WATER(TraitGroup.ELEMENTAL),
    AIR(TraitGroup.ELEMENTAL),

    ORDER(TraitGroup.MAGICAL),
    CHAOS(TraitGroup.MAGICAL),

    KINDNESS(TraitGroup.MAGICAL),
    HAPPINESS(TraitGroup.MAGICAL),
    GENEROSITY(TraitGroup.MAGICAL),

    DARKNESS(TraitGroup.DARKNESS),
    ROT(TraitGroup.DARKNESS),
    FAMINE(TraitGroup.DARKNESS),
    POISON(TraitGroup.DARKNESS),
    BLOOD(TraitGroup.DARKNESS);

    private static final Map<String, Trait> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Trait::name, Function.identity()));
    private static final Map<Identifier, Trait> IDS = Arrays.stream(values()).collect(Collectors.toMap(Trait::getId, Function.identity()));
    private final Identifier id;
    private final Identifier sprite;
    private final TraitGroup group;

    Trait(TraitGroup group) {
        this.id = new Identifier("unicopia", name().toLowerCase());
        this.sprite = new Identifier("unicopia", "textures/gui/trait/" + name().toLowerCase() + ".png");
        this.group = group;
    }

    public Identifier getId() {
        return id;
    }

    public TraitGroup getGroup() {
        return group;
    }

    public Identifier getSprite() {
        return sprite;
    }

    public static Collection<Trait> all() {
        return IDS.values();
    }

    public static Optional<Trait> fromId(Identifier id) {
        return Optional.ofNullable(IDS.getOrDefault(id, null));
    }

    public static Optional<Trait> fromId(String name) {
        return Optional.ofNullable(Identifier.tryParse(name)).flatMap(Trait::fromId);
    }

    public static Optional<Trait> fromName(String name) {
        return Optional.ofNullable(REGISTRY.getOrDefault(name.toUpperCase(), null));
    }
}
