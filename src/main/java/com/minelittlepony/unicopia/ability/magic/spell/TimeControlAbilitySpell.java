package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;

/**
 * Internal.
 * <p>
 * Used by the time change ability
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

        if (!source.asWorld().getGameRules().getBoolean(UGameRules.DO_TIME_MAGIC) || situation != Situation.BODY || !source.canUse(Abilities.TIME)) {
            return false;
        }

        update(source, Rot.of(source));
        return source.subtractEnergyCost(2);
    }

    public void update(Caster<?> source, Rot rotation) {
        if (!source.asWorld().getGameRules().getBoolean(UGameRules.DO_TIME_MAGIC)) {
            return;
        }
        if (!(source.asWorld() instanceof ServerWorld sw)) {
            return;
        }

        float yaw = -(rotation.yaw() + 90);
        float pitch = -(rotation.pitch() / 90F);
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

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.putBoolean("initilized", initilized);
        compound.putLong("timeOffset", timeOffset);
        compound.putFloat("angleOffset", angleOffset);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        initilized = compound.getBoolean("initilized");
        timeOffset = compound.getLong("timeOffset");
        angleOffset = compound.getFloat("angleOffset");
    }
}
