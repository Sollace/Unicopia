package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class MimicSpell extends AbstractDisguiseSpell implements HomingSpell, TimedSpell {
    static final int BASE_DURATION = 120 * 20;

    public static void appendTooltip(CustomisedSpellType<? extends MimicSpell> type, List<Text> tooltip) {
        TimedSpell.appendDurationTooltip(type, tooltip);
    }

    private final Timer timer;

    protected MimicSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer(BASE_DURATION + TimedSpell.getExtraDuration(getTraits()));
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
