package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractSpell implements Spell {

    private boolean isDead;
    private boolean isDirty;

    private final SpellType<?> type;

    protected AbstractSpell(SpellType<?> type) {
        this.type = type;
    }

    @Override
    public SpellType<?> getType() {
        return type;
    }

    @Override
    public void setDead() {
        isDead = true;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public Affinity getAffinity() {
        return getType().getAffinity();
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putBoolean("dead", isDead);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        setDirty(false);
        isDead = compound.getBoolean("dead");
    }
}
