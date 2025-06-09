package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public interface StateChange {
    Map<Identifier, Serializer<?>> SERIALIZERS = new HashMap<>();

    Codec<Serializer<?>> SERIALIZER_CODEC = Identifier.CODEC.flatComapMap(
        id -> SERIALIZERS.get(id),
        serializer -> DataResult.success(serializer.id())
    );
    Codec<StateChange> CODEC = SERIALIZER_CODEC.dispatch("action", StateChange::getSerializer, Serializer::codec);

    Serializer<SetStateChange> SET_STATE = register("set_state", SetStateChange.CODEC);
    Serializer<SetPropertyChange> SET_PROPERTY = register("set_property", SetPropertyChange.CODEC);
    Serializer<CyclePropertyChange> CYCLE_PROPERTY = register("cycle_property", CyclePropertyChange.CODEC);

    record Serializer<T extends StateChange>(Identifier id, MapCodec<T> codec) {}
    record SetStateChange(String state, float chance) implements StateChange {
        static final MapCodec<SetStateChange> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("state").forGetter(SetStateChange::state),
                Codec.FLOAT.optionalFieldOf("chance", -1F).forGetter(SetStateChange::chance)
        ).apply(i, SetStateChange::new));

        @Override
        public Optional<StatePredicate> getInverse() {
            final Optional<StateChange> self = Optional.of(this);
            final Predicate<BlockState> test = StatePredicate.ofState(state);
            return Optional.of(new StatePredicate() {
                @Override
                public Optional<StateChange> getInverse() {
                    return self;
                }

                @Override
                public boolean test(BlockState state) {
                    return test.test(state);
                }
            });
        }

        @Override
        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
            if (chance > 0 && world.random.nextFloat() > chance) {
                return state;
            }
            return Registries.BLOCK.getOptionalValue(Identifier.of(this.state)).map(Block::getDefaultState)
                    .map(newState -> StateUtil.copyState(state, newState))
                    .orElse(state);
        }

        @Override
        public Serializer<SetStateChange> getSerializer() {
            return SET_STATE;
        }
    }

    record SetPropertyChange(String property, String value, float chance) implements StateChange {
        static final MapCodec<SetPropertyChange> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("property").forGetter(SetPropertyChange::property),
                Codec.STRING.fieldOf("value").forGetter(SetPropertyChange::value),
                Codec.FLOAT.optionalFieldOf("chance", -1F).forGetter(SetPropertyChange::chance)
        ).apply(i, SetPropertyChange::new));

        @Override
        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
            if (chance > 0 && world.random.nextFloat() > chance) {
                return state;
            }
            return StatePredicate.getProperty(state, property).flatMap(property -> {
                return property.parse(value).map(v -> state.with(property, v));
            }).orElse(state);
        }

        @Override
        public Serializer<SetPropertyChange> getSerializer() {
            return SET_PROPERTY;
        }
    }

    record CyclePropertyChange(String property, float chance) implements StateChange {
        static final MapCodec<CyclePropertyChange> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("property").forGetter(CyclePropertyChange::property),
                Codec.FLOAT.optionalFieldOf("chance", -1F).forGetter(CyclePropertyChange::chance)
        ).apply(i, CyclePropertyChange::new));

        @Override
        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
            if (chance > 0 && world.random.nextFloat() > chance) {
                return state;
            }
            return StatePredicate.getProperty(state, property).map(property -> state.cycle(property)).orElse(state);
        }

        @Override
        public Serializer<CyclePropertyChange> getSerializer() {
            return CYCLE_PROPERTY;
        }
    }

    static <T extends StateChange> Serializer<T> register(String name, MapCodec<T> codec) {
        var serializer = new Serializer<>(Unicopia.id(name), codec);
        SERIALIZERS.put(serializer.id(), serializer);
        return serializer;
    }

    default Optional<StatePredicate> getInverse() {
        return Optional.empty();
    }

    @NotNull BlockState getConverted(World world, @NotNull BlockState state);

    Serializer<?> getSerializer();
}
