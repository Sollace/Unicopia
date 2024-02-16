package com.minelittlepony.unicopia.block;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockEntityUtil {
    @SuppressWarnings("unchecked")
    @Nullable
    static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (@Nullable BlockEntityTicker<A>) ticker : null;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    static <T extends BlockEntity> Optional<T> getOrCreateBlockEntity(World world, BlockPos pos, BlockEntityType<T> type) {
        return world.getBlockEntity(pos, type).or(() -> {
            BlockState state = world.getBlockState(pos);
            if (!(state.hasBlockEntity())) {
                return Optional.empty();
            }
            BlockEntity e =  ((BlockEntityProvider)state.getBlock()).createBlockEntity(pos, state);
            if (e == null || e.getType() != type) {
                return Optional.empty();
            }
            e.setCachedState(state);
            world.addBlockEntity(e);
            return Optional.of((T)e);
        });
    }

}
