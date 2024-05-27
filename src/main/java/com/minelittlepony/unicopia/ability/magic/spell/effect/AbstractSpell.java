package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.server.world.Ether;

import net.minecraft.nbt.NbtCompound;

public abstract class AbstractSpell implements Spell {

    private UUID uuid = UUID.randomUUID();
    private final CustomisedSpellType<?> type;

    protected final DataTracker dataTracker = new DataTracker(0);

    private final DataTracker.Entry<Boolean> dead = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private final DataTracker.Entry<Boolean> dying = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private boolean dirty;
    private final DataTracker.Entry<Boolean> hidden = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private boolean destroyed;

    protected AbstractSpell(CustomisedSpellType<?> type) {
        this.type = type;
    }

    @Override
    public final DataTracker getDataTracker() {
        return dataTracker;
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    protected final SpellType<?> getType() {
        return type.type();
    }

    @Override
    public final CustomisedSpellType<?> getTypeAndTraits() {
        return type;
    }

    protected final SpellTraits getTraits() {
        return type.traits();
    }

    @Override
    public final void setDead() {
        dying.set(true);
    }

    @Override
    public final boolean isDead() {
        return dead.get();
    }

    @Override
    public final boolean isDying() {
        return dying.get();
    }

    @Deprecated
    @Override
    public final boolean isDirty() {
        return dirty;
    }

    @Deprecated
    @Override
    public final void setDirty() {
        dirty = true;
    }

    @Override
    public final boolean isHidden() {
        return hidden.get();
    }

    @Override
    public final void setHidden(boolean hidden) {
        this.hidden.set(hidden);
    }

    @Override
    public void tickDying(Caster<?> caster) {
        dead.set(true);
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
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putBoolean("dying", dying.get());
        compound.putBoolean("dead", dead.get());
        compound.putBoolean("hidden", hidden.get());
        compound.putUuid("uuid", uuid);
        compound.put("traits", getTraits().toNbt());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        dirty = false;
        if (compound.containsUuid("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        dying.set(compound.getBoolean("dying"));
        dead.set(compound.getBoolean("dead"));
        hidden.set(compound.getBoolean("hidden"));
    }

    @Override
    public final String toString() {
        return "Spell{" + getTypeAndTraits() + "}[uuid=" + uuid + ", dead=" + dead + ", hidden=" + hidden + "]";
    }
}
