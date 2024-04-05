package com.minelittlepony.unicopia.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

/**
 * Here be dragons
 */
public interface FluidHelper {
    static FluidState getFullFluidState(FluidVariant variant) {
        return PsyFluidHelper.getFullFluidState(variant);
    }

    static Optional<Pair<Long, FluidVariant>> extract(ItemStack stack, PlayerEntity player, Hand hand) {
        return getAsContainer(stack, ContainerItemContext.forPlayerInteraction(player, hand))
                .filter(c -> !c.isResourceBlank())
                .map(container -> applyTransaction(t -> {
                    FluidVariant type = container.getResource();
                    long amountExtracted = container.extract(type, FluidConstants.BUCKET, t);
                    if (amountExtracted > 0) {
                        return new Pair<>(amountExtracted, type);
                    }
                    return null;
                }));
    }

    static long deposit(ItemStack stack, PlayerEntity player, Hand hand, FluidVariant variant, long amount) {
        return amount - getAsStorage(stack, ContainerItemContext.forPlayerInteraction(player, hand))
                .map(storage -> applyTransaction(t -> storage.insert(variant, amount, t)))
                .orElse(0L);
    }

    private static Optional<Storage<FluidVariant>> getAsStorage(ItemStack stack, ContainerItemContext context) {
        return Optional.ofNullable(FluidStorage.ITEM.find(stack, context));
    }

    private static Optional<StorageView<FluidVariant>> getAsContainer(ItemStack stack, ContainerItemContext context) {
        return getAsStorage(stack, context).map(storage -> {
            Iterator<StorageView<FluidVariant>> iter = storage.iterator();
            return iter.hasNext() ? iter.next() : null;
        });
    }

    private static <T> T applyTransaction(Function<Transaction, T> action) {
        try (@SuppressWarnings("deprecation") var transaction = Transaction.isOpen()
                ? Transaction.getCurrentUnsafe().openNested()
                : Transaction.openOuter()) {
            try {
                return action.apply(transaction);
            } finally {
                transaction.commit();
            }
        }
    }
}
