package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.nbt.NbtCompound;

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
        setDirty();
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
    public void setDirty() {
        isDirty = true;
    }

    @Override
    public Affinity getAffinity() {
        return getType().getAffinity();
    }

    @Override
    public boolean apply(Caster<?> caster) {
        caster.setSpell(this);
        return true;
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putBoolean("dead", isDead);
        compound.putUuid("uuid", uuid);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        isDirty = false;
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        isDead = compound.getBoolean("dead");
    }
}
