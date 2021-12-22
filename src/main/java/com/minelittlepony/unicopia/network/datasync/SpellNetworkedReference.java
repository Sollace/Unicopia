package com.minelittlepony.unicopia.network.datasync;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;

import net.minecraft.nbt.NbtCompound;

public class SpellNetworkedReference<T extends Spell> implements NetworkedReference<T> {
    private Optional<T> currentValue = Optional.empty();

    @Nullable
    private NbtCompound lastValue;

    private final Caster<?> owner;

    private boolean dirty;

    public SpellNetworkedReference(Caster<?> owner) {
        this.owner = owner;
    }

    @Override
    public Optional<T> getReference() {
        return currentValue.filter(s -> !s.isDead());
    }

    private boolean mustDelete(@Nullable NbtCompound comp) {
        return comp == null || !comp.contains("effect_id") || !comp.contains("uuid");
    }

    private boolean mustReplace(NbtCompound comp) {
        return currentValue.isEmpty() || !currentValue.get().getUuid().equals(comp.getUuid("uuid"));
    }

    private boolean mustUpdate(NbtCompound comp) {
        if (owner.isClient() && !Objects.equals(lastValue, comp)) {
            lastValue = comp;
            return true;
        }
        return false;
    }

    private boolean mustSend() {
        return currentValue.filter(Spell::isDirty).isPresent();
    }

    @Override
    public void updateReference(@Nullable T newValue) {
        newValue = newValue == null || newValue.isDead() ? null : newValue;

        @Nullable
        T oldValue = currentValue.orElse(null);
        if (oldValue != newValue) {
            dirty = true;
            currentValue = Optional.ofNullable(newValue);

            if (oldValue != null && (newValue == null || !oldValue.getUuid().equals(newValue.getUuid()))) {
                oldValue.setDead();
                oldValue.onDestroyed(owner);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fromNbt(NbtCompound comp) {
        dirty = false;

        if (mustDelete(comp)) {
            updateReference(null);
            return false;
        }

        if (mustReplace(comp)) {
            updateReference((T)SpellType.fromNBT(comp));
            return false;
        }

        if (mustUpdate(comp)) {
            currentValue.ifPresent(s -> s.fromNBT(comp));
            return false;
        }

        if (mustSend()) {
            updateReference(getReference().orElse(null));
            return true;
        }

        return false;
    }

    @Override
    public NbtCompound toNbt() {
        dirty = false;
        return getReference().map(SpellType::toNBT).orElseGet(NbtCompound::new);
    }

    @Override
    public boolean isDirty() {
        return dirty || mustSend();
    }
}
