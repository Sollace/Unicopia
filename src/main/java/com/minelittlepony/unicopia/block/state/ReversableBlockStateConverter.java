package com.minelittlepony.unicopia.block.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

public interface ReversableBlockStateConverter extends BlockStateConverter {
    ReversableBlockStateConverter getInverse();

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private final List<ReversableBlockStateConverterImpl.Entry> entries = new ArrayList<>();

        private Builder() {}

        public Builder add(StatePredicate match, ReversableStateChange stateChange) {
            entries.add(new ReversableBlockStateConverterImpl.Entry(match, stateChange, Optional.empty()));
            return this;
        }

        public Builder add(StatePredicate match, ReversableStateChange stateChange, StatePredicate inverseMatch, ReversableStateChange inverseStateChange) {
            entries.add(new ReversableBlockStateConverterImpl.Entry(match, stateChange, Optional.of(new ReversableBlockStateConverterImpl.Entry(inverseMatch, inverseStateChange, Optional.empty()))));
            return this;
        }

        public Builder apply(Function<Builder, Builder> function) {
            return function.apply(this);
        }

        public ReversableBlockStateConverter build() {
            return new ReversableBlockStateConverterImpl(entries, null);
        }

        public DataResult<JsonElement> encode() {
            return ReversableBlockStateConverterImpl.CODEC.encodeStart(JsonOps.INSTANCE, new ReversableBlockStateConverterImpl(entries, null));
        }
    }
}
