package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import net.minecraft.entity.Entity;
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

    interface Holder extends CloudInteractionContext {
        CloudInteractionContext getCloudInteractionContext();

        @Override
        default boolean isPlayer() {
            return getCloudInteractionContext().isPlayer();
        }

        @Override
        default boolean isPegasis() {
            return getCloudInteractionContext().isPegasis();
        }

        @Override
        default boolean canTouch(CloudType type) {
            return getCloudInteractionContext().canTouch(type);
        }
    }

    class Impl implements CloudInteractionContext {
        public static final CloudInteractionContext EMPTY = type -> true;

        private final boolean isPlayer;
        private final boolean isPegasis;

        private Impl(Entity entity) {
            this.isPlayer = entity instanceof PlayerEntity;
            this.isPegasis = EquinePredicates.ENTITY_INTERACT_WITH_CLOUD_BLOCKS.test(entity);
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
