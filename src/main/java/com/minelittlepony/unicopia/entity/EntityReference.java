package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * An indirect reference to an entity by its unique id.
 * Used to store the 'owner' reference for certain objects that allows them to\
 * remember who they belong to even when the entity has been unloaded.
 *
 * @param <T> The type of the entity this reference points to.
 */
public class EntityReference<T extends Entity> implements NbtSerialisable {

    /**
     * Server UUID of the entity used to look up the entity instance on the server.
     */
    private Optional<UUID> uuid = Optional.empty();
    /**
     * Corresponding client id used to look up the entity instance on the client.
     */
    private int clientId;
    /**
     * The last-known position of the entity.
     */
    private Optional<Vec3d> pos = Optional.empty();

    public EntityReference() {}

    public EntityReference(T entity) {
        set(entity);
    }

    public EntityReference(NbtCompound nbt) {
        fromNBT(nbt);
    }

    public void copyFrom(EntityReference<? extends T> other) {
        uuid = other.uuid;
        clientId = other.clientId;
        pos = other.pos;
    }

    public void set(@Nullable T entity) {
        if (entity != null) {
            uuid = Optional.of(entity.getUuid());
            clientId = entity.getId();
            pos = Optional.of(entity.getPos());
        } else {
            uuid = Optional.empty();
            clientId = 0;
            pos = Optional.empty();
        }
    }

    /**
     * Gets the assigned entity's UUID
     */
    public Optional<UUID> getId() {
        return uuid;
    }

    /**
     * Gets the last known position of the assigned entity.
     */
    public Optional<Vec3d> getPosition() {
        return pos;
    }

    public boolean isSet() {
        return getId().isPresent();
    }

    public boolean referenceEquals(Entity entity) {
        return entity != null && entity.getUuid().equals(uuid.orElse(null));
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
        if (uuid.isPresent() && world instanceof ServerWorld serverWorld) {
            return uuid.map(serverWorld::getEntity).map(e -> (T)e).filter(this::checkReference);
        }

        if (clientId != 0) {
            return Optional.ofNullable((T)world.getEntityById(clientId)).filter(this::checkReference);
        }

        return Optional.empty();
    }

    private boolean checkReference(Entity e) {
        pos = Optional.of(e.getPos());
        return e instanceof PlayerEntity || !e.isRemoved();
    }

    @Override
    public void toNBT(NbtCompound tag) {
        uuid.ifPresent(uuid -> tag.putUuid("uuid", uuid));
        pos.ifPresent(p -> tag.put("pos", NbtSerialisable.writeVector(p)));
        tag.putInt("clientId", clientId);
    }

    @Override
    public void fromNBT(NbtCompound tag) {
        uuid = tag.containsUuid("uuid") ? Optional.of(tag.getUuid("uuid")) : Optional.empty();
        pos = tag.contains("pos") ? Optional.ofNullable(NbtSerialisable.readVector(tag.getList("pos", NbtElement.DOUBLE_TYPE))) : Optional.empty();
        clientId = tag.getInt("clientId");
    }
}
