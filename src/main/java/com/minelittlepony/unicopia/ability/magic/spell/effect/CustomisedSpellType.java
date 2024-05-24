package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.client.TextHelper;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;

public record CustomisedSpellType<T extends Spell> (
        SpellType<T> type,
        SpellTraits traits,
        Supplier<SpellTraits> traitsDifferenceSupplier
    ) implements SpellPredicate<T> {

    public boolean isEmpty() {
        return type.isEmpty();
    }

    public boolean isStackable() {
        return type().isStackable();
    }

    public SpellTraits relativeTraits() {
        return traitsDifferenceSupplier.get();
    }

    public T create() {
        try {
            return type.getFactory().create(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public T create(NbtCompound compound) {
        T spell = create();
        if (spell != null) {
            spell.fromNBT(compound);
        }
        return spell;
    }

    @Nullable
    public T apply(Caster<?> caster, CastingMethod method) {
        if (isEmpty()) {
            return null;
        }

        T spell = create();
        if (spell != null) {
            Spell s = spell.prepareForCast(caster, method);
            if (s != null && s.apply(caster)) {
                return spell;
            }
        }

        return null;
    }

    @Override
    public boolean test(@Nullable Spell spell) {
        return spell != null && spell.getTypeAndTraits().equals(this);
    }

    public ItemStack getDefaultStack() {
        return traits.applyTo(type.getDefualtStack());
    }

    public void appendTooltip(List<Text> lines) {
        MutableText lore = Text.translatable(type().getTranslationKey() + ".lore").formatted(type().getAffinity().getColor());

        if (!InteractionManager.getInstance().getClientSpecies().canCast()) {
            lore = lore.formatted(Formatting.OBFUSCATED);
        }
        lines.addAll(TextHelper.wrap(lore, 180).toList());
        float corruption = ((int)traits().getCorruption() * 10) + type().getAffinity().getCorruption();
        List<Text> modifiers = new ArrayList<>();
        type.getTooltip().accept(this, modifiers);
        if (corruption != 0) {
            modifiers.add(EffectUtils.formatModifierChange("affinity.unicopia.corruption", corruption, true));
        }
        if (!modifiers.isEmpty()) {
            lines.add(Text.empty());
            lines.add(Text.translatable("affinity.unicopia.when_cast").formatted(Formatting.GRAY));
            lines.addAll(modifiers);
        }

    }

    public TypedActionResult<CustomisedSpellType<?>> toAction() {
        return isEmpty() ? TypedActionResult.fail(this) : TypedActionResult.pass(this);
    }

    public NbtCompound toNbt(NbtCompound compound) {
        type.toNbt(compound);
        compound.put("traits", traits.toNbt());
        return compound;
    }

    public static <T extends Spell> CustomisedSpellType<T> fromNBT(NbtCompound compound) {
        SpellType<T> type = SpellType.getKey(compound);
        return type.withTraits(SpellTraits.fromNbt(compound.getCompound("traits")).orElse(type.getTraits()));
    }
}
