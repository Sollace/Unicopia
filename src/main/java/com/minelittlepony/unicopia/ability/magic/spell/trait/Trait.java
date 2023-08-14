package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.command.CommandArgumentEnum;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum Trait implements CommandArgumentEnum<Trait> {
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
    private final List<Text> tooltipLines;

    Trait(TraitGroup group) {
        this.id = Unicopia.id(name().toLowerCase(Locale.ROOT));
        this.sprite = Unicopia.id("textures/gui/trait/" + name().toLowerCase(Locale.ROOT) + ".png");
        this.group = group;

        Formatting corruptionColor = getGroup().getCorruption() < -0.01F
                ? Formatting.GREEN
                : getGroup().getCorruption() > 0.25F
                    ? Formatting.RED
                    : Formatting.WHITE;

        tooltipLines = List.of(
            Text.translatable("gui.unicopia.trait.group", getGroup().name().toLowerCase(Locale.ROOT)).formatted(Formatting.ITALIC, Formatting.GRAY),
            Text.empty(),
            Text.empty(),
            Text.translatable("trait." + getId().getNamespace() + "." + getId().getPath() + ".description").formatted(Formatting.GRAY),
            Text.empty(),
            Text.translatable("gui.unicopia.trait.corruption", ItemStack.MODIFIER_FORMAT.format(getGroup().getCorruption())).formatted(Formatting.ITALIC, corruptionColor)
        );

        MutableText tooltipText = getName().copy();
        tooltipLines.forEach(line -> tooltipText.append(line).append("\n"));
        this.tooltip = tooltipText;
        this.obfuscatedTooltip = tooltipText.copy().formatted(Formatting.OBFUSCATED);

    }

    public Identifier getId() {
        return id;
    }

    @Override
    public String asString() {
        return name();
    }

    public TraitGroup getGroup() {
        return group;
    }

    public Identifier getSprite() {
        return sprite;
    }

    public Text getName() {
        return Text.translatable("gui.unicopia.trait.label", getShortName()).formatted(Formatting.YELLOW);
    }

    public Text getShortName() {
        return Text.translatable("trait." + getId().getNamespace() + "." + getId().getPath() + ".name");
    }

    public List<Text> getTooltipLines() {
        return tooltipLines;
    }

    public Text getTooltip() {
        return tooltip;
    }

    public Text getObfuscatedTooltip() {
        return obfuscatedTooltip;
    }

    public List<Item> getItems() {
        return SpellTraits.ITEMS.getOrDefault(this, List.of());
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

    public static EnumArgumentType<Trait> argument() {
        return new ArgumentType();
    }

    public static final class ArgumentType extends EnumArgumentType<Trait> {
        @SuppressWarnings("deprecation")
        static final Codec<Trait> CODEC = StringIdentifiable.createCodec(Trait::values);

        protected ArgumentType() {
            super(CODEC, Trait::values);
        }
    }
}
