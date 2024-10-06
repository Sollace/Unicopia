package com.minelittlepony.unicopia.util.serialization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface PacketCodecUtils {
    PacketCodec<PacketByteBuf, PacketByteBuf> BUFFER = PacketCodec.of((bytes, buffer) -> {
        buffer.writeInt(bytes.writerIndex());
        buffer.writeBytes(bytes);
    }, buffer -> new PacketByteBuf(buffer.readBytes(buffer.readInt())));
    PacketCodec<PacketByteBuf, Optional<PacketByteBuf>> OPTIONAL_BUFFER = PacketCodecs.optional(BUFFER);

    PacketCodec<RegistryByteBuf, ByteBuf> REGISTRY_BUFFER = PacketCodec.of((bytes, buffer) -> {
        buffer.writeInt(bytes.writerIndex());
        buffer.writeBytes(bytes);
    }, buffer -> new RegistryByteBuf(buffer.readBytes(buffer.readInt()), buffer.getRegistryManager()));
    PacketCodec<RegistryByteBuf, Optional<ByteBuf>> OPTIONAL_REGISTRY_BUFFER = PacketCodecs.optional(REGISTRY_BUFFER);

    PacketCodec<PacketByteBuf, RegistryKey<?>> REGISTRY_KEY = PacketCodec.tuple(
            Identifier.PACKET_CODEC, RegistryKey::getRegistry,
            Identifier.PACKET_CODEC, RegistryKey::getValue,
            (registry, value) -> RegistryKey.of(RegistryKey.ofRegistry(registry), value)
    );
    PacketCodec<PacketByteBuf, Optional<RegistryKey<?>>> OPTIONAL_REGISTRY_KEY = PacketCodecs.optional(REGISTRY_KEY);
    PacketCodec<PacketByteBuf, Vec3d> VECTOR = PacketCodec.tuple(
            PacketCodecs.DOUBLE, Vec3d::getX,
            PacketCodecs.DOUBLE, Vec3d::getY,
            PacketCodecs.DOUBLE, Vec3d::getZ,
            Vec3d::new
    );
    PacketCodec<PacketByteBuf, Optional<Vec3d>> OPTIONAL_VECTOR = PacketCodecs.optional(VECTOR);
    PacketCodec<ByteBuf, Optional<BlockPos>> OPTIONAL_POS = PacketCodecs.optional(BlockPos.PACKET_CODEC);
    PacketCodec<ByteBuf, Set<String>> STRING_SET = PacketCodecs.STRING.collect(PacketCodecs.toCollection(HashSet::new));

    Function<Class<?>, PacketCodec<ByteBuf, ?>> ENUM_CODEC_CACHE = Util.memoize(type -> {
        final Object[] values = type.getEnumConstants();
        return PacketCodecs.indexed(i -> values[i], i -> ((Enum<?>)i).ordinal());
    });

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> PacketCodec<ByteBuf, T> ofEnum(Class<T> type) {
        return (PacketCodec<ByteBuf, T>)ENUM_CODEC_CACHE.apply(type);
    }

    static <B extends ByteBuf, K, V> PacketCodec.ResultFunction<B, V, Map<K, V>> toMap(Function<V, K> keyFunction) {
        return codec -> map(HashMap::new, codec, keyFunction, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, C extends Map<K, V>> PacketCodec<B, C> map(
            IntFunction<C> factory,
            PacketCodec<? super B, V> elementCodec,
            Function<V, K> keyFunction,
            int maxSize
        ) {
            return new PacketCodec<>() {
                @Override
                public C decode(B byteBuf) {
                    int i = PacketCodecs.readCollectionSize(byteBuf, maxSize);
                    C collection = factory.apply(Math.min(i, 65536));

                    for (int j = 0; j < i; j++) {
                        V v = elementCodec.decode(byteBuf);
                        K k = keyFunction.apply(v);
                        collection.put(k, v);
                    }

                    return collection;
                }

                @Override
                public void encode(B byteBuf, C collection) {
                    PacketCodecs.writeCollectionSize(byteBuf, collection.size(), maxSize);

                    for (V object : collection.values()) {
                        elementCodec.encode(byteBuf, object);
                    }
                }
            };
        }
}
