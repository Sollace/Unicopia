package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * A magic effect with a set duration capable of reporting how long it has until it runs out.
 */
public interface TimedSpell extends Spell {
    int BASE_DURATION = 120 * 20;

    Timer getTimer();

    static void appendDurationTooltip(CustomisedSpellType<?> type, List<Text> tooltip) {
        tooltip.add(SpellAttributes.ofTime(SpellAttributes.DURATION, BASE_DURATION + getExtraDuration(type.traits())));
    }

    static int getExtraDuration(SpellTraits traits) {
        return (int)(traits.get(Trait.FOCUS, 0, 160) * 19) * 20;
    }

    class Timer implements Tickable, NbtSerialisable {
        private int maxDuration;
        private int duration;
        private int prevDuration;

        public Timer(int initial) {
            maxDuration = initial;
            duration = initial;
        }

        @Override
        public void tick() {
            prevDuration = duration;
            duration--;
        }

        public float getPercentTimeRemaining(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevDuration, duration) / (float)maxDuration;
        }

        public int getTicksRemaining() {
            return duration;
        }

        @Override
        public void toNBT(NbtCompound compound) {
            compound.putInt("duration", duration);
            compound.putInt("maxDuration", maxDuration);
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            duration = compound.getInt("duration");
            maxDuration = compound.getInt("maxDuration");
        }
    }
}
