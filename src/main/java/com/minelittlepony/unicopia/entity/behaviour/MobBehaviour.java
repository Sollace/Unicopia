package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class MobBehaviour extends EntityBehaviour<MobEntity> {
    @Override
    public void update(Caster<?> source, MobEntity entity, Spell spell) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && isSneakingOnGround(source)) {
                entity.tryAttack(RayTraceHelper.findEntity(source.getEntity(), 6, 1, e -> e instanceof LivingEntity).orElse(entity));
            }
        }
    }
}
