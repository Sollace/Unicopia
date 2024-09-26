package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class MimicSpell extends AbstractDisguiseSpell implements HomingSpell, TimedSpell {

    static final TooltipFactory TOOLTIP = TimedSpell.TIME;

    private final Timer timer = new Timer(TIME.get(getTraits()));

    protected MimicSpell(CustomisedSpellType<?> type) {
        super(type);
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

        return super.tick(caster, situation);
    }

    @Override
    public boolean setTarget(Entity target) {
        setDisguise(target);
        return true;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        timer.toNBT(compound, lookup);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        timer.fromNBT(compound, lookup);
    }
}
