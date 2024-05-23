package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.*;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.server.world.Ether;

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

    private Spell getOrEmpty() {
        return MoreObjects.firstNonNull(delegate.get(), EmptySpell.INSTANCE);
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
        getOrEmpty().setDead();
    }

    @Override
    public void tickDying(Caster<?> caster) {
        getOrEmpty().tickDying(caster);
    }

    @Override
    public boolean isDead() {
        return getOrEmpty().isDead();
    }

    @Override
    public boolean isDying() {
        return getOrEmpty().isDying();
    }

    @Override
    public boolean isDirty() {
        return dirty || delegate.hasDirtySpell();
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    @Override
    public boolean isHidden() {
        return hidden || getOrEmpty().isHidden();
    }

    @Override
    public void setHidden(boolean hidden) {
        getOrEmpty().setHidden(hidden);
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
        if (!caster.isClient()) {
            Ether.get(caster.asWorld()).remove(this, caster);
        }
        getOrEmpty().destroy(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        Spell s = getOrEmpty();
        if (s.isDying()) {
            s.tickDying(source);
            return !s.isDead();
        }
        return s.tick(source, situation);
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
