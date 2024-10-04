package com.minelittlepony.unicopia.network.track;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record TrackableDataType<T>(int id, PacketCodec<RegistryByteBuf, TypedValue<T>> codec) {
    private static final Int2ObjectMap<TrackableDataType<?>> REGISTRY = new Int2ObjectOpenHashMap<>();
    public static final PacketCodec<RegistryByteBuf, TypedValue<?>> PACKET_CODEC = PacketCodecs.INTEGER.<RegistryByteBuf>cast().xmap(
            id -> Objects.requireNonNull(REGISTRY.get(id.intValue()), "Unknown trackable data type id: " + id),
            type -> type.id()
    ).dispatch(value -> Untyped.cast(value.type), TrackableDataType::codec);

    public static final TrackableDataType<Integer> INT = of(Identifier.of("integer"), PacketCodecs.INTEGER);
    public static final TrackableDataType<Float> FLOAT = of(Identifier.of("float"), PacketCodecs.FLOAT);
    public static final TrackableDataType<Boolean> BOOLEAN = of(Identifier.of("boolean"), PacketCodecs.BOOL);
    public static final TrackableDataType<UUID> UUID = of(Identifier.of("uuid"), Uuids.PACKET_CODEC);
    public static final TrackableDataType<NbtCompound> NBT = of(Identifier.of("nbt"), PacketCodecs.NBT_COMPOUND);
    public static final TrackableDataType<NbtCompound> COMPRESSED_NBT = of(Identifier.of("compressed_nbt"), PacketCodecs.NBT_COMPOUND);
    public static final TrackableDataType<Optional<PacketByteBuf>> RAW_BYTES = of(Identifier.of("raw_bytes"), PacketCodecUtils.OPTIONAL_BUFFER);

    public static final TrackableDataType<Optional<BlockPos>> OPTIONAL_POS = of(Identifier.of("optional_pos"), PacketCodecUtils.OPTIONAL_POS);
    public static final TrackableDataType<Optional<Vec3d>> OPTIONAL_VECTOR = of(Identifier.of("optional_vector"), PacketCodecUtils.OPTIONAL_VECTOR);
    private static final TrackableDataType<Optional<RegistryKey<?>>> OPTIONAL_REGISTRY_KEY = of(Identifier.of("optional_registry_key"), PacketCodecUtils.OPTIONAL_REGISTRY_KEY);

    public static final TrackableDataType<Race> RACE = of(Unicopia.id("race"), PacketCodecs.registryValue(Race.REGISTRY_KEY));

    public static <T> TrackableDataType<Optional<RegistryKey<T>>> ofRegistryKey() {
        return Untyped.cast(OPTIONAL_REGISTRY_KEY);
    }

    public static <T> TrackableDataType<T> of(Identifier typeName, PacketCodec<? super RegistryByteBuf, T> codec) {
        return Untyped.cast(REGISTRY.computeIfAbsent(typeName.hashCode(), t -> {
            AtomicReference<TrackableDataType<T>> ref = new AtomicReference<>();
            ref.set(new TrackableDataType<>(t, codec.<RegistryByteBuf>cast().xmap(
                    value -> new TypedValue<>(ref.get(), value),
                    value -> value.value
            )));
            return ref.get();
        }));
    }

    public static class TypedValue<T> {
        final TrackableDataType<T> type;
        public T value;

        public TypedValue(TrackableDataType<T> type, T value) {
            this.type = type;
            this.value = value;
        }
    }
}
