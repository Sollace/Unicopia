package com.minelittlepony.unicopia.magic.spell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.DispenceableMagicEffect;
import com.minelittlepony.unicopia.magic.HeldMagicEffect;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.Useable;

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
        register(ChargingSpell::new);
        register(IceSpell::new);
        register(PortalSpell::new);
        register(AwkwardSpell::new);
        register(InfernoSpell::new);
        register(FaithfulAssistantSpell::new);
        register(RevealingSpell::new);
        register(DarknessSpell::new);
        register(FlameSpell::new);
        register(GlowingSpell::new);
        register(ChangelingTrapSpell::new);
        register(ScorchSpell::new);
        register(DisguiseSpell::new);
    }

    @Nullable
    public MagicEffect getSpellFromName(String name) {
        if (entries.containsKey(name)) {
            return entries.get(name).create();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends MagicEffect> T copyInstance(T effect) {
        return (T)createEffectFromNBT(serializeEffectToNBT(effect));
    }

    @Nullable
    public MagicEffect createEffectFromNBT(CompoundTag compound) {
        if (compound.containsKey("effect_id")) {
            MagicEffect effect = getSpellFromName(compound.getString("effect_id"));

            if (effect != null) {
                effect.fromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    public CompoundTag serializeEffectToNBT(MagicEffect effect) {
        CompoundTag compound = effect.toNBT();

        compound.putString("effect_id", effect.getName());

        return compound;
    }

    private Optional<Entry<?>> getEntryFromStack(ItemStack stack) {
        return Optional.ofNullable(entries.get(getKeyFromStack(stack)));
    }

    @Nullable
    public DispenceableMagicEffect getDispenseActionFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::dispensable).orElse(null);
    }

    @Nullable
    public Useable getUseActionFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::useable).orElse(null);
    }

    @Nullable
    public HeldMagicEffect getHeldFrom(ItemStack stack) {
        return getEntryFromStack(stack).map(Entry::holdable).orElse(null);
    }

    @Nullable
    public MagicEffect getSpellFrom(ItemStack stack) {
        return getSpellFromName(getKeyFromStack(stack));
    }

    public <T extends MagicEffect> void register(Supplier<T> factory) {
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
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().containsKey("spell");
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
    class Entry<T extends MagicEffect> {
        final Supplier<T> factory;

        final int color;

        final boolean canDispense;
        final boolean canUse;
        final boolean canHold;

        final Affinity affinity;

        Entry(Supplier<T> factory) throws Exception {
            T inst = factory.get();

            this.factory = factory;
            this.color = inst.getTint();
            this.canDispense = inst instanceof DispenceableMagicEffect;
            this.canUse = inst instanceof Useable;
            this.canHold = inst instanceof HeldMagicEffect;
            this.affinity = inst.getAffinity();

            if (inst.isCraftable()) {
                for (Affinity affinity : affinity.getImplicators()) {
                    keysByAffinity.computeIfAbsent(affinity, a -> new HashSet<>()).add(inst.getName());
                }
            }

            entries.put(inst.getName(), this);
        }

        Useable useable() {
            if (!canUse) {
                return null;
            }

            return (Useable)create();
        }

        HeldMagicEffect holdable() {
            if (!canHold) {
                return null;
            }

            return (HeldMagicEffect)create();
        }

        DispenceableMagicEffect dispensable() {
            if (!canDispense) {
                return null;
            }

            return (DispenceableMagicEffect)create();
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
