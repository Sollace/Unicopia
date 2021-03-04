package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractSpell implements Spell {

    private boolean isDead;
    private boolean isDirty;

    private final SpellType<?> type;

    private UUID uuid;

    protected AbstractSpell(SpellType<?> type) {
        this.type = type;
        uuid = UUID.randomUUID();
    }

    @Override
    public final UUID getUuid() {
        return uuid;
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
        compound.putUuid("uuid", uuid);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        setDirty(false);
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        isDead = compound.getBoolean("dead");
    }
}
