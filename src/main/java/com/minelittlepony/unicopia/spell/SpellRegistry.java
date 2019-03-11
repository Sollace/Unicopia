package com.minelittlepony.unicopia.spell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SpellRegistry {

    private static final SpellRegistry instance = new SpellRegistry();

    public static SpellRegistry instance() {
        return instance;
    }

    public static boolean stackHasEnchantment(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("spell");
    }

    private final Map<String, Entry<?>> entries = new HashMap<>();

    private final Map<SpellAffinity, Set<String>> keysByAffinity = new HashMap<>();

    private SpellRegistry() {
        registerSpell(SpellShield::new);
        registerSpell(SpellCharge::new);
        registerSpell(SpellFire::new);
        registerSpell(SpellIce::new);
        registerSpell(SpellPortal::new);
        registerSpell(SpellVortex::new);
        registerSpell(SpellDisguise::new);
        registerSpell(SpellNecromancy::new);
        registerSpell(SpellAwkward::new);
        registerSpell(SpellInferno::new);
        registerSpell(SpellDrake::new);
        registerSpell(SpellReveal::new);
        registerSpell(SpellDarkness::new);
        registerSpell(SpellFlame::new);
        registerSpell(SpellSiphon::new);
        registerSpell(SpellLight::new);
        registerSpell(SpellChangelingTrap::new);
        registerSpell(SpellScorch::new);
    }

    @Nullable
    public IMagicEffect getSpellFromName(String name) {
        if (entries.containsKey(name)) {
            return entries.get(name).create();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends IMagicEffect> T copyInstance(T effect) {
        return (T)createEffectFromNBT(serializeEffectToNBT(effect));
    }

    public IMagicEffect createEffectFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("effect_id")) {
            IMagicEffect effect = getSpellFromName(compound.getString("effect_id"));

            if (effect != null) {
                effect.readFromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    public NBTTagCompound serializeEffectToNBT(IMagicEffect effect) {
        NBTTagCompound compound = effect.toNBT();

        compound.setString("effect_id", effect.getName());

        return compound;
    }

    private Optional<Entry<?>> getEntryFromStack(ItemStack stack) {
        return Optional.ofNullable(entries.get(getKeyFromStack(stack)));
    }

    @Nullable
    public IDispenceable getDispenseActionFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::dispensable).orElse(null);
    }

    @Nullable
    public IUseAction getUseActionFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::useable).orElse(null);
    }

    @Nullable
    public IHeldEffect getHeldFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::holdable).orElse(null);
    }

    @Nullable
    public IMagicEffect getSpellFrom(ItemStack stack) {
        return getSpellFromName(getKeyFromStack(stack));
    }

    public <T extends IMagicEffect> void registerSpell(Supplier<T> factory) {
        try {
            new Entry<T>(factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ItemStack disenchantStack(ItemStack stack) {
        if (stackHasEnchantment(stack)) {
            stack.getTagCompound().removeTag("spell");

            if (stack.getTagCompound().isEmpty()) {
                stack.setTagCompound(null);
            }
        }

        return stack;
    }

    public ItemStack enchantStack(ItemStack stack, ItemStack from) {
        return enchantStack(stack, getKeyFromStack(from));
    }

    public ItemStack enchantStack(ItemStack stack, String name) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString("spell", name);

        return stack;
    }

    @Nonnull
    public static String getKeyFromStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("spell")) {
            return "";
        }

        return stack.getTagCompound().getString("spell");
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

    public Set<String> getAllNames(SpellAffinity affinity) {
        return keysByAffinity.get(affinity);
    }

    @Immutable
    class Entry<T extends IMagicEffect> {
        final Supplier<T> factory;

        final int color;

        final boolean canDispense;
        final boolean canUse;
        final boolean canHold;

        final SpellAffinity affinity;

        Entry(Supplier<T> factory) throws Exception {
            T inst = factory.get();

            this.factory = factory;
            this.color = inst.getTint();
            this.canDispense = inst instanceof IDispenceable;
            this.canUse = inst instanceof IUseAction;
            this.canHold = inst instanceof IHeldEffect;
            this.affinity = inst.getAffinity();

            if (inst.isCraftable()) {
                for (SpellAffinity affinity : affinity.getImplicators()) {
                    keysByAffinity.computeIfAbsent(affinity, a -> new HashSet<>()).add(inst.getName());
                }
            }

            entries.put(inst.getName(), this);
        }

        IUseAction useable() {
            if (!canUse) {
                return null;
            }

            return (IUseAction)create();
        }

        IHeldEffect holdable() {
            if (!canHold) {
                return null;
            }

            return (IHeldEffect)create();
        }

        IDispenceable dispensable() {
            if (!canDispense) {
                return null;
            }

            return (IDispenceable)create();
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
