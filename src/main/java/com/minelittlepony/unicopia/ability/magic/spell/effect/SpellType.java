package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.CompoundSpell;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ChangelingDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.JoustingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.ThrowableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public final class SpellType<T extends Spell> implements Affine, SpellPredicate<T> {

    public static final Identifier EMPTY_ID = new Identifier("unicopia", "null");
    public static final SpellType<?> EMPTY_KEY = new SpellType<>(EMPTY_ID, Affinity.NEUTRAL, 0xFFFFFF, false, SpellTraits.EMPTY, (t, c) -> null);

    private static final Registry<SpellType<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "spells"));
    private static final Map<Affinity, Set<SpellType<?>>> BY_AFFINITY = new EnumMap<>(Affinity.class);

    public static final SpellType<CompoundSpell> COMPOUND_SPELL = register("compound", Affinity.NEUTRAL, 0, false, CompoundSpell::new);
    public static final SpellType<PlaceableSpell> PLACED_SPELL = register("placed", Affinity.NEUTRAL, 0, false, PlaceableSpell::new);
    public static final SpellType<ThrowableSpell> THROWN_SPELL = register("thrown", Affinity.NEUTRAL, 0, false, ThrowableSpell::new);

    public static final SpellType<AbstractDisguiseSpell> CHANGELING_DISGUISE = register("disguise", Affinity.BAD, 0x19E48E, false, ChangelingDisguiseSpell::new);
    public static final SpellType<JoustingSpell> RAINBOOM = register("rainboom", Affinity.GOOD, 0xBDBDF9, false, JoustingSpell::new);

    public static final SpellType<IceSpell> FROST = register("frost", Affinity.GOOD, 0xBDBDF9, true, IceSpell::new);
    public static final SpellType<ScorchSpell> SCORCH = register("scorch", Affinity.BAD, 0, true, ScorchSpell::new);
    public static final SpellType<FireSpell> FLAME = register("flame", Affinity.GOOD, 0xFF5D00, true, FireSpell::new);
    public static final SpellType<InfernoSpell> INFERNAL = register("infernal", Affinity.BAD, 0xF00F00, true, InfernoSpell::new);
    public static final SpellType<ShieldSpell> SHIELD = register("shield", Affinity.GOOD, 0x66CDAA, true, ShieldSpell::new);
    public static final SpellType<ShieldSpell> REPULSE = register("repulse", Affinity.BAD, 0x66CDAA, true, ShieldSpell::new);
    public static final SpellType<AttractiveSpell> VORTEX = register("vortex", Affinity.GOOD, 0x4CDEE7, true, AttractiveSpell::new);
    public static final SpellType<AttractiveSpell> SUFFER = register("suffer", Affinity.BAD, 0x4CDEE7, true, AttractiveSpell::new);
    public static final SpellType<NecromancySpell> NECROMANCY = register("necromancy", Affinity.BAD, 0x8A3A3A, true, NecromancySpell::new);
    public static final SpellType<SiphoningSpell> SIPHONING = register("siphoning", Affinity.GOOD, 0xe308ab, true, SiphoningSpell::new);
    public static final SpellType<SiphoningSpell> DRAINING = register("draining", Affinity.BAD, 0xe308ab, true, SiphoningSpell::new);
    public static final SpellType<RevealingSpell> REVEALING = register("reveal", Affinity.GOOD, 0x5CE81F, true, RevealingSpell::new);
    public static final SpellType<AwkwardSpell> AWKWARD = register("awkward", Affinity.GOOD, 0xE1239C, true, AwkwardSpell::new);
    public static final SpellType<TransformationSpell> TRANSFORMATION = register("transformation", Affinity.GOOD, 0x3A59AA, true, TransformationSpell::new);
    public static final SpellType<FeatherFallSpell> FEATHER_FALL = register("feather_fall", Affinity.GOOD, 0x00EEFF, true, FeatherFallSpell.DEFAULT_TRAITS, FeatherFallSpell::new);
    public static final SpellType<CatapultSpell> CATAPULT = register("catapult", Affinity.GOOD, 0x33FF00, true, CatapultSpell.DEFAULT_TRAITS, CatapultSpell::new);
    public static final SpellType<FireBoltSpell> FIRE_BOLT = register("fire_bolt", Affinity.GOOD, 0x888800, true, FireBoltSpell::new);

    private final Identifier id;
    private final Affinity affinity;
    private final int color;
    private final boolean obtainable;

    private final Factory<T> factory;

    @Nullable
    private String translationKey;

    private final CustomisedSpellType<T> traited;
    private final SpellTraits traits;

    private SpellType(Identifier id, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        this.id = id;
        this.affinity = affinity;
        this.color = color;
        this.obtainable = obtainable;
        this.factory = factory;
        this.traits = traits;
        traited = new CustomisedSpellType<>(this, traits);
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public Identifier getId() {
        return id;
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
            translationKey = Util.createTranslationKey(getAffinity().getTranslationKey(), getId());
        }
        return translationKey;
    }

    public Text getName() {
        return new TranslatableText(getTranslationKey());
    }

    public CustomisedSpellType<T> withTraits() {
        return traited;
    }

    public CustomisedSpellType<T> withTraits(SpellTraits traits) {
        return traits.isEmpty() ? withTraits() : new CustomisedSpellType<>(this, traits);
    }

    @Nullable
    public T create(SpellTraits traits) {
        try {
            return factory.create(this, traits);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public T apply(Caster<?> caster, SpellTraits traits) {
        if (isEmpty()) {
            return null;
        }

        T spell = create(traits);
        if (spell.apply(caster)) {
            return spell;
        }

        return null;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getType() == this;
    }

    public boolean isEmpty() {
        return this == EMPTY_KEY;
    }

    public static <T extends Spell> SpellType<T> register(Identifier id, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        SpellType<T> type = new SpellType<>(id, affinity, color, obtainable, traits, factory);
        byAffinity(affinity).add(type);
        Registry.register(REGISTRY, id, type);
        return type;
    }

    public static <T extends Spell> SpellType<T> register(String name, Affinity affinity, int color, boolean obtainable, Factory<T> factory) {
        return register(name, affinity, color, obtainable, SpellTraits.EMPTY, factory);
    }

    public static <T extends Spell> SpellType<T> register(String name, Affinity affinity, int color, boolean obtainable, SpellTraits traits, Factory<T> factory) {
        return register(new Identifier("unicopia", name), affinity, color, obtainable, traits, factory);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> empty() {
        return (SpellType<T>)EMPTY_KEY;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> getKey(Identifier id) {
        return (SpellType<T>)(EMPTY_ID.equals(id) ? EMPTY_KEY : REGISTRY.getOrEmpty(id).orElse(EMPTY_KEY));
    }

    public static SpellType<?> random(Random random) {
        return REGISTRY.getRandom(random);
    }

    public static Set<SpellType<?>> byAffinity(Affinity affinity) {
        return BY_AFFINITY.computeIfAbsent(affinity, a -> new HashSet<>());
    }

    @Nullable
    public static Spell fromNBT(@Nullable NbtCompound compound) {
        if (compound != null && compound.contains("effect_id")) {
            Spell effect = getKey(new Identifier(compound.getString("effect_id"))).create(SpellTraits.EMPTY);

            if (effect != null) {
                effect.fromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    public static NbtCompound toNBT(Spell effect) {
        NbtCompound compound = effect.toNBT();

        compound.putString("effect_id", effect.getType().getId().toString());

        return compound;
    }

    public interface Factory<T extends Spell> {
        T create(SpellType<T> type, SpellTraits traits);
    }
}
