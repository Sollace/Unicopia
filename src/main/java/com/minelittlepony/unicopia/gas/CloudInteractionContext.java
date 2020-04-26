package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.CloudEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface CloudInteractionContext {

    static CloudInteractionContext of(Entity entity) {
        return entity == null ? Impl.EMPTY : new Impl(entity);
    }

    default boolean isPlayer() {
        return false;
    }

    default boolean isPegasis() {
        return false;
    }

    boolean canTouch(CloudType type);

    class Impl implements CloudInteractionContext {
        private static final CloudInteractionContext EMPTY = type -> false;

        private final boolean isPlayer;
        private final boolean isPegasis;

        private Impl(Entity entity) {
            this.isPlayer = entity instanceof PlayerEntity;
            this.isPegasis = isPegasis(entity);
        }

        private boolean isPegasis(Entity entity) {
            if (entity instanceof PlayerEntity) {
                return EquinePredicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)entity)
                    || (EquinePredicates.MAGI.test(entity) && CloudEntity.getFeatherEnchantStrength((PlayerEntity)entity) > 0);
            }

            if (entity instanceof ItemEntity) {
                return EquinePredicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)entity);
            }

            if (entity instanceof CloudEntity && entity.hasVehicle()) {
                return isPegasis(entity.getVehicle());
            }

            return false;
        }

        @Override
        public boolean isPlayer() {
            return isPlayer;
        }

        @Override
        public boolean isPegasis() {
            return isPegasis;
        }

        @Override
        public boolean canTouch(CloudType type) {
            return type.isTouchable(isPlayer()) || isPegasis();
        }
    }
}
