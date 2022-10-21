package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public enum Trait {
    /**
     * Imparts physical strength or enhances endurance.
     * Spells with more of the strength trait hit harder and last longer.
     */
    STRENGTH(TraitGroup.NATURAL),
    /**
     * Narrows a spell to focus its energy more effectively.
     * Adding the focus trait to spells will decrease the cost of its effects whilst extending its range to more targets in cases of multi-target spells.
     */
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
    /**
     * Causes a spell to favor others over the caster.
     * Can be used to increase range and power, but to the detriment to the caster.
     *
     * Complemented by the Element of Harmony and the Element of Kindness.
     * Spells with this trait are better suited to lending aid to those in need.
     */
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

    private final Text tooltip;
    private final Text obfuscatedTooltip;

    Trait(TraitGroup group) {
        this.id = Unicopia.id(name().toLowerCase());
        this.sprite = Unicopia.id("textures/gui/trait/" + name().toLowerCase() + ".png");
        this.group = group;

        Formatting corruptionColor = getGroup().getCorruption() < -0.01F
                ? Formatting.GREEN
                : getGroup().getCorruption() > 0.25F
                    ? Formatting.RED
                    : Formatting.WHITE;

        MutableText tooltipText = Text.translatable("gui.unicopia.trait.label",
                            Text.translatable("trait." + getId().getNamespace() + "." + getId().getPath() + ".name")
                        ).formatted(Formatting.YELLOW)
                        .append(Text.translatable("gui.unicopia.trait.group", getGroup().name().toLowerCase()).formatted(Formatting.ITALIC, Formatting.GRAY))
                        .append(Text.literal("\n\n").formatted(Formatting.WHITE)
                        .append(Text.translatable("trait." + getId().getNamespace() + "." + getId().getPath() + ".description").formatted(Formatting.GRAY))
                        .append("\n")
                        .append(Text.translatable("gui.unicopia.trait.corruption", ItemStack.MODIFIER_FORMAT.format(getGroup().getCorruption())).formatted(Formatting.ITALIC, corruptionColor)));
        this.tooltip = tooltipText;
        this.obfuscatedTooltip = tooltipText.copy().formatted(Formatting.OBFUSCATED);

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

    public Text getTooltip() {
        return tooltip;
    }

    public Text getObfuscatedTooltip() {
        return obfuscatedTooltip;
    }

    public static Collection<Trait> all() {
        return IDS.values();
    }

    public static Stream<Trait> fromNbt(NbtList nbt) {
        return nbt.stream()
                .map(NbtElement::asString)
                .map(Trait::fromId)
                .flatMap(Optional::stream);
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
