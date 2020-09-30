package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class MobBehaviour extends EntityBehaviour<MobEntity> {

    private MobEntity dummy;

    @Override
    public void update(Pony player, MobEntity entity, DisguiseSpell spell) {

        if (player.sneakingChanged() && isSneakingOnGround(player)) {

            LivingEntity target = RayTraceHelper.<LivingEntity>findEntity(player.getEntity(), 6, 1,
                    e -> e instanceof LivingEntity && e != entity && e != player.getOwner())
                    .orElseGet(() -> getDummy(entity));

            entity.tryAttack(target);
            target.setAttacker(player.getOwner());
        }
    }

    private MobEntity getDummy(MobEntity entity) {
        if (dummy == null) {
            dummy = (MobEntity)entity.getType().create(entity.world);
        }
        return dummy;
    }
}
