package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.UUID;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.nbt.NbtCompound;

public abstract class AbstractSpell implements Spell {

    private boolean isDead;
    private boolean isDirty;

    private final SpellType<?> type;

    private SpellTraits traits;

    private UUID uuid = UUID.randomUUID();

    protected AbstractSpell(SpellType<?> type, SpellTraits traits) {
        this.type = type;
        this.traits = traits;
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public SpellType<?> getType() {
        return type;
    }

    protected SpellTraits getTraits() {
        return traits == null ? SpellTraits.EMPTY : traits;
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
    public final boolean apply(Caster<?> caster) {
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
        compound.put("traits", getTraits().toNbt());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        isDirty = false;
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        isDead = compound.getBoolean("dead");
        if (compound.contains("traits")) {
            traits = SpellTraits.readNbt(compound.getCompound("traits")).orElse(SpellTraits.EMPTY);
        }
    }
}
