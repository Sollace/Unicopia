package com.minelittlepony.unicopia.network.track;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record TrackableDataType<T>(int id, PacketCodec<PacketByteBuf, T> codec) {
    private static final Int2ObjectMap<TrackableDataType<?>> REGISTRY = new Int2ObjectOpenHashMap<>();

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

    public static final TrackableDataType<Race> RACE = TrackableDataType.of(Unicopia.id("race"), PacketCodecs.registryValue(Race.REGISTRY_KEY));

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> TrackableDataType<Optional<RegistryKey<T>>> ofRegistryKey() {
        return (TrackableDataType)OPTIONAL_REGISTRY_KEY;
    }

    @SuppressWarnings("unchecked")
    public static <T> TrackableDataType<T> of(PacketByteBuf buffer) {
        int id = buffer.readInt();
        return Objects.requireNonNull((TrackableDataType<T>)REGISTRY.get(id), "Unknown trackable data type id: " + id);
    }

    @SuppressWarnings("unchecked")
    public static <T> TrackableDataType<T> of(Identifier typeName, PacketCodec<? extends ByteBuf, T> codec) {
        return (TrackableDataType<T>)REGISTRY.computeIfAbsent(typeName.hashCode(), t -> new TrackableDataType<>(t, (PacketCodec<PacketByteBuf, T>)codec));
    }

    public T read(PacketByteBuf buffer) {
        return codec().decode(buffer);
    }

    public void write(PacketByteBuf buffer, T value) {
        buffer.writeInt(id);
        codec().encode(buffer, value);
    }
}
