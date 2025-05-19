package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

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
            target.setAttacker(player.asEntity());
        }

        if (entity instanceof IronGolemEntity i) {
            boolean hasPoppy = player.asEntity().getStackInHand(Hand.MAIN_HAND).isOf(Items.POPPY);
            if (hasPoppy != i.getLookingAtVillagerTicks() > 0) {
                i.setLookingAtVillager(hasPoppy);
            }
        }
    }

    protected LivingEntity findTarget(Pony player, T entity) {
        return TraceHelper.<LivingEntity>findEntity(player.asEntity(), 6, 1,
                e -> e instanceof LivingEntity && e != entity && !player.isOwnedBy(e))
                .orElseGet(() -> getDummy(entity));
    }

    @SuppressWarnings("unchecked")
    protected T getDummy(T entity) {
        if (dummy == null) {
            dummy = (T)entity.getType().create(entity.getWorld());
        }

        return dummy;
    }
}
