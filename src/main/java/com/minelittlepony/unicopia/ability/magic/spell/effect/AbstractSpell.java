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
            type = type.type().withTraits(SpellTraits.fromNbt(compound.getCompound("traits")).orElse(SpellTraits.EMPTY));
        }
    }

    @Override
    public final String toString() {
        return "Spell[uuid=" + uuid + ", dead=" + isDead + ", type=" + getType() + "]";
    }
}
