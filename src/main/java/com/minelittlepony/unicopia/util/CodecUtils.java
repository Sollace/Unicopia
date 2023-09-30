package com.minelittlepony.unicopia.util;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;

public interface CodecUtils {
    /**
     * Combines the result of two unrelated codecs into a single object.
     * <p>
     * The first codec serves as the "base" whilst the second codec serves as an additional field to merge into
     * that object when serializing. Deserializing produces a pair with the parent value and the extra value.
     * <p>
     * Recommended usage:
     * <code>
     * Codec<MyObject> CODEC = CodecUtils.extend(SOME_CODEC, MY_FIELD_CODEC.fieldOf("my_extra_field")).xmap(
     *      pair -> new MyObject(pair.getLeft(), pair.getRight()),
     *      myObject -> Pair.of(myObject.parent(), myObject.myExtraField());
     * </code>
     * <p>
     * Json:
     * <code>
     * {
     *   "something": "something",
     *   "something_else": 1,
     *
     *   "my_extra_field": "HAH EAT THAT CODECS"
     * }
     * </code>
     * @param <A> The base type
     * @param <B> Type of the field to append
     * @param baseCodec   Codec for the base type
     * @param fieldCodec  Codec for the appended field
     * @return A codec for serializing objects of the base with the extra field inserted
     */
    static <A, B> Codec<Pair<Optional<A>, Optional<B>>> extend(Codec<A> baseCodec, MapCodec<B> fieldCodec) {
        return Codec.of(new Encoder<Pair<Optional<A>, Optional<B>>>() {
            @Override
            public <T> DataResult<T> encode(Pair<Optional<A>, Optional<B>> input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(input.getFirst().get(), ops, prefix)
                        .flatMap(leftResult -> input.getSecond()
                            .map(r -> fieldCodec.encode(r, ops, ops.mapBuilder()).build(prefix))
                            .orElse(DataResult.success(leftResult)));
            }
        }, new Decoder<Pair<Optional<A>, Optional<B>>>() {
            @Override
            public <T> DataResult<Pair<Pair<Optional<A>, Optional<B>>, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(new Pair<>(new Pair<>(
                        baseCodec.decode(ops, input).map(Pair::getFirst).result(),
                        fieldCodec.decode(ops, ops.getMap(input).result().get()).result()
                ), input));
            }
        });
    }
}
