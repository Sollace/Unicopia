package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.MathHelper;

/**
 * A magic effect with a set duration capable of reporting how long it has until it runs out.
 */
public interface TimedSpell extends Spell {
    int BASE_DURATION = 120 * 20;
    SpellAttribute<Integer> TIME = SpellAttribute.create(SpellAttributeType.SOAPINESS, AttributeFormat.TIME, AttributeFormat.PERCENTAGE, Trait.FOCUS, focus -> BASE_DURATION + (int)(MathHelper.clamp(focus, 0, 160) * 19) * 20);

    Timer getTimer();

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
        public void toNBT(NbtCompound compound, WrapperLookup lookup) {
            compound.putInt("duration", duration);
            compound.putInt("maxDuration", maxDuration);
        }

        @Override
        public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
            duration = compound.getInt("duration");
            maxDuration = compound.getInt("maxDuration");
        }
    }
}
