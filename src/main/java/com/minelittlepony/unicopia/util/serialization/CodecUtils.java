package com.minelittlepony.unicopia.util.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface CodecUtils {
    Codec<ItemConvertible> ITEM = Registries.ITEM.getCodec().xmap(i -> () -> i, ItemConvertible::asItem);
    Codec<Optional<BlockPos>> OPTIONAL_POS = Codecs.optional(BlockPos.CODEC);
    Codec<Vec3d> VECTOR = Codec.withAlternative(RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::getZ)
    ).apply(instance, Vec3d::new)), Vec3d.CODEC);
    Codec<Optional<Vec3d>> OPTIONAL_VECTOR = Codecs.optional(VECTOR);
    Codec<Optional<UUID>> OPTIONAL_UUID = Codecs.optional(Uuids.CODEC);

    double MAX_HOR_AXIS = 3.0000512E7;
    double MAX_VER_AXIS = 2.0E7;
    Codec<Vec3d> POSITION_VECTOR = Vec3d.CODEC.xmap(vec -> new Vec3d(
            MathHelper.clamp(vec.x, -MAX_HOR_AXIS, MAX_HOR_AXIS),
            MathHelper.clamp(vec.y, -MAX_VER_AXIS, MAX_VER_AXIS),
            MathHelper.clamp(vec.z, -MAX_HOR_AXIS, MAX_HOR_AXIS)
    ), Function.identity());

    static <A, B> Codec<Map<A, B>> toMutable(Codec<Map<A, B>> codec) {
        return codec.xmap(map -> new HashMap<>(map), Function.identity());
    }

    static <V> MapCodec<V> dispatched(Function<V, String> typeGetter, Map<String, Codec<? extends V>> typeLookup) {
        return new MapCodec<>() {
            @Override
            public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
                var entries = input.entries().filter(entry -> typeLookup.containsKey(ops.getStringValue(entry.getFirst()).getOrThrow())).toList();
                if (entries.size() != 1) {
                    return DataResult.error(() -> "Map only have one key. Instead found " + entries.size() + " in " + input);
                }

                return typeLookup.get(entries.get(0).getFirst()).decode(ops, entries.get(0).getSecond()).map(pair -> pair.getFirst());
            }

            @Override
            public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                var type = typeGetter.apply(Untyped.cast(input));
                prefix.add(ops.createString(type), typeLookup.get(type).encodeStart(ops, Untyped.cast(input)));
                return prefix;
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return typeLookup.keySet().stream().map(ops::createString);
            }
        };
    }

    static <K> Codec<K> apply(Codec<K> codec, UnaryOperator<Codec<K>> func) {
        return func.apply(codec);
    }

    static <K> Codec<Set<K>> setOf(Codec<K> codec) {
        return codec.listOf().xmap(
                l -> l.stream().distinct().collect(Collectors.toUnmodifiableSet()),
                s -> new ArrayList<>(s)
        );
    }

    static <K> Codec<Supplier<K>> supplierOf(Codec<K> codec) {
        return codec.xmap(k -> () -> k, Supplier::get);
    }

    static MapCodec<TriState> tristateOf(String fieldName) {
        return Codec.BOOL.optionalFieldOf(fieldName).xmap(
                b -> b.map(TriState::of).orElse(TriState.DEFAULT),
                t -> Optional.ofNullable(t.get())
        );
    }

    static <K, V> Codec<Map.Entry<K, V>> mapEntryOf(MapCodec<K> keyCodec, MapCodec<V> valueCodec) {
        return RecordCodecBuilder.create(i -> i.group(
                keyCodec.forGetter(Map.Entry::getKey),
                valueCodec.fieldOf("target").forGetter(Map.Entry::getValue)
        ).apply(i, Map::entry));
    }

    static <K, V> Codec<Map<K, V>> mapOf(Codec<Map.Entry<K, V>> entryCodec) {
        return setOf(entryCodec).xmap(
                set -> set.stream().collect(entriesToMap()),
                Map::entrySet
        );
    }

    static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entriesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    static <T> Codec<DefaultedList<T>> defaultedList(Codec<T> elementCodec, T empty) {
        return elementCodec.listOf().xmap(
                elements -> new DefaultedList<>(elements, empty) {},
                Function.identity()
        );
    }
}
