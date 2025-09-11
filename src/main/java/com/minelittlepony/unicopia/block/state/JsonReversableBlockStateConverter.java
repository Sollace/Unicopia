package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;

public class JsonReversableBlockStateConverter implements ReversableBlockStateConverter {
    @SuppressWarnings("unchecked")
    public static final Codec<JsonReversableBlockStateConverter> CODEC = Entry.CODEC.listOf().xmap(
            entries -> new JsonReversableBlockStateConverter(entries, null),
            c -> (List<Entry>)c.entries
    );

    private final List<? extends BlockStateConverter> entries;

    private final Supplier<ReversableBlockStateConverter> inverse;

    public JsonReversableBlockStateConverter(List<? extends BlockStateConverter> entries, @Nullable ReversableBlockStateConverter inverse) {
        this.inverse = inverse == null ? Suppliers.memoize(() -> new JsonReversableBlockStateConverter(entries.stream()
                    .filter(entry -> entry instanceof ReversableBlockStateConverter)
                    .map(entry -> ((ReversableBlockStateConverter)entry).getInverse())
                    .filter(Objects::nonNull)
                    .toList(), this)) : () -> inverse;
        this.entries = entries;
    }

    @Override
    public BlockStateConverter getInverse() {
        return inverse.get();
    }

    @Override
    public boolean canConvert(@Nullable BlockState state) {
        return entries.stream().anyMatch(entry -> entry.canConvert(state));
    }

    @Override
    public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
        return entries.stream().filter(entry -> entry.canConvert(state))
                .findFirst()
                .map(entry -> entry.getConverted(world, state))
                .orElse(state);
    }

    public record Entry (
            StatePredicate match,
            StateChange stateChange,
            Optional<Entry> inverse
    ) implements ReversableBlockStateConverter {
        public static final MapCodec<Entry> UN_REVERSABLE_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                StatePredicate.CODEC.fieldOf("match").forGetter(Entry::match),
                StateChange.CODEC.fieldOf("apply").forGetter(Entry::stateChange)
        ).apply(i, (match, stateChange) -> new Entry(match, stateChange, Optional.empty())));
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                StatePredicate.CODEC.fieldOf("match").forGetter(Entry::match),
                StateChange.CODEC.fieldOf("apply").forGetter(Entry::stateChange),
                UN_REVERSABLE_CODEC.codec().optionalFieldOf("inverse").forGetter(Entry::inverse)
        ).apply(i, Entry::new));

        @Override
        public boolean canConvert(@Nullable BlockState state) {
            return state != null && match.test(state);
        }

        @Override
        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
            return stateChange.getConverted(world, state);
        }

        @Nullable
        @Override
        public BlockStateConverter getInverse() {
            return inverse.orElseGet(() -> {
                return stateChange.getInverse()
                    .flatMap(invertedMatch -> StatePredicate.getInverse(match)
                        .map(invertedStateChange -> new Entry(invertedMatch, invertedStateChange, Optional.of(this))))
                    .orElse(null);
            });
        }
    }
}
