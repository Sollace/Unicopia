package com.minelittlepony.unicopia.util.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemConvertible;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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

    @Deprecated
    Codec<BlockState> BLOCK_STATE_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(BlockState input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(ops.createString(BlockArgumentParser.stringifyBlockState(input)));
        }

        @Override
        public <T> DataResult<Pair<BlockState, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).flatMap(pair -> {
                try {
                    return DataResult.success(new Pair<>(BlockArgumentParser.block(Registries.BLOCK, pair, false).blockState(), input));
                } catch (CommandSyntaxException e) {
                    return DataResult.error(() -> e.getMessage());
                }
            });
        }

        public String toString() {
            return Codec.STRING + "[BlockArgumentParsed]";
        }
    };

    static <A, B, C> PacketCodec<A, C> flatXmap(PacketCodec<A, B> codec, BiFunction<A, B, C> to, BiFunction<A, C, B> from) {
        return new PacketCodec<>() {
            @Override
            public C decode(A buf) {
                return to.apply(buf, codec.decode(buf));
            }

            @Override
            public void encode(A buf, C value) {
                codec.encode(buf, from.apply(buf, value));
            }
        };
    }

    static <A, B> Codec<Map<A, B>> toMutable(Codec<Map<A, B>> codec) {
        return codec.xmap(map -> new HashMap<>(map), Function.identity());
    }

    static Codec<Identifier> identifierWithDefaultnamespace(String defaultNamespace) {
        return Codec.STRING.comapFlatMap(
                s -> s.indexOf(':') != -1 ? Identifier.validate(s) : DataResult.success(Identifier.of(defaultNamespace, s)),
                id -> id.getNamespace().contentEquals(defaultNamespace) ? id.getPath() : id.toString()
        );
    }

    static <T, E> Codec<E> fuzzy(
            Codec<T> typeCodec,
            Supplier<Stream<T>> types,
            Function<E, T> elementToType,
            Function<T, Codec<? extends E>> typeToElementCodec) {
        return new Codec<>() {
            @Override
            public <V> DataResult<V> encode(E input, DynamicOps<V> ops, V prefix) {
                return typeToElementCodec.apply(elementToType.apply(input)).encode(Untyped.cast(input), ops, prefix);
            }

            @Override
            public <V> DataResult<Pair<E, V>> decode(DynamicOps<V> ops, V input) {
                List<String> errors = new ArrayList<>();
                return types.get().map(type -> Untyped.<Object, DataResult<Pair<E, V>>>cast(typeToElementCodec.apply(type).decode(ops, input)).ifError(error -> errors.add(error.message())))
                        .filter(result -> result.isSuccess())
                        .findFirst()
                        .orElseGet(() -> DataResult.error(() -> "Dispatched could not find matching codec. Errors: [" + String.join(";", errors) + "]"));
            }

            @Override
            public String toString() {
                return "FuzzyCodec[type=" + typeCodec + "]";
            }
        };
    }

    static <V, Type> MapCodec<V> mapFuzzy(
            Codec<Type> typeCodec,
            Supplier<Stream<Type>> types,
            Function<V, Type> valueToTypeFunction,
            Function<Type, Codec<? extends V>> elementCodecGetter) {
        return new MapCodec<>() {
            @Override
            public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
                var entries = input.entries()
                        .map(entry -> new Pair<>(typeCodec.decode(ops, entry.getFirst()).result().map(Pair::getFirst).orElse(null), entry.getSecond()))
                        .filter(entry -> entry.getFirst() != null)
                        .toList();
                if (entries.size() != 1) {
                    return DataResult.error(() -> "May only have one key. Instead found " + entries.size() + " in " + input);
                }

                return elementCodecGetter.apply(entries.getFirst().getFirst())
                        .decode(ops, entries.getFirst().getSecond())
                        .map(Pair::getFirst);
            }

            @Override
            public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                var type = valueToTypeFunction.apply(Untyped.cast(input));
                prefix.add(typeCodec.encodeStart(ops, type).getOrThrow(), elementCodecGetter.apply(type).encodeStart(ops, Untyped.cast(input)));
                return prefix;
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return types.get().map(type -> typeCodec.encodeStart(ops, type).getOrThrow());
            }

            public String toString() {
                return "FuzzyMapCodec[type=" + typeCodec + "]";
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
