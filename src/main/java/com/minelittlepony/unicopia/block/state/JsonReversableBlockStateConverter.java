package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.*;

import net.minecraft.block.BlockState;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class JsonReversableBlockStateConverter implements ReversableBlockStateConverter {

    private final List<BlockStateConverter> entries;

    @Nullable
    private ReversableBlockStateConverter inverse;

    public JsonReversableBlockStateConverter(JsonElement json) {
        this(new ArrayList<>(), null);
        json.getAsJsonArray().forEach(entry -> {
            entries.add(Entry.of(entry.getAsJsonObject(), true));
        });
    }

    public JsonReversableBlockStateConverter(List<BlockStateConverter> entries, @Nullable ReversableBlockStateConverter inverse) {
        this.inverse = inverse;
        this.entries = entries;
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

    @Override
    public BlockStateConverter getInverse() {
        if (inverse == null) {
            inverse = new JsonReversableBlockStateConverter(entries.stream()
                    .filter(entry -> entry instanceof ReversableBlockStateConverter)
                    .map(entry -> ((ReversableBlockStateConverter)entry).getInverse())
                    .filter(Objects::nonNull)
                    .toList(), this);
        }
        return inverse;
    }

    record Entry (
            Predicate<BlockState> match,
            StateChange stateChange,
            Optional<Entry> inverse
    ) implements ReversableBlockStateConverter {
        public static Entry of(JsonObject json, boolean allowInversion) {
            return new Entry(
                StatePredicate.of(json.get("match")),
                StateChange.fromJson(JsonHelper.getObject(json, "apply")),
                allowInversion && json.has("inverse") ? Optional.of(of(JsonHelper.getObject(json, "inverse"), false)) : Optional.empty()
            );
        }

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
