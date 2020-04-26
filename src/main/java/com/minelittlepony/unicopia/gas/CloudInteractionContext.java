package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface CloudInteractionContext {

    static CloudInteractionContext of(Entity entity) {
        return entity == null ? empty() : new Impl(entity);
    }

    static CloudInteractionContext empty() {
        return Impl.EMPTY;
    }

    default boolean isPlayer() {
        return false;
    }

    default boolean isPegasis() {
        return false;
    }

    default ItemStack getHeldStack() {
        return ItemStack.EMPTY;
    }

    default boolean isEmpty() {
        return this == empty();
    }

    boolean canTouch(GasState type);

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
        default ItemStack getHeldStack() {
            return getCloudInteractionContext().getHeldStack();
        }

        @Override
        default boolean canTouch(GasState type) {
            return getCloudInteractionContext().canTouch(type);
        }

        @Override
        default boolean isEmpty() {
            return getCloudInteractionContext().isEmpty();
        }
    }

    class Impl implements CloudInteractionContext {
        private static final CloudInteractionContext EMPTY = type -> true;

        private final boolean isPlayer;
        private final boolean isPegasis;

        private ItemStack main = ItemStack.EMPTY;

        private Impl(Entity entity) {
            this.isPlayer = EquinePredicates.IS_PLAYER.test(entity);
            this.isPegasis = EquinePredicates.ENTITY_INTERACT_WITH_CLOUD_BLOCKS.test(entity);

            if (entity instanceof LivingEntity) {
                main = ((LivingEntity)entity).getMainHandStack();
                if (main.isEmpty()) {
                    main = ((LivingEntity)entity).getOffHandStack();
                }
            }
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
        public boolean canTouch(GasState type) {
            return type.isTouchable(isPlayer(), isPegasis());
        }

        @Override
        public ItemStack getHeldStack() {
            return main;
        }
    }
}
