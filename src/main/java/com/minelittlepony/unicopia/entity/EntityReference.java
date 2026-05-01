package com.minelittlepony.unicopia.entity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.network.track.TrackableObject;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * An indirect reference to an entity by its unique id.
 * Used to store the 'owner' reference for certain objects that allows them to
 * remember who they belong to even when the entity has been unloaded.
 *
 * Will also remember the position and certain attributes of the owner.
 *
 * @param <T> The type of the entity this reference points to.
 */
public class EntityReference<T extends Entity> implements NbtSerialisable, TrackableObject<EntityReference<T>> {
    public static final Codec<EntityReference<?>> CODEC = EntityValues.CODEC.codec().xmap(r -> new EntityReference<>(r), r -> r.reference);
    public static final Codec<List<EntityReference<?>>> LIST_CODEC = CODEC.listOf();

    public static <T extends Entity> Codec<EntityReference<T>> codec() {
        return Untyped.cast(CODEC);
    }

    public static <T extends Entity> Codec<List<EntityReference<T>>> listCodec() {
        return Untyped.cast(LIST_CODEC);
    }

    @Nullable
    private EntityValues<T> reference;
    private WeakReference<T> directReference = new WeakReference<>(null);

    private boolean dirty = true;

    public EntityReference() {}

    public EntityReference(T entity) {
        set(entity);
    }

    public EntityReference(NbtCompound nbt, WrapperLookup lookup) {
        fromNBT(nbt, lookup);
    }

    private EntityReference(EntityValues<T> reference) {
        this.reference = reference;
    }

    @SuppressWarnings("unchecked")
    public void copyFrom(EntityReference<? extends T> other) {
        this.reference = ((EntityReference<T>)other).reference;
        this.directReference = new WeakReference<>(other.directReference.get());
        dirty = true;
    }

    public boolean set(@Nullable T entity) {
        this.directReference = new WeakReference<>(entity);
        this.reference = entity == null ? null : new EntityValues<>(entity);
        this.dirty = true;
        return entity != null;
    }

    public Optional<EntityValues<T>> getTarget() {
        T value = directReference.get();
        if (value != null) {
            this.reference = new EntityValues<>(value);
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
        T t = directReference.get();
        if (t == null && reference != null) {
            directReference = new WeakReference<>(t = reference.resolve(world).orElse(null));
        }
        return t;
    }

    public Optional<T> getOrEmpty(World world) {
        return Optional.ofNullable(get(world));
    }

    @Override
    public void toNBT(NbtCompound tag, WrapperLookup lookup) {
        getTarget().ifPresent(ref -> {
            EntityValues.CODEC.codec().encodeStart(lookup.getOps(NbtOps.INSTANCE), ref).result().ifPresent(nbt -> tag.copyFrom((NbtCompound)nbt));
        });
    }

    @Override
    public void fromNBT(NbtCompound tag, WrapperLookup lookup) {
        this.reference = tag.decode(EntityValues.<T>mapCodec(), lookup.getOps(NbtOps.INSTANCE)).orElse(null);
        this.dirty = true;
        if (reference != null) {
            T value = directReference.get();
            if (value != null) {
                reference = new EntityValues<>(value);
            }
        }
    }

    @Override
    public int hashCode() {
        return getTarget().map(EntityValues::uuid).orElse(Util.NIL_UUID).hashCode();
    }

    @Override
    public Status getStatus() {
        if (dirty) {
            dirty = false;
            return Status.UPDATED;
        }
        return Status.DEFAULT;
    }

    @Override
    public void write(RegistryByteBuf buffer) {
        buffer.writeOptional(getTarget(), EntityValues.packetCodec());
    }

    @Override
    public void read(RegistryByteBuf buffer) {
        reference = buffer.readNullable(EntityValues.packetCodec());
        dirty = true;
        if (reference != null) {
            T value = directReference.get();
            if (value != null) {
                reference = new EntityValues<>(value);
            }
        }
    }

    @Override
    public void copyTo(EntityReference<T> destination) {
        destination.reference = reference;
        destination.directReference = directReference;
    }

    @Override
    public void discard(boolean immediate) {
        set(null);
    }

    public record EntityValues<T extends Entity>(
            UUID uuid,
            Vec3d pos,
            int clientId,
            boolean isPlayer,
            boolean isDead,
            Levelled.LevelStore level,
            Levelled.LevelStore corruption) {
        public static final MapCodec<EntityValues<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Uuids.CODEC.fieldOf("uuid").forGetter(EntityValues::uuid),
                CodecUtils.VECTOR.fieldOf("pos").forGetter(EntityValues::pos),
                Codec.INT.fieldOf("clientId").forGetter(EntityValues::clientId),
                Codec.BOOL.fieldOf("isPlayer").forGetter(EntityValues::isPlayer),
                Codec.BOOL.fieldOf("isDead").forGetter(EntityValues::isDead),
                Levelled.CODEC.fieldOf("level").forGetter(EntityValues::level),
                Levelled.CODEC.fieldOf("corruption").forGetter(EntityValues::corruption)
        ).apply(instance, EntityValues::new));
        public static final PacketCodec<ByteBuf, EntityValues<?>> PACKET_CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, EntityValues::uuid,
                PacketCodecUtils.VECTOR, EntityValues::pos,
                PacketCodecs.INTEGER, EntityValues::clientId,
                PacketCodecs.BOOLEAN, EntityValues::isPlayer,
                PacketCodecs.BOOLEAN, EntityValues::isDead,
                Levelled.PACKET_CODEC, EntityValues::level,
                Levelled.PACKET_CODEC, EntityValues::corruption,
                EntityValues::new
        );

        public static <T extends Entity> MapCodec<EntityValues<T>> mapCodec() {
            return Untyped.cast(CODEC);
        }

        public static <T extends Entity> PacketCodec<ByteBuf, EntityValues<T>> packetCodec() {
            return Untyped.cast(PACKET_CODEC);
        }

        public EntityValues(Entity entity) {
            this(
                entity.getUuid(),
                entity.getPos(),
                entity.getId(), entity instanceof PlayerEntity,
                !entity.isAlive(),
                Caster.of(entity).map(Caster::getLevel).map(Levelled::copyOf).orElse(Levelled.ZERO),
                Caster.of(entity).map(Caster::getCorruption).map(Levelled::copyOf).orElse(Levelled.ZERO)
            );
        }

        @SuppressWarnings("unchecked")
        public Optional<T> resolve(World world) {
            if (world instanceof ServerWorld serverWorld) {
                return Optional.ofNullable((T)serverWorld.getEntity(uuid));
            }
            Entity target = world.getEntityById(clientId());
            if (target == null || !target.getUuid().equals(uuid)) {
                return Optional.empty();
            }

            return Optional.of((T)target);
        }
    }
}
