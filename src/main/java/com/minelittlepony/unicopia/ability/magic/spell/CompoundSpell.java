package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class CompoundSpell extends AbstractDelegatingSpell {
    private final List<Spell> spells = new ArrayList<>();

    public CompoundSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    protected Collection<Spell> getDelegates() {
        return spells;
    }

    @Override
    public Spell combineWith(Spell other) {
        if (other instanceof CompoundSpell) {
            spells.addAll(((CompoundSpell)other).spells);
        } else {
            spells.add(other);
        }
        return this;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        NbtList spells = new NbtList();
        this.spells.forEach(spell -> {
            spells.add(spell.toNBT());
        });
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        spells.clear();
        if (compound.contains("spells", NbtElement.LIST_TYPE)) {
            spells.addAll(compound.getList("spells", NbtElement.COMPOUND_TYPE).stream()
                .map(el -> SpellType.fromNBT((NbtCompound)el))
                .filter(Objects::nonNull)
                .toList());
        }
    }
}
