package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;

public class EndermanBehaviour extends EntityBehaviour<EndermanEntity> {
    @Override
    public void update(Living<?> source, EndermanEntity entity, Disguise spell) {
        entity.setInvulnerable(!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(source.asEntity()));
        if (source.asEntity().isSneaking() || source.asEntity().isSprinting()) {
            entity.setTarget(entity);
        } else {
            entity.setTarget(null);
        }

        ItemStack stack = source.asEntity().getStackInHand(Hand.MAIN_HAND);
        if (stack.getItem() instanceof BlockItem bi) {
            entity.setCarriedBlock(bi.getBlock().getDefaultState());
        } else {
            entity.setCarriedBlock(null);
        }

        //if (entity.hurtTime > 0) {
        /*    Vec3d teleportedPos = entity.getPos();

            if (!teleportedPos.equals(source.asEntity().getPos())) {
                source.asEntity().refreshPositionAfterTeleport(teleportedPos.x, teleportedPos.y, teleportedPos.z);
            }*/
        //}
    }
}
