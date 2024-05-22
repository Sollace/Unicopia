package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.*;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import net.minecraft.nbt.NbtCompound;

public abstract class AbstractDelegatingSpell implements Spell {
    private boolean dirty;
    private boolean hidden;
    private boolean destroyed;

    private UUID uuid = UUID.randomUUID();

    private final CustomisedSpellType<?> type;
    protected final SpellReference<Spell> delegate = new SpellReference<>();

    public AbstractDelegatingSpell(CustomisedSpellType<?> type) {
        this.type = type;
    }

    public final Spell getDelegate() {
        return delegate.get();
    }

    @Override
    public boolean equalsOrContains(UUID id) {
        return Spell.super.equalsOrContains(id) || delegate.equalsOrContains(id);
    }

    @Override
    public <T extends Spell> Stream<T> findMatches(SpellPredicate<T> predicate) {
        return Stream.concat(Spell.super.findMatches(predicate), delegate.findMatches(predicate));
    }

    @Override
    public CustomisedSpellType<?> getTypeAndTraits() {
        return type;
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public void setDead() {
        if (delegate.get() instanceof Spell p) {
            p.setDead();
        }
    }

    @Override
    public void tickDying(Caster<?> caster) {
        if (delegate.get() instanceof Spell p) {
            p.tickDying(caster);
        }
    }

    @Override
    public boolean isDead() {
        return !(delegate.get() instanceof Spell p) || p.isDead();
    }

    @Override
    public boolean isDying() {
        return delegate.get() instanceof Spell p && p.isDying();
    }

    @Override
    public boolean isDirty() {
        return dirty || (delegate.get() instanceof Spell p && p.isDirty());
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    @Override
    public boolean isHidden() {
        return hidden || (delegate.get() instanceof Spell p && p.isHidden());
    }

    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public final void destroy(Caster<?> caster) {
        if (destroyed) {
            return;
        }
        destroyed = true;
        setDead();
        onDestroyed(caster);
    }

    protected void onDestroyed(Caster<?> caster) {
        if (delegate.get() instanceof Spell s) {
            s.destroy(caster);
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (delegate.get() instanceof Spell s) {
            if (s.isDying()) {
                s.tickDying(source);
                return !s.isDead();
            }
            return s.tick(source, situation) && !isDead();
        }
        return !isDead();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putUuid("uuid", uuid);
        compound.putBoolean("hidden", hidden);
        compound.put("spell", delegate.toNBT());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        dirty = false;
        hidden = compound.getBoolean("hidden");
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        delegate.fromNBT(compound.getCompound("spell"));
    }

    @Override
    public final String toString() {
        return "Delegate{" + getTypeAndTraits() + "}[uuid=" + uuid + ", destroyed=" + destroyed + ", hidden=" + hidden + "][spell=" + delegate.get() + "]";
    }
}
