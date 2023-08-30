package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

/**
 * Internal.
 * <p>
 * Used by the Rainboom ability.
 */
public class TimeControlAbilitySpell extends AbstractSpell {

    private boolean initilized;

    private long timeOffset;
    private float angleOffset;

    public TimeControlAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY || !(source instanceof Pony pony) || !Abilities.TIME.canUse(pony.getCompositeRace())) {
            return false;
        }

        if (source.asWorld() instanceof ServerWorld sw) {

            float yaw = -(source.asEntity().getHeadYaw() + 90);
            float pitch = -(source.asEntity().getPitch(1) / 90F);

            long time = (long)(pitch * 6000);

            // sunrise(0) - midday(1) - sunset(2) - midnight(3)

            if (!initilized) {
                initilized = true;
                timeOffset = sw.getTimeOfDay() - time;
                angleOffset = UnicopiaWorldProperties.forWorld(sw).getTangentalSkyAngle() - yaw;
            }

            if (angleOffset > 90 && angleOffset < 270) {
                time *= -1;
            }

            time += timeOffset;
            if (time < 0) {
                time += 24000;
            }
            time %= 24000;

            sw.setTimeOfDay(time);
            sw.getServer().sendTimeUpdatePackets();

            UnicopiaWorldProperties.forWorld(sw).setTangentalSkyAngle(angleOffset + yaw);
        }

        return source.subtractEnergyCost(2);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("initilized", initilized);
        compound.putLong("timeOffset", timeOffset);
        compound.putFloat("angleOffset", angleOffset);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        initilized = compound.getBoolean("initilized");
        timeOffset = compound.getLong("timeOffset");
        angleOffset = compound.getFloat("angleOffset");
    }
}
