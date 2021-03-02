package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SpellType<T extends Spell> implements Affine {

    public static final Identifier EMPTY_ID = new Identifier("unicopia", "null");
    public static final SpellType<?> EMPTY_KEY = new SpellType<>(EMPTY_ID, Affinity.NEUTRAL, 0xFFFFFF, false, (t, a) -> null);

    private static final Map<Identifier, SpellType<?>> REGISTRY = new HashMap<>();
    private static final Map<Affinity, Set<SpellType<?>>> BY_AFFINITY = new EnumMap<>(Affinity.class);

    public static final SpellType<IceSpell> FROST = register("frost", Affinity.GOOD, 0xBDBDF9, true, IceSpell::new);
    public static final SpellType<ScorchSpell> SCORCH = register("scorch", Affinity.BAD, 0, true, ScorchSpell::new);
    public static final SpellType<FireSpell> FLAME = register("flame", Affinity.GOOD, 0xFF5D00, true, FireSpell::new);
    public static final SpellType<InfernoSpell> INFERNAL = register("infernal", Affinity.BAD, 0xF00F00, true, InfernoSpell::new);
    public static final SpellType<ShieldSpell> SHIELD = register("shield", Affinity.NEUTRAL, 0x66CDAA, true, ShieldSpell::new);
    public static final SpellType<AttractiveSpell> VORTEX = register("vortex", Affinity.NEUTRAL, 0x4CDEE7, true, AttractiveSpell::new);
    public static final SpellType<NecromancySpell> NECROMANCY = register("necromancy", Affinity.BAD, 0x3A3A3A, true, NecromancySpell::new);
    public static final SpellType<SiphoningSpell> SIPHONING = register("siphoning", Affinity.NEUTRAL, 0xe308ab, true, SiphoningSpell::new);
    public static final SpellType<DisguiseSpell> DISGUISE = register("disguise", Affinity.BAD, 0x19E48E, false, DisguiseSpell::new);
    public static final SpellType<RevealingSpell> REVEALING = register("reveal", Affinity.GOOD, 0x5CE81F, true, RevealingSpell::new);
    public static final SpellType<JoustingSpell> JOUSTING = register("joust", Affinity.GOOD, 0xBDBDF9, false, JoustingSpell::new);
    public static final SpellType<AwkwardSpell> AWKWARD = register("awkward", Affinity.GOOD, 0xE1239C, true, AwkwardSpell::new);

    private final Identifier id;
    private final Affinity affinity;
    private final int color;
    private final boolean obtainable;

    private final Factory<T> factory;

    private final Map<Affinity, String> translationKeys = new EnumMap<>(Affinity.class);

    SpellType(Identifier id, Affinity affinity, int color, boolean obtainable, Factory<T> factory) {
        this.id = id;
        this.affinity = affinity;
        this.color = color;
        this.obtainable = obtainable;
        this.factory = factory;
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

    public String getTranslationKey(Affinity affinity) {
        return translationKeys.computeIfAbsent(affinity, a -> Util.createTranslationKey(a.getTranslationKey(), getId()));
    }

    public Text getName(Affinity affinity) {
        return new TranslatableText(getTranslationKey(affinity));
    }

    @Nullable
    public T create() {
        return create(getAffinity());
    }

    @Nullable
    public T create(Affinity affinity) {
        try {
            return factory.create(this, affinity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Spell> SpellType<T> register(Identifier id, Affinity affinity, int color, boolean obtainable, Factory<T> factory) {
        SpellType<T> type = new SpellType<>(id, affinity, color, obtainable, factory);

        for (Affinity i : affinity.getImplicators()) {
            byAffinity(i).add(type);
        }

        REGISTRY.put(id, type);
        return type;
    }

    public static <T extends Spell> SpellType<T> register(String name, Affinity affinity, int color, boolean obtainable, Factory<T> factory) {
        return register(new Identifier("unicopia", name), affinity, color, obtainable, factory);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> SpellType<T> getKey(Identifier id) {
        return (SpellType<T>)REGISTRY.getOrDefault(id, EMPTY_KEY);
    }

    public static Set<SpellType<?>> byAffinity(Affinity affinity) {
        return BY_AFFINITY.computeIfAbsent(affinity, a -> new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Spell> T copy(T effect) {
        return (T)fromNBT(toNBT(effect));
    }

    @Nullable
    public static Spell fromNBT(CompoundTag compound) {
        if (compound.contains("effect_id")) {
            Spell effect = getKey(new Identifier(compound.getString("effect_id"))).create();

            if (effect != null) {
                effect.fromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    public static CompoundTag toNBT(Spell effect) {
        CompoundTag compound = effect.toNBT();

        compound.putString("effect_id", effect.getType().getId().toString());

        return compound;
    }

    public interface Factory<T extends Spell> {
        T create(SpellType<T> type, Affinity affinity);
    }
}
