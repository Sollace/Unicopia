package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
            clientId = entity.getId();
            pos = Optional.of(entity.getPos());
        }
    }

    /**
     * Gets the position the last known position of the assigned entity.
     */
    public Optional<Vec3d> getPosition() {
        return pos;
    }

    public boolean isPresent(World world) {
        return getOrEmpty(world).isPresent();
    }

    public void ifPresent(World world, Consumer<T> consumer) {
        getOrEmpty(world).ifPresent(consumer);
    }

    @Nullable
    public T get(World world) {
        return getOrEmpty(world).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public Optional<T> getOrEmpty(World world) {
        if (uuid != null && world instanceof ServerWorld) {
            return Optional.ofNullable((T)((ServerWorld)world).getEntity(uuid)).filter(this::checkReference);
        }

        if (clientId != 0) {
            return Optional.ofNullable((T)world.getEntityById(clientId)).filter(this::checkReference);
        }

        return Optional.empty();
    }

    private boolean checkReference(Entity e) {
        pos = Optional.of(e.getPos());
        return !e.isRemoved();
    }

    @Override
    public void toNBT(NbtCompound tag) {
        if (uuid != null) {
            tag.putUuid("uuid", uuid);
        }
        pos.ifPresent(p -> {
            tag.put("pos", NbtSerialisable.writeVector(p));
        });
        tag.putInt("clientId", clientId);
    }

    @Override
    public void fromNBT(NbtCompound tag) {
        uuid = tag.containsUuid("uuid") ? tag.getUuid("uuid") : null;
        pos = tag.contains("pos") ? Optional.ofNullable(NbtSerialisable.readVector(tag.getList("pos", NbtElement.DOUBLE_TYPE))) : Optional.empty();
        clientId = tag.getInt("clientId");
    }
}
