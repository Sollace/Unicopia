package com.minelittlepony.unicopia.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class EntityReference<T extends Entity> implements NbtSerialisable {

    private UUID uuid;
    private int clientId;

    public void set(@Nullable T entity) {
        if (entity != null) {
            uuid = entity.getUuid();
            clientId = entity.getEntityId();
        }
    }

    public boolean isPresent(World world) {
        T entity = get(world);
        return entity != null && !entity.removed;
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
        tag.putInt("clientId", clientId);
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        if (tag.containsUuid("uuid")) {
            uuid = tag.getUuid("uuid");
        }
        clientId = tag.getInt("clientId");
    }
}
