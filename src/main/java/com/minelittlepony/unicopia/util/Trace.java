package com.minelittlepony.unicopia.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;

@SuppressWarnings("unchecked")
public record Trace(
        Optional<? extends HitResult> result
    ) {
    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that passes the given predicate
     * or block that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     * @param predicate    Predicate test to filter entities
     *
     * @return A Trace describing what was found.
     */
    public static Trace create(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        return new Trace(
                ((Optional<HitResult>)(Optional<?>)TraceHelper.traceEntity(e, distance, tickDelta, predicate))
                .or(() -> Optional.ofNullable(e.raycast(distance, tickDelta, false)))
        );
    }

    static <T extends Entity> T toEntity(EntityHitResult hit) {
        return (T)hit.getEntity();
    }

    public Optional<EntityHitResult> getEntityResult() {
        return result()
                .filter(s -> s.getType() == HitResult.Type.ENTITY)
                .map(EntityHitResult.class::cast);
    }

    public Optional<BlockHitResult> getBlockResult() {
        return result()
                .filter(s -> s.getType() == HitResult.Type.BLOCK)
                .map(BlockHitResult.class::cast);
    }

    public <T extends Entity> Optional<T> getEntity() {
        return getEntityResult().map(Trace::toEntity);
    }

    public Optional<BlockPos> getBlockPos() {
        return getBlockResult().map(BlockHitResult::getBlockPos);
    }

    public Optional<BlockPos> getBlockOrEntityPos() {
        return getBlockPos().or(() -> getEntity().map(Entity::getBlockPos));
    }

    public Optional<Direction> getSide() {
        return getBlockResult().map(BlockHitResult::getSide);
    }

    public Trace ifEntity(Consumer<Entity> consumer) {
        getEntity().ifPresent(consumer);
        return this;
    }

    public Trace ifBlock(Consumer<BlockPos> consumer) {
        getBlockPos().ifPresent(consumer);
        return this;
    }
}