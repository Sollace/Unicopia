package com.minelittlepony.unicopia.util.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public record PacketCodec<T>(PacketByteBuf.PacketReader<T> reader, PacketByteBuf.PacketWriter<T> writer) {
    public static final PacketCodec<Boolean> BOOLEAN = new PacketCodec<>(PacketByteBuf::readBoolean, PacketByteBuf::writeBoolean);
    public static final PacketCodec<Float> FLOAT = new PacketCodec<>(PacketByteBuf::readFloat, PacketByteBuf::writeFloat);
    public static final PacketCodec<Integer> INT = new PacketCodec<>(PacketByteBuf::readInt, PacketByteBuf::writeInt);
    public static final PacketCodec<Byte> BYTE = new PacketCodec<>(PacketByteBuf::readByte, (b, v) -> b.writeByte(v));
    public static final PacketCodec<Long> LONG = new PacketCodec<>(PacketByteBuf::readLong, PacketByteBuf::writeLong);
    public static final PacketCodec<String> STRING = new PacketCodec<>(PacketByteBuf::readString, PacketByteBuf::writeString);
    public static final PacketCodec<UUID> UUID = new PacketCodec<>(PacketByteBuf::readUuid, PacketByteBuf::writeUuid);
    public static final PacketCodec<Optional<UUID>> OPTIONAL_UUID = UUID.asOptional();

    public static final PacketCodec<Identifier> IDENTIFIER = STRING.xMap(Identifier::new, Identifier::toString);

    public static final PacketCodec<NbtCompound> NBT = new PacketCodec<>(PacketByteBuf::readNbt, PacketByteBuf::writeNbt);

    public static final PacketCodec<PacketByteBuf> RAW_BYTES = new PacketCodec<>(
            buffer -> new PacketByteBuf(buffer.readBytes(buffer.readInt())),
            (buffer, bytes) -> {
        buffer.writeInt(bytes.writerIndex());
        buffer.writeBytes(bytes);
    });
    public static final PacketCodec<NbtCompound> COMPRESSED_NBT = RAW_BYTES.xMap(buffer -> {
        try (InputStream in = new ByteBufInputStream(buffer)) {
            return NbtIo.readCompressed(in, NbtTagSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }, nbt -> {
        var buffer = new PacketByteBuf(Unpooled.buffer());
        try (ByteBufOutputStream out = new ByteBufOutputStream(buffer)) {
            NbtIo.writeCompressed(nbt, out);
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    public static final PacketCodec<BlockPos> POS = new PacketCodec<>(PacketByteBuf::readBlockPos, PacketByteBuf::writeBlockPos);
    public static final PacketCodec<Optional<BlockPos>> OPTIONAL_POS = POS.asOptional();

    public static final <T> PacketCodec<T> ofRegistry(Registry<T> registry) {
        return INT.xMap(registry::get, registry::getRawId);
    }

    public static final <T extends Enum<T>> PacketCodec<T> ofEnum(Supplier<T[]> valuesGetter) {
        final T[] values = valuesGetter.get();
        return INT.xMap(id -> values[MathHelper.clamp(id, 0, values.length)], Enum::ordinal);
    }

    public T read(PacketByteBuf buf) {
        return reader().apply(buf);
    }

    public void write(PacketByteBuf buf, T value) {
        writer().accept(buf, value);
    }

    public PacketCodec<Optional<T>> asOptional() {
        return new PacketCodec<>(buf -> buf.readOptional(reader), (buf, v) -> buf.writeOptional(v, writer));
    }

    public <X> PacketCodec<X> xMap(Function<T, X> to, Function<X, T> from) {
        return new PacketCodec<>(buf -> to.apply(reader.apply(buf)), (buf, v) -> writer.accept(buf, from.apply(v)));
    }

    public <X> PacketCodec<X> andThen(BiFunction<PacketByteBuf, T, X> to, Function<X, T> from, BiConsumer<PacketByteBuf, X> write) {
        return new PacketCodec<>(buf -> {
            return to.apply(buf, reader.apply(buf));
        }, (buf, v) -> {
            writer.accept(buf, from.apply(v));
            write.accept(buf, v);
        });
    }
}
