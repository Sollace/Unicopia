package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

/**
 * A magic effect with a set duration capable of reporting how long it has until it runs out.
 */
public interface TimedSpell extends Spell {
    Timer getTimer();

    class Timer implements NbtSerialisable {
        public int maxDuration;
        public int duration;
        public int prevDuration;

        public Timer(int initial) {
            maxDuration = initial;
            duration = initial;
        }

        public void tick() {
            prevDuration = duration;
            duration--;
        }

        public float getPercentTimeRemaining(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevDuration, duration) / maxDuration;
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
