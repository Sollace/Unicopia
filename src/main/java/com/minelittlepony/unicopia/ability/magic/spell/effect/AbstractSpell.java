package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.server.world.Ether;

import net.minecraft.nbt.NbtCompound;

public abstract class AbstractSpell implements Spell {

    private boolean dead;
    private boolean dying;
    private boolean dirty;
    private boolean hidden;
    private boolean destroyed;

    private final CustomisedSpellType<?> type;

    private UUID uuid = UUID.randomUUID();

    protected AbstractSpell(CustomisedSpellType<?> type) {
        this.type = type;
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
        dying = true;
        setDirty();
    }

    @Override
    public final boolean isDead() {
        return dead;
    }

    @Override
    public final boolean isDying() {
        return dying;
    }

    @Override
    public final boolean isDirty() {
        return dirty;
    }

    @Override
    public final void setDirty() {
        dirty = true;
    }

    @Override
    public final boolean isHidden() {
        return hidden;
    }

    @Override
    public final void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void tickDying(Caster<?> caster) {
        dead = true;
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
        compound.putBoolean("dying", dying);
        compound.putBoolean("dead", dead);
        compound.putBoolean("hidden", hidden);
        compound.putUuid("uuid", uuid);
        compound.put("traits", getTraits().toNbt());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        dirty = false;
        if (compound.containsUuid("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        dying = compound.getBoolean("dying");
        dead = compound.getBoolean("dead");
        hidden = compound.getBoolean("hidden");
    }

    @Override
    public final String toString() {
        return "Spell{" + getTypeAndTraits() + "}[uuid=" + uuid + ", dead=" + dead + ", hidden=" + hidden + "]";
    }
}
