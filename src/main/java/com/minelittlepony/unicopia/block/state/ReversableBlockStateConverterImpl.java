package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;

class ReversableBlockStateConverterImpl implements ReversableBlockStateConverter {
    public static final Codec<ReversableBlockStateConverter> CODEC = Entry.CODEC.listOf().xmap(
            entries -> new ReversableBlockStateConverterImpl(entries, null),
            converter -> ((ReversableBlockStateConverterImpl)converter).entries
    );

    private final List<Entry> entries;

    private final Supplier<ReversableBlockStateConverter> inverse;

    ReversableBlockStateConverterImpl(List<Entry> entries, @Nullable ReversableBlockStateConverter inverse) {
        this.entries = entries;
        this.inverse = inverse == null ? Suppliers.memoize(() -> new ReversableBlockStateConverterImpl(entries.stream().flatMap(entry -> entry.getInverse().stream()).toList(), this)) : Suppliers.ofInstance(inverse);
    }

    @Override
    public boolean canConvert(@Nullable BlockState state) {
        return entries.stream().anyMatch(entry -> entry.canConvert(state));
    }

    @Override
    public Optional<@NotNull BlockState> getConverted(World world, @NotNull BlockState state) {
        return entries.stream()
                .filter(entry -> entry.canConvert(state))
                .findFirst()
                .flatMap(entry -> entry.getConverted(world, state));
    }

    @Override
    public ReversableBlockStateConverter getInverse() {
        return inverse.get();
    }

    record Entry (
            StatePredicate match,
            ReversableStateChange stateChange,
            Optional<Entry> inverse
    ) {
        private static final Codec<Entry> INVERSE_CODEC = RecordCodecBuilder.create(i -> i.group(
                StatePredicate.CODEC.fieldOf("match").forGetter(Entry::match),
                ReversableStateChange.CODEC.fieldOf("apply").forGetter(Entry::stateChange)
        ).apply(i, (match, change) -> new Entry(match, change, Optional.empty())));
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                StatePredicate.CODEC.fieldOf("match").forGetter(Entry::match),
                ReversableStateChange.CODEC.fieldOf("apply").forGetter(Entry::stateChange),
                INVERSE_CODEC.optionalFieldOf("inverse").forGetter(Entry::inverse)
        ).apply(i, Entry::new));

        public boolean canConvert(@Nullable BlockState state) {
            return state != null && match.test(state);
        }

        public Optional<@NotNull BlockState> getConverted(World world, @NotNull BlockState state) {
            return stateChange.getConverted(world, state);
        }

        @Nullable
        public Optional<Entry> getInverse() {
            return inverse.or(() -> {
                return stateChange.getInverse()
                    .flatMap(invertedMatch -> invertedMatch.getInverse()
                        .map(invertedStateChange -> new Entry(invertedMatch, invertedStateChange, Optional.of(this))));
            });
        }
    }
}
