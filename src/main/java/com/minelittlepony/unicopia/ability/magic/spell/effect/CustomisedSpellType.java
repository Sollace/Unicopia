package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.TypedActionResult;

public record CustomisedSpellType<T extends Spell> (
        SpellType<T> type,
        SpellTraits traits
    ) implements SpellPredicate<T> {

    public boolean isEmpty() {
        return type.isEmpty();
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
    public T apply(Caster<?> caster) {
        if (isEmpty()) {
            return null;
        }

        T spell = create();
        if (spell != null && spell.apply(caster)) {
            return spell;
        }

        return null;
    }

    @Override
    public boolean test(Spell spell) {
        return type.test(spell) && spell.getTraits().equals(traits);
    }

    public ItemStack getDefaultStack() {
        return traits.applyTo(type.getDefualtStack());
    }

    public TypedActionResult<CustomisedSpellType<?>> toAction() {
        return isEmpty() ? TypedActionResult.fail(this) : TypedActionResult.pass(this);
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        type.toNbt(tag);
        tag.put("traits", traits.toNbt());
        return tag;
    }

    public static CustomisedSpellType<?> fromNBT(NbtCompound compound) {
        SpellType<?> type = SpellType.getKey(compound);
        SpellTraits traits = SpellTraits.fromNbt(compound.getCompound("traits")).orElse(type.getTraits());

        return type.withTraits(traits);
    }
}
