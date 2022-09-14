package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class CompoundSpell extends AbstractDelegatingSpell {
    private final List<Spell> spells = new ArrayList<>();

    public CompoundSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Collection<Spell> getDelegates() {
        return spells;
    }

    @Override
    public Spell combineWith(Spell other) {
        if (other instanceof AbstractDelegatingSpell) {
            spells.addAll(((AbstractDelegatingSpell)other).getDelegates());
        } else {
            spells.add(other);
        }
        return this;
    }

    @Override
    protected void loadDelegates(NbtCompound compound) {
        spells.clear();
        if (compound.contains("spells", NbtElement.LIST_TYPE)) {
            spells.addAll(Spell.SERIALIZER.readAll(compound.getList("spells", NbtElement.COMPOUND_TYPE)).toList());
        }
    }

    @Override
    protected void saveDelegates(NbtCompound compound) {
        compound.put("spells", Spell.SERIALIZER.writeAll(spells));
    }
}
