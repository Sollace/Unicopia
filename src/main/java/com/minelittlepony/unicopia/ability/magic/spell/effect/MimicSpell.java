package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

public class MimicSpell extends AbstractDisguiseSpell implements HomingSpell, TimedSpell {

    private final Timer timer;

    protected MimicSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer((120 + (int)(getTraits().get(Trait.FOCUS, 0, 160) * 19)) * 20);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        setDirty();

        return super.tick(caster, situation);
    }

    @Override
    public boolean setTarget(Entity target) {
        setDisguise(target);
        return true;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        timer.fromNBT(compound);
    }
}
