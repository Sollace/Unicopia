package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Spell;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class SpellRegistry {

    private static final SpellRegistry instance = new SpellRegistry();

    public static SpellRegistry instance() {
        return instance;
    }

    private final Map<String, Entry<?>> entries = new HashMap<>();

    private final Map<Affinity, Set<String>> keysByAffinity = new HashMap<>();

    private SpellRegistry() {
        register(ShieldSpell::new);
        register(FireSpell::new);
        register(AttractiveSpell::new);
        register(NecromancySpell::new);
        register(SiphoningSpell::new);
        register(IceSpell::new);
        register(AwkwardSpell::new);
        register(InfernoSpell::new);
        register(RevealingSpell::new);
        register(ScorchSpell::new);
        register(DisguiseSpell::new);
        register(JoustingSpell::new);
    }

    @Nullable
    public Spell getSpellFromName(String name) {
        if (entries.containsKey(name)) {
            return entries.get(name).create();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Spell> T copyInstance(T effect) {
        return (T)createEffectFromNBT(toNBT(effect));
    }

    @Nullable
    public Spell createEffectFromNBT(CompoundTag compound) {
        if (compound.contains("effect_id")) {
            Spell effect = getSpellFromName(compound.getString("effect_id"));

            if (effect != null) {
                effect.fromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    public static CompoundTag toNBT(Spell effect) {
        CompoundTag compound = effect.toNBT();

        compound.putString("effect_id", effect.getName());

        return compound;
    }

    @Nullable
    public Spell getSpellFrom(ItemStack stack) {
        return getSpellFromName(getKeyFromStack(stack));
    }

    public <T extends Spell> void register(Supplier<T> factory) {
        try {
            new Entry<>(factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ItemStack disenchantStack(ItemStack stack) {
        if (stackHasEnchantment(stack)) {
            stack.getTag().remove("spell");

            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
        }

        return stack;
    }

    public ItemStack enchantStack(ItemStack stack, ItemStack from) {
        return enchantStack(stack, getKeyFromStack(from));
    }

    public ItemStack enchantStack(ItemStack stack, String name) {
        stack.getOrCreateTag().putString("spell", name);

        return stack;
    }

    public static boolean stackHasEnchantment(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().contains("spell");
    }

    @Nonnull
    public static String getKeyFromStack(ItemStack stack) {
        if (stackHasEnchantment(stack)) {
            return stack.getTag().getString("spell");
        }

        return "";
    }

    public int getSpellTintFromStack(ItemStack stack) {
        return getSpellTint(getKeyFromStack(stack));
    }

    public int getSpellTint(String key) {
        if (entries.containsKey(key)) {
            return entries.get(key).color;
        }

        return 0xffffff;
    }

    public Set<String> getAllNames(Affinity affinity) {
        return keysByAffinity.get(affinity);
    }

    @Immutable
    class Entry<T extends Spell> {
        final Supplier<T> factory;

        final int color;

        final Affinity affinity;

        Entry(Supplier<T> factory) throws Exception {
            T inst = factory.get();

            this.factory = factory;
            this.color = inst.getTint();
            this.affinity = inst.getAffinity();

            for (Affinity affinity : affinity.getImplicators()) {
                keysByAffinity.computeIfAbsent(affinity, a -> new HashSet<>()).add(inst.getName());
            }

            entries.put(inst.getName(), this);
        }

        T create() {
            try {
                return factory.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
