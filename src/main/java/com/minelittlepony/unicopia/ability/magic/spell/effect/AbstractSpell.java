package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.nbt.NbtCompound;

public abstract class AbstractSpell implements Spell {

    private boolean dead;
    private boolean dirty;
    private boolean hidden;
    private boolean destroyed;

    private CustomisedSpellType<?> type;

    private UUID uuid = UUID.randomUUID();

    protected AbstractSpell(CustomisedSpellType<?> type) {
        this.type = type;
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public final SpellType<?> getType() {
        return type.type();
    }

    public final CustomisedSpellType<?> getTypeAndTraits() {
        return type;
    }

    @Override
    public final SpellTraits getTraits() {
        return type.traits();
    }

    @Override
    public final void setDead() {
        dead = true;
        setDirty();
    }

    @Override
    public final boolean isDead() {
        return dead;
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
    public Affinity getAffinity() {
        return getType().getAffinity();
    }

    protected void onDestroyed(Caster<?> caster) {
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

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putBoolean("dead", dead);
        compound.putBoolean("hidden", hidden);
        compound.putUuid("uuid", uuid);
        compound.put("traits", getTraits().toNbt());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        dirty = false;
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        dead = compound.getBoolean("dead");
        hidden = compound.getBoolean("hidden");
        if (compound.contains("traits")) {
            type = type.type().withTraits(SpellTraits.fromNbt(compound.getCompound("traits")).orElse(SpellTraits.EMPTY));
        }
    }

    @Override
    public final String toString() {
        return "Spell{" + getTypeAndTraits() + "}[uuid=" + uuid + ", dead=" + dead + ", hidden=" + hidden + "]";
    }
}
