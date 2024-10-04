package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ChangelingFeedingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.DispersableDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.RainboomAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell;
import com.minelittlepony.unicopia.ability.magic.spell.RageAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.ThrowableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.TimeControlAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.command.ServerCommandSource;

public final class SpellType<T extends Spell> implements Affine, SpellPredicate<T> {
    public static final Identifier EMPTY_ID = Unicopia.id("none");
    public static final SpellType<?> EMPTY_KEY = builder(t -> null).affinity(Affinity.NEUTRAL).color(0xFFFFFF).unobtainable().build(EMPTY_ID);

    public static final Registry<SpellType<?>> REGISTRY = RegistryUtils.createSimple(Unicopia.id("spells"));
    public static final RegistryKey<? extends Registry<SpellType<?>>> REGISTRY_KEY = REGISTRY.getKey();
    public static final Codec<SpellType<?>> CODEC = REGISTRY.getCodec();
    public static final PacketCodec<RegistryByteBuf, SpellType<?>> PACKET_CODEC = PacketCodecs.registryValue(REGISTRY_KEY);

    private static final DynamicCommandExceptionType UNKNOWN_SPELL_TYPE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("spell_type.unknown", id));

    public static final SpellType<PlacementControlSpell> PLACE_CONTROL_SPELL = register("place_controller", SpellType.<PlacementControlSpell>builder(PlacementControlSpell::new).affinity(Affinity.NEUTRAL).unobtainable().stackable().shape(GemstoneItem.Shape.DONUT));
    public static final SpellType<ThrowableSpell> THROWN_SPELL = register("thrown", SpellType.<ThrowableSpell>builder(ThrowableSpell::new).affinity(Affinity.NEUTRAL).unobtainable().shape(GemstoneItem.Shape.DONUT));

    public static final SpellType<DispersableDisguiseSpell> CHANGELING_DISGUISE = register("disguise", builder(DispersableDisguiseSpell::new).affinity(Affinity.BAD).color(0x19E48E).unobtainable().shape(GemstoneItem.Shape.ARROW));
    public static final SpellType<ChangelingFeedingSpell> FEED = register("feed", SpellType.<ChangelingFeedingSpell>builder(ChangelingFeedingSpell::new).affinity(Affinity.BAD).color(0xBDBDF9).unobtainable().shape(GemstoneItem.Shape.ARROW));
    public static final SpellType<RainboomAbilitySpell> RAINBOOM = register("rainboom", builder(RainboomAbilitySpell::new).color(0xBDBDF9).unobtainable().shape(GemstoneItem.Shape.ROCKET));
    public static final SpellType<RageAbilitySpell> RAGE = register("rage", builder(RageAbilitySpell::new).color(0xBDBDF9).unobtainable().shape(GemstoneItem.Shape.FLAME));
    public static final SpellType<TimeControlAbilitySpell> TIME_CONTROL = register("time_control", builder(TimeControlAbilitySpell::new).color(0xBDBDF9).unobtainable().shape(GemstoneItem.Shape.STAR));

    public static final SpellType<IceSpell> FROST = register("frost", builder(IceSpell::new).color(0xEABBFF).shape(GemstoneItem.Shape.TRIANGLE).traits(IceSpell.DEFAULT_TRAITS).tooltip(IceSpell.TOOLTIP));
    public static final SpellType<ChillingBreathSpell> CHILLING_BREATH = register("chilling_breath", builder(ChillingBreathSpell::new).affinity(Affinity.NEUTRAL).color(0xFFEAFF).shape(GemstoneItem.Shape.TRIANGLE).traits(ChillingBreathSpell.DEFAULT_TRAITS));
    public static final SpellType<ScorchSpell> SCORCH = register("scorch", builder(ScorchSpell::new).affinity(Affinity.BAD).color(0xF8EC1F).stackable().shape(GemstoneItem.Shape.FLAME).traits(ScorchSpell.DEFAULT_TRAITS).tooltip(AbstractAreaEffectSpell.TOOLTIP));
    public static final SpellType<FireSpell> FLAME = register("flame", builder(FireSpell::new).color(0xFFBB99).shape(GemstoneItem.Shape.FLAME).traits(FireSpell.DEFAULT_TRAITS).tooltip(AbstractAreaEffectSpell.TOOLTIP));
    public static final SpellType<InfernoSpell> INFERNAL = register("infernal", builder(InfernoSpell::new).affinity(Affinity.BAD).color(0xFFAA00).shape(GemstoneItem.Shape.FLAME).traits(InfernoSpell.DEFAULT_TRAITS).tooltip(AbstractAreaEffectSpell.TOOLTIP));
    public static final SpellType<ShieldSpell> SHIELD = register("shield", builder(ShieldSpell::new).affinity(Affinity.NEUTRAL).color(0x66CDAA).shape(GemstoneItem.Shape.SHIELD).traits(ShieldSpell.DEFAULT_TRAITS).tooltip(ShieldSpell.TOOLTIP));
    public static final SpellType<AreaProtectionSpell> ARCANE_PROTECTION = register("arcane_protection", builder(AreaProtectionSpell::new).affinity(Affinity.BAD).color(0x99CDAA).shape(GemstoneItem.Shape.SHIELD).traits(AreaProtectionSpell.DEFAULT_TRAITS).tooltip(AreaProtectionSpell.TOOLTIP));
    public static final SpellType<AttractiveSpell> VORTEX = register("vortex", builder(AttractiveSpell::new).affinity(Affinity.NEUTRAL).color(0xFFEA88).shape(GemstoneItem.Shape.VORTEX).traits(AttractiveSpell.DEFAULT_TRAITS).tooltip(AttractiveSpell.TOOLTIP));
    public static final SpellType<DarkVortexSpell> DARK_VORTEX = register("dark_vortex", builder(DarkVortexSpell::new).affinity(Affinity.BAD).color(0xA33333).stackable().shape(GemstoneItem.Shape.VORTEX).traits(DarkVortexSpell.DEFAULT_TRAITS));
    public static final SpellType<NecromancySpell> NECROMANCY = register("necromancy", builder(NecromancySpell::new).affinity(Affinity.BAD).color(0xFA3A3A).shape(GemstoneItem.Shape.SKULL).tooltip(NecromancySpell.TOOLTIP));
    public static final SpellType<SiphoningSpell> SIPHONING = register("siphoning", builder(SiphoningSpell::new).affinity(Affinity.NEUTRAL).color(0xFFA3AA).shape(GemstoneItem.Shape.LAMBDA).tooltip(AbstractAreaEffectSpell.TOOLTIP));
    public static final SpellType<DisperseIllusionSpell> REVEALING = register("reveal", builder(DisperseIllusionSpell::new).color(0xFFFFAF).shape(GemstoneItem.Shape.CROSS).tooltip(DisperseIllusionSpell.TOOLTIP));
    public static final SpellType<AwkwardSpell> AWKWARD = register("awkward", builder(AwkwardSpell::new).affinity(Affinity.NEUTRAL).color(0x3A59FF).shape(GemstoneItem.Shape.ICE));
    public static final SpellType<TransformationSpell> TRANSFORMATION = register("transformation", builder(TransformationSpell::new).color(0x19E48E).shape(GemstoneItem.Shape.BRUSH));
    public static final SpellType<FeatherFallSpell> FEATHER_FALL = register("feather_fall", builder(FeatherFallSpell::new).color(0x00EEFF).shape(GemstoneItem.Shape.LAMBDA).traits(FeatherFallSpell.DEFAULT_TRAITS).tooltip(FeatherFallSpell.TOOLTIP));
    public static final SpellType<CatapultSpell> CATAPULT = register("catapult", builder(CatapultSpell::new).color(0x22FF00).shape(GemstoneItem.Shape.ROCKET).traits(CatapultSpell.DEFAULT_TRAITS).tooltip(CatapultSpell.TOOLTIP));
    public static final SpellType<FireBoltSpell> FIRE_BOLT = register("fire_bolt", builder(FireBoltSpell::new).color(0xFF8811).shape(GemstoneItem.Shape.FLAME).traits(FireBoltSpell.DEFAULT_TRAITS).tooltip(FireBoltSpell.TOOLTIP));
    public static final SpellType<LightSpell> LIGHT = register("light", builder(LightSpell::new).color(0xEEFFAA).shape(GemstoneItem.Shape.STAR).traits(LightSpell.DEFAULT_TRAITS).tooltip(LightSpell.TOOLTIP));
    public static final SpellType<DisplacementSpell> DISPLACEMENT = register("displacement", builder(DisplacementSpell::new).affinity(Affinity.NEUTRAL).color(0x9900FF).stackable().shape(GemstoneItem.Shape.BRUSH).traits(PortalSpell.DEFAULT_TRAITS).tooltip(DisplacementSpell.TOOLTIP));
    public static final SpellType<PortalSpell> PORTAL = register("portal", builder(PortalSpell::new).color(0x99FFFF).shape(GemstoneItem.Shape.RING).traits(PortalSpell.DEFAULT_TRAITS));
    public static final SpellType<MimicSpell> MIMIC = register("mimic", builder(MimicSpell::new).color(0xFFFF00).shape(GemstoneItem.Shape.ARROW).tooltip(MimicSpell.TOOLTIP));
    public static final SpellType<MindSwapSpell> MIND_SWAP = register("mind_swap", builder(MindSwapSpell::new).affinity(Affinity.BAD).color(0xF9FF99).shape(GemstoneItem.Shape.WAVE).tooltip(MimicSpell.TOOLTIP));
    public static final SpellType<HydrophobicSpell> HYDROPHOBIC = register("hydrophobic", SpellType.<HydrophobicSpell>builder(s -> new HydrophobicSpell(s, FluidTags.WATER)).affinity(Affinity.NEUTRAL).color(0xF999FF).stackable().shape(GemstoneItem.Shape.ROCKET).tooltip(HydrophobicSpell.TOOLTIP));
    public static final SpellType<BubbleSpell> BUBBLE = register("bubble", builder(BubbleSpell::new).affinity(Affinity.NEUTRAL).color(0xF999FF).shape(GemstoneItem.Shape.DONUT).traits(BubbleSpell.DEFAULT_TRAITS).tooltip(BubbleSpell.TOOLTIP));
    public static final SpellType<DispellEvilSpell> DISPEL_EVIL = register("dispel_evil", builder(DispellEvilSpell::new).color(0x00FF00).shape(GemstoneItem.Shape.CROSS).traits(DispellEvilSpell.DEFAULT_TRAITS).tooltip(DispellEvilSpell.TOOLTIP));

    public static void bootstrap() {}

    private final Identifier id;
    private final Affinity affinity;
    private final int color;
    private final boolean obtainable;
    private final boolean stackable;
    private final GemstoneItem.Shape shape;

    private final Factory<T> factory;

    @Nullable
    private String translationKey;

    private final CustomisedSpellType<T> traited;
    private final SpellTraits traits;

    private final Supplier<ItemStack> defaultStack;

    private final TooltipFactory tooltipFunction;

    private SpellType(Identifier id, Affinity affinity, int color, boolean obtainable, boolean stackable, GemstoneItem.Shape shape, SpellTraits traits, TooltipFactory tooltipFunction, Factory<T> factory) {
        this.id = id;
        this.affinity = affinity;
        this.color = color;
        this.obtainable = obtainable;
        this.shape = shape;
        this.tooltipFunction = tooltipFunction;
        this.factory = factory;
        this.traits = traits;
        this.stackable = stackable;
        traited = new CustomisedSpellType<>(this, traits, SpellTraits::empty);
        defaultStack = Suppliers.memoize(() -> EnchantableItem.enchant(UItems.GEMSTONE.getDefaultStack(), this));
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public boolean isStackable() {
        return stackable;
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getDefualtStack() {
        return defaultStack.get();
    }

    /**
     * Gets the tint for this spell when applied to a gem.
     */
    public int getColor() {
        return color;
    }

    @Override
    public Affinity getAffinity() {
        return affinity;
    }

    public GemstoneItem.Shape getGemShape() {
        return shape;
    }

    public SpellTraits getTraits() {
        return traits;
    }

    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.createTranslationKey("spell", getId());
        }
        return translationKey;
    }

    public Text getName() {
        return Text.translatable(getTranslationKey());
    }

    public CustomisedSpellType<T> withTraits() {
        return traited;
    }

    public CustomisedSpellType<T> withTraits(SpellTraits traits) {
        return traits.isEmpty() ? withTraits() : new CustomisedSpellType<>(this, traits, Suppliers.memoize(() -> traits.map((trait, value) -> value - getTraits().get(trait))));
    }

    public Factory<T> getFactory() {
        return factory;
    }

    public TooltipFactory getTooltip() {
        return tooltipFunction;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getTypeAndTraits().type() == this;
    }

    public void toNbt(NbtCompound tag) {
        tag.putString("effect_id", getId().toString());
    }

    public boolean isEmpty() {
        return this == EMPTY_KEY;
    }

    @Override
    public String toString() {
        return "SpellType[" + getTranslationKey() + "]";
    }

    public static <T extends Spell> SpellType<T> register(String name, Builder<T> builder) {
        return register(Unicopia.id(name), builder);
    }

    public static <T extends Spell> SpellType<T> register(Identifier id, Builder<T> builder) {
        return Registry.register(REGISTRY, id, builder.build(id));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> empty() {
        return (SpellType<T>)EMPTY_KEY;
    }

    public static <T extends Spell> SpellType<T> getKey(NbtCompound tag) {
        return getKey(Identifier.tryParse(tag.getString("effect_id")));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> getKey(@Nullable Identifier id) {
        return (SpellType<T>)REGISTRY.getOrEmpty(id).orElse(EMPTY_KEY);
    }

    public static SpellType<?> fromArgument(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier id = context.getArgument(name, RegistryKey.class).getValue();
        return REGISTRY.getOrEmpty(id).orElseThrow(() -> UNKNOWN_SPELL_TYPE_EXCEPTION.create(id));
    }

    public interface Factory<T extends Spell> {
        T create(CustomisedSpellType<T> type);
    }

    public static <T extends Spell> Builder<T> builder(Factory<T> factory) {
        return new Builder<>(factory);
    }

    static class Builder<T extends Spell> {
        private final Factory<T> factory;
        private Affinity affinity = Affinity.GOOD;
        private int color;
        private boolean obtainable = true;
        private boolean stackable = false;
        private GemstoneItem.Shape shape = GemstoneItem.Shape.ROUND;
        private SpellTraits traits = SpellTraits.EMPTY;
        private TooltipFactory tooltipFunction = TooltipFactory.EMPTY;

        Builder(Factory<T> factory) {
            this.factory = factory;
        }

        public Builder<T> affinity(Affinity affinity) {
            this.affinity = affinity;
            return this;
        }

        public Builder<T> color(int color) {
            this.color = color;
            return this;
        }

        public Builder<T> unobtainable() {
            obtainable = false;
            return this;
        }

        public Builder<T> stackable() {
            stackable = true;
            return this;
        }

        public Builder<T> shape(GemstoneItem.Shape shape) {
            this.shape = shape;
            return this;
        }

        public Builder<T> traits(SpellTraits traits) {
            this.traits = traits;
            return this;
        }

        public Builder<T> tooltip(TooltipFactory tooltipFunction) {
            this.tooltipFunction = tooltipFunction;
            return this;
        }

        public SpellType<T> build(Identifier id) {
            return new SpellType<>(id, affinity, color, obtainable, stackable, shape, traits, tooltipFunction, factory);
        }
    }
}
