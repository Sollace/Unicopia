package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;

public class RavagerBehaviour extends EntityBehaviour<RavagerEntity> {
    @Override
    public void update(Caster<?> source, RavagerEntity entity, Spell spell) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && this.isSneakingOnGround(source)) {
                entity.tryAttack(RayTraceHelper.findEntity(source.getEntity(), 6, 1, e -> e instanceof LivingEntity).orElse(entity));
            }
        }
    }
}
