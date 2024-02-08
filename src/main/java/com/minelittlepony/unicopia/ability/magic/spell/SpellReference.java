package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;

public final class SpellReference<T extends Spell> implements NbtSerialisable {
    @Nullable
    private transient T spell;
    private int nbtHash;

    @Nullable
    public T get() {
        return spell == null || spell.isDead() ? null : spell;
    }

    public void set(T spell) {
        set(spell, null);
    }

    public boolean hasDirtySpell() {
        return spell != null && spell.isDirty();
    }

    public boolean set(T spell, @Nullable Caster<?> owner) {
        spell = spell == null || spell.isDead() ? null : spell;
        if (spell == this.spell) {
            return false;
        }
        T oldValue = this.spell;
        this.spell = spell;
        nbtHash = 0;
        if (owner != null && oldValue != null && (spell == null || !oldValue.getUuid().equals(spell.getUuid()))) {
            oldValue.destroy(owner);
        }
        return true;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        if (spell != null && !spell.isDead()) {
            spell.toNBT(compound);
            spell.getType().toNbt(compound);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        final int hash = compound.hashCode();
        if (nbtHash == hash) {
            return;
        }
        nbtHash = hash;

        if (spell == null || !Objects.equals(Spell.getUuid(compound), spell.getUuid())) {
            spell = Spell.readNbt(compound);
        } else {
            spell.fromNBT(compound);
        }
    }
}
