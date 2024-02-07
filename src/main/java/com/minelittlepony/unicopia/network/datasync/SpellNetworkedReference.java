package com.minelittlepony.unicopia.network.datasync;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellReference;

import net.minecraft.nbt.NbtCompound;

public class SpellNetworkedReference<T extends Spell> implements NetworkedReference<T> {
    private final SpellReference<T> currentValue = new SpellReference<>();
    private final Caster<?> owner;
    private boolean dirty;

    public SpellNetworkedReference(Caster<?> owner) {
        this.owner = owner;
    }

    @Override
    public Optional<T> getReference() {
        return Optional.ofNullable(currentValue.get());
    }

    @Override
    public void updateReference(@Nullable T newValue) {
        dirty |= currentValue.set(newValue, owner);
    }

    @Override
    public boolean fromNbt(NbtCompound comp) {
        dirty = false;
        currentValue.fromNBT(comp);
        return isDirty();
    }

    @Override
    public NbtCompound toNbt() {
        dirty = false;
        return currentValue.toNBT();
    }

    @Override
    public boolean isDirty() {
        return !owner.isClient() && (dirty || currentValue.hasDirtySpell());
    }
}
