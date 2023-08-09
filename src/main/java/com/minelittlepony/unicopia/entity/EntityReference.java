package com.minelittlepony.unicopia.entity;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * An indirect reference to an entity by its unique id.
 * Used to store the 'owner' reference for certain objects that allows them to\
 * remember who they belong to even when the entity has been unloaded.
 *
 * Will also remember the position and certain attributes of the owner.
 *
 * @param <T> The type of the entity this reference points to.
 */
public class EntityReference<T extends Entity> implements NbtSerialisable {
    @Nullable
    private EntityValues<T> reference;

    private WeakReference<T> directReference = new WeakReference<>(null);

    public EntityReference() {}

    public EntityReference(T entity) {
        set(entity);
    }

    public EntityReference(NbtCompound nbt) {
        fromNBT(nbt);
    }

    @SuppressWarnings("unchecked")
    public void copyFrom(EntityReference<? extends T> other) {
        this.reference = ((EntityReference<T>)other).reference;
        this.directReference = new WeakReference<>(other.directReference.get());
    }

    public boolean set(@Nullable T entity) {
        this.directReference = new WeakReference<>(entity);
        this.reference = entity == null ? null : new EntityValues<>(entity);
        return entity != null;
    }

    public Optional<EntityValues<T>> getTarget() {
        T value = directReference.get();
        if (value != null) {
            set(value);
        }
        return Optional.ofNullable(reference);
    }

    public boolean isSet() {
        return reference != null;
    }

    public boolean referenceEquals(Entity entity) {
        return entity != null && referenceEquals(entity.getUuid());
    }

    public boolean referenceEquals(UUID uuid) {
        return (reference == null ? Util.NIL_UUID : reference.uuid()).equals(uuid);
    }

    public boolean referenceEquals(@Nullable EntityReference<?> other) {
        final EntityValues<?> st = reference;
        final EntityValues<?> ot = other == null ? null : other.reference;
        return st == ot || (st != null && ot != null && Objects.equals(st.uuid(), ot.uuid()));
    }

    public void ifPresent(World world, Consumer<T> consumer) {
        getOrEmpty(world).ifPresent(consumer);
    }

    @Nullable
    public T get(World world) {
        return getOrEmpty(world).orElse(null);
    }

    public Optional<T> getOrEmpty(World world) {
        return Optional.ofNullable(directReference.get())
                .or(() -> reference == null ? Optional.empty() : reference.resolve(world))
                .filter(this::set);
    }

    @Override
    public void toNBT(NbtCompound tag) {
        getTarget().ifPresent(ref -> ref.toNBT(tag));
    }

    @Override
    public void fromNBT(NbtCompound tag) {
        this.reference = tag.contains("uuid") ? new EntityValues<>(tag) : null;
    }

    @Override
    public int hashCode() {
        return getTarget().map(EntityValues::uuid).orElse(Util.NIL_UUID).hashCode();
    }

    public record EntityValues<T extends Entity>(
            UUID uuid,
            Vec3d pos,
            int clientId,
            boolean isPlayer,
            boolean isDead,
            Levelled.LevelStore level,
            Levelled.LevelStore corruption) {
        public EntityValues(Entity entity) {
            this(
                entity.getUuid(),
                entity.getPos(),
                entity.getId(), entity instanceof PlayerEntity,
                !entity.isAlive(),
                Caster.of(entity).map(Caster::getLevel).map(Levelled::copyOf).orElse(Levelled.EMPTY),
                Caster.of(entity).map(Caster::getCorruption).map(Levelled::copyOf).orElse(Levelled.EMPTY)
            );
        }

        public EntityValues(NbtCompound tag) {
            this(
                tag.getUuid("uuid"),
                NbtSerialisable.readVector(tag.getList("pos", NbtElement.DOUBLE_TYPE)),
                tag.getInt("clientId"),
                tag.getBoolean("isPlayer"),
                tag.getBoolean("isDead"),
                Levelled.fromNbt(tag.getCompound("level")),
                Levelled.fromNbt(tag.getCompound("corruption"))
            );
        }

        @SuppressWarnings("unchecked")
        public Optional<T> resolve(World world) {
            if (world instanceof ServerWorld serverWorld) {
                return Optional.ofNullable((T)serverWorld.getEntity(uuid));
            }
            return Optional.ofNullable((T)world.getEntityById(clientId()));
        }

        public void toNBT(NbtCompound tag) {
            tag.putUuid("uuid", uuid);
            tag.put("pos", NbtSerialisable.writeVector(pos));
            tag.putInt("clientId", clientId);
            tag.putBoolean("isPlayer", isPlayer);
            tag.putBoolean("isDead", isDead);
            tag.put("level", level.toNbt());
            tag.put("corruption", corruption.toNbt());
        }
    }
}
