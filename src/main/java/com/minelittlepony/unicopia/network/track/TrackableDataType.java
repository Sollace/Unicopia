package com.minelittlepony.unicopia.network.track;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record TrackableDataType<T>(int id, PacketCodec<T> codec) {
    private static final Int2ObjectMap<TrackableDataType<?>> REGISTRY = new Int2ObjectOpenHashMap<>();

    public static final TrackableDataType<Integer> INT = of(new Identifier("integer"), PacketCodec.INT);
    public static final TrackableDataType<Float> FLOAT = of(new Identifier("float"), PacketCodec.FLOAT);
    public static final TrackableDataType<Boolean> BOOLEAN = of(new Identifier("boolean"), PacketCodec.BOOLEAN);
    public static final TrackableDataType<UUID> UUID = of(new Identifier("uuid"), PacketCodec.UUID);
    public static final TrackableDataType<NbtCompound> NBT = of(new Identifier("nbt"), PacketCodec.NBT);
    public static final TrackableDataType<NbtCompound> COMPRESSED_NBT = of(new Identifier("compressed_nbt"), PacketCodec.COMPRESSED_NBT);
    public static final TrackableDataType<Optional<PacketByteBuf>> RAW_BYTES = of(new Identifier("raw_bytes"), PacketCodec.RAW_BYTES.asOptional());

    public static final TrackableDataType<Optional<BlockPos>> OPTIONAL_POS = of(new Identifier("optional_pos"), PacketCodec.OPTIONAL_POS);
    public static final TrackableDataType<Race> RACE = TrackableDataType.of(Unicopia.id("race"), PacketCodec.ofRegistry(Race.REGISTRY));

    @SuppressWarnings("unchecked")
    public static <T> TrackableDataType<T> of(PacketByteBuf buffer) {
        int id = buffer.readInt();
        return Objects.requireNonNull((TrackableDataType<T>)REGISTRY.get(id), "Unknown trackable data type id: " + id);
    }

    @SuppressWarnings("unchecked")
    public static <T> TrackableDataType<T> of(Identifier typeName, PacketCodec<T> codec) {
        return (TrackableDataType<T>)REGISTRY.computeIfAbsent(typeName.hashCode(), t -> new TrackableDataType<>(t, codec));
    }

    public T read(PacketByteBuf buffer) {
        return codec().read(buffer);
    }

    public void write(PacketByteBuf buffer, T value) {
        buffer.writeInt(id);
        codec().write(buffer, value);
    }
}
