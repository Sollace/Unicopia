package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityReference<T extends Entity> implements NbtSerialisable {

    private UUID uuid;
    private int clientId;

    private Optional<Vec3d> pos = Optional.empty();

    public void set(@Nullable T entity) {
        if (entity != null) {
            uuid = entity.getUuid();
            clientId = entity.getEntityId();
            pos = Optional.of(entity.getPos());
        }
    }

    public Optional<Vec3d> getPosition() {
        return pos;
    }

    public boolean isPresent(World world) {
        T entity = get(world);
        return entity != null && !entity.removed;
    }

    public void ifPresent(World world, Consumer<T> consumer) {
        if (isPresent(world)) {
            consumer.accept(get(world));
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public T get(World world) {
        if (uuid != null && world instanceof ServerWorld) {
            return (T)((ServerWorld)world).getEntity(uuid);
        }

        if (clientId != 0) {
            return (T)world.getEntityById(clientId);
        }

        return null;
    }

    @Override
    public void toNBT(CompoundTag tag) {
        if (uuid != null) {
            tag.putUuid("uuid", uuid);
        }
        pos.ifPresent(p -> {
            tag.put("pos", NbtSerialisable.writeVector(p));
        });
        tag.putInt("clientId", clientId);
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        uuid = tag.containsUuid("uuid") ? tag.getUuid("uuid") : null;
        pos = tag.contains("pos") ? Optional.ofNullable(NbtSerialisable.readVector(tag.getList("pos", 6))) : Optional.empty();
        clientId = tag.getInt("clientId");
    }
}
