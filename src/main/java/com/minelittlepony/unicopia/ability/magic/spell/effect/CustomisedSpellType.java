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
        return type.create(traits);
    }

    @Nullable
    public T apply(Caster<?> caster) {
        return type.apply(caster, traits);
    }

    @Override
    public boolean test(Spell spell) {
        return type.test(spell);
    }

    public ItemStack getDefaultStack() {
        return type.getDefualtStack();
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
