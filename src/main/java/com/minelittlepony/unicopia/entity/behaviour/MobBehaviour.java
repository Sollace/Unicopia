package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class MobBehaviour<T extends MobEntity> extends EntityBehaviour<T> {

    private T dummy;

    @Override
    public void onDestroy(T entity) {
        entity.setAiDisabled(false);
        super.onDestroy(entity);
    }

    @Override
    public void update(Pony player, T entity, Disguise spell) {
        if (player.sneakingChanged() && isSneakingOnGround(player)) {
            LivingEntity target = findTarget(player, entity);
            entity.tryAttack(target);
            target.setAttacker(player.getMaster());
        }
    }

    protected LivingEntity findTarget(Pony player, T entity) {
        return TraceHelper.<LivingEntity>findEntity(player.getEntity(), 6, 1,
                e -> e instanceof LivingEntity && e != entity && !player.isOwnedBy(e))
                .orElseGet(() -> getDummy(entity));
    }

    @SuppressWarnings("unchecked")
    protected T getDummy(T entity) {
        if (dummy == null) {
            dummy = (T)entity.getType().create(entity.world);
        }

        return dummy;
    }
}
