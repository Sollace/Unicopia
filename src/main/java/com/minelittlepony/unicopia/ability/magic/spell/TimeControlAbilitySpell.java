package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Internal.
 * <p>
 * Used by the Rainboom ability.
 */
public class TimeControlAbilitySpell extends AbstractSpell {

    private Vec3d prevRotation = null;

    public TimeControlAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY) {
            return false;
        }

        if (!(source instanceof Pony pony) || !Abilities.TIME.canUse(pony.getCompositeRace())) {
            return false;
        }

        if (source.asWorld() instanceof ServerWorld sw) {

            float yaw = MathHelper.wrapDegrees(source.asEntity().getHeadYaw());
            float pitch = MathHelper.wrapDegrees(source.asEntity().getPitch(1)) / 90F;

            if (yaw > 0) {
                pitch += 90;
            }

            Vec3d rotation = new Vec3d(pitch, 0, 1);

            if (prevRotation != null) {
                pitch = (float)MathHelper.lerp(0.05, pitch, rotation.x);

                sw.setTimeOfDay((long)(pitch * 6000));
            }

            prevRotation = new Vec3d(pitch, 0, 1);
        }

        return source.subtractEnergyCost(2);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
    }
}
