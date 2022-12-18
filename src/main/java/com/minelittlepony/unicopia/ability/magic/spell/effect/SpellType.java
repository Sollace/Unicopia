package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.DispersableDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.RainboomAbilitySpell;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.ThrowableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;

public final class SpellType<T extends Spell> implements Affine, SpellPredicate<T> {
    public static final Identifier EMPTY_ID = Unicopia.id("none");
    public static final SpellType<?> EMPTY_KEY = new SpellType<>(EMPTY_ID, Affinity.NEUTRAL, 0xFFFFFF, false, SpellTraits.EMPTY, t -> null);

    public static final Registry<SpellType<?>> REGISTRY = RegistryUtils.createSimple(Unicopia.id("spells"));
    public static final Map<Affinity, Set<SpellType<?>>> BY_AFFINITY = new EnumMap<>(Affinity.class);

    public static final SpellType<PlaceableSpell> PLACED_SPELL = register("placed", Affinity.NEUTRAL, 0, false, SpellTraits.EMPTY, PlaceableSpell::new);
    public static final SpellType<ThrowableSpell> THROWN_SPELL = register("thrown", Affinity.NEUTRAL, 0, false, SpellTraits.EMPTY, ThrowableSpell::new);

    public static final SpellType<DispersableDisguiseSpell> CHANGELING_DISGUISE = register("disguise", Affinity.BAD, 0x19E48E, false, SpellTraits.EMPTY, DispersableDisguiseSpell::new);
    public static final SpellType<RainboomAbilitySpell> RAINBOOM = register("rainboom", Affinity.GOOD, 0xBDBDF9, false, SpellTraits.EMPTY, RainboomAbilitySpell::new);

    public static final SpellType<IceSpell> FROST = register("frost", Affinity.GOOD, 0xEABBFF, true, IceSpell.DEFAULT_TRAITS, IceSpell::new);
    public static final SpellType<ChillingBreathSpell> CHILLING_BREATH = register("chilling_breath", Affinity.NEUTRAL, 0xFFEAFF, true, ChillingBreathSpell.DEFAULT_TRAITS, ChillingBreathSpell::new);
    public static final SpellType<ScorchSpell> SCORCH = register("scorch", Affinity.BAD, 0xF8EC1F, true, ScorchSpell.DEFAULT_TRAITS, ScorchSpell::new);
    public static final SpellType<FireSpell> FLAME = register("flame", Affinity.GOOD, 0xFFBB99, true, FireSpell.DEFAULT_TRAITS, FireSpell::new);
    public static final SpellType<InfernoSpell> INFERNAL = register("infernal", Affinity.BAD, 0xFFAA00, true, InfernoSpell.DEFAULT_TRAITS, InfernoSpell::new);
    public static final SpellType<ShieldSpell> SHIELD = register("shield", Affinity.NEUTRAL, 0x66CDAA, true, ShieldSpell.DEFAULT_TRAITS, ShieldSpell::new);
    public static final SpellType<AreaProtectionSpell> ARCANE_PROTECTION = register("arcane_protection", Affinity.BAD, 0x99CDAA, true, AreaProtectionSpell.DEFAULT_TRAITS, AreaProtectionSpell::new);
    public static final SpellType<AttractiveSpell> VORTEX = register("vortex", Affinity.NEUTRAL, 0xFFEA88, true, AttractiveSpell.DEFAULT_TRAITS, AttractiveSpell::new);
    public static final SpellType<DarkVortexSpell> DARK_VORTEX = register("dark_vortex", Affinity.BAD, 0xA33333, true, DarkVortexSpell.DEFAULT_TRAITS, DarkVortexSpell::new);
    public static final SpellType<NecromancySpell> NECROMANCY = register("necromancy", Affinity.BAD, 0xFA3A3A, true, SpellTraits.EMPTY, NecromancySpell::new);
    public static final SpellType<SiphoningSpell> SIPHONING = register("siphoning", Affinity.NEUTRAL, 0xFFA3AA, true, SpellTraits.EMPTY, SiphoningSpell::new);
    public static final SpellType<DisperseIllusionSpell> REVEALING = register("reveal", Affinity.GOOD, 0xFFFFAF, true, SpellTraits.EMPTY, DisperseIllusionSpell::new);
    public static final SpellType<AwkwardSpell> AWKWARD = register("awkward", Affinity.GOOD, 0x3A59FF, true, SpellTraits.EMPTY, AwkwardSpell::new);
    public static final SpellType<TransformationSpell> TRANSFORMATION = register("transformation", Affinity.GOOD, 0x19E48E, true, SpellTraits.EMPTY, TransformationSpell::new);
    public static final SpellType<FeatherFallSpell> FEATHER_FALL = register("feather_fall", Affinity.GOOD, 0x00EEFF, true, FeatherFallSpell.DEFAULT_TRAITS, FeatherFallSpell::new);
    public static final SpellType<CatapultSpell> CATAPULT = register("catapult", Affinity.GOOD, 0x22FF00, true, CatapultSpell.DEFAULT_TRAITS, CatapultSpell::new);
    public static final SpellType<FireBoltSpell> FIRE_BOLT = register("fire_bolt", Affinity.GOOD, 0xFF8811, true, FireBoltSpell.DEFAULT_TRAITS, FireBoltSpell::new);
    public static final SpellType<LightSpell> LIGHT = register("light", Affinity.GOOD, 0xEEFFAA, true, LightSpell.DEFAULT_TRAITS, LightSpell::new);
    public static final SpellType<DisplacementSpell> DISPLACEMENT = register("displacement", Affinity.NEUTRAL, 0x9900FF, true, PortalSpell.DEFAULT_TRAITS, DisplacementSpell::new);
    public static final SpellType<PortalSpell> PORTAL = register("portal", Affinity.GOOD, 0x99FFFF, true, PortalSpell.DEFAULT_TRAITS, PortalSpell::new);
    public static final SpellType<MimicSpell> MIMIC = register("mimic", Affinity.GOOD, 0xFFFF00, true, SpellTraits.EMPTY, MimicSpell::new);
    public static final SpellType<MindSwapSpell> MIND_SWAP = register("mind_swap", Affinity.BAD, 0xF9FF99, true, SpellTraits.EMPTY, MindSwapSpell::new);
    public static final SpellType<HydrophobicSpell> HYDROPHOBIC = register("hydrophobic", Affinity.NEUTRAL, 0xF999FF, true, SpellTraits.EMPTY, s -> new HydrophobicSpell(s, FluidTags.WATER));

    public static void bootstrap() {}

    private final Identifier id;
    private final Affinity affinity;
    private final int color;
    private final boolean obtainable;

    private final Factory<T> factory;

    @Nullable
    private String translationKey;

    private final CustomisedSpellType<T> traited;
    private final SpellTraits traits;

    private final ItemStack defaultStack;

    private SpellType(Identifier id, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        this.id = id;
        this.affinity = affinity;
        this.color = color;
        this.obtainable = obtainable;
        this.factory = factory;
        this.traits = traits;
        traited = new CustomisedSpellType<>(this, traits);
        defaultStack = GemstoneItem.enchant(UItems.GEMSTONE.getDefaultStack(), this);
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getDefualtStack() {
        return defaultStack;
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
        return traits.isEmpty() ? withTraits() : new CustomisedSpellType<>(this, traits);
    }

    public Factory<T> getFactory() {
        return factory;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getType() == this;
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

    public static <T extends Spell> SpellType<T> register(String name, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        return register(Unicopia.id(name), affinity, color, obtainable, traits, factory);
    }

    public static <T extends Spell> SpellType<T> register(Identifier id, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        SpellType<T> type = new SpellType<>(id, affinity, color, obtainable, traits, factory);
        byAffinity(affinity).add(type);
        Registry.register(REGISTRY, id, type);
        return type;
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

    public static Set<SpellType<?>> byAffinity(Affinity affinity) {
        return BY_AFFINITY.computeIfAbsent(affinity, a -> new LinkedHashSet<>());
    }

    public interface Factory<T extends Spell> {
        T create(CustomisedSpellType<T> type);
    }
}
