package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

public interface StatePredicate extends Predicate<BlockState> {
    Codec<StatePredicate> TYPE_CODEC = Identifier.CODEC.dispatch(StatePredicate::getType, type -> StatePredicate.REGISTRY.get(type));
    Codec<StatePredicate> CODEC = Codec.xor(Codec.lazyInitialized(() -> Union.CODEC), TYPE_CODEC).xmap(
            Either::unwrap,
            predicate -> predicate instanceof Union union ? Either.left(union) : Either.right(predicate)
    );
    Map<Identifier, MapCodec<? extends StatePredicate>> REGISTRY = Map.of(
        FluidTag.ID, FluidTag.MAP_CODEC,
        Tag.ID, Tag.MAP_CODEC,
        State.ID, State.MAP_CODEC,
        Union.ID, Union.MAP_CODEC,
        PropertyOp.ID, PropertyOp.MAP_CODEC,
        Plants.ID, Plants.MAP_CODEC
    );

    static void bootstrap() {
        ReversableStateChange.bootstrap();
    }

    @SuppressWarnings("deprecation")
    static boolean isFluid(BlockState state) {
        return state.isLiquid();
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<T>> Optional<Property<T>> getProperty(BlockState state, String name) {
        return (Optional<Property<T>>)(Object)state.getProperties().stream()
                .filter(property -> property.getName().contentEquals(name))
                .findFirst();
    }

    default Optional<ReversableStateChange> getInverse() {
        return Optional.empty();
    }

    Identifier getType();

    record Plants() implements StatePredicate {
        public static final Identifier ID = Unicopia.id("is_plant");
        public static final Plants INSTANCE = new Plants();
        public static final MapCodec<Plants> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public boolean test(BlockState state) {
            return state.getBlock() instanceof PlantBlock;
        }
    }

    record FluidTag(TagKey<Fluid> tag) implements StatePredicate {
        public static final Identifier ID = Unicopia.id("fluid_tag");
        public static final Codec<FluidTag> CODEC = TagKey.codec(RegistryKeys.FLUID).xmap(FluidTag::new, FluidTag::tag);
        public static final MapCodec<FluidTag> MAP_CODEC = CODEC.fieldOf("tag");

        public FluidTag(String tag) {
            this(TagKey.of(RegistryKeys.FLUID, Identifier.of(tag)));
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public boolean test(BlockState state) {
            return isFluid(state) && state.getFluidState().isIn(tag);
        }
    }

    record Tag(TagKey<Block> tag, Optional<ReversableStateChange> inverse) implements StatePredicate {
        public static final Identifier ID = Unicopia.id("block_tag");
        public static final Codec<Tag> CODEC = TagKey.codec(RegistryKeys.BLOCK).xmap(Tag::new, Tag::tag);
        public static final MapCodec<Tag> MAP_CODEC = CODEC.fieldOf("tag");

        public Tag(String tag) {
            this(TagKey.of(RegistryKeys.BLOCK, Identifier.of(tag)), Optional.empty());
        }

        public Tag(TagKey<Block> tag) {
            this(tag, Optional.empty());
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public boolean test(BlockState state) {
            return state.isIn(tag);
        }

        @Override
        public Optional<ReversableStateChange> getInverse() {
            return inverse.or(() -> Optional.of(new ReversableStateChange.SetRandomState(this)));
        }
    }

    record State(Identifier id, List<PropertyOp> properties, Optional<ReversableStateChange> inverse) implements StatePredicate {
        public static final Identifier ID = Unicopia.id("block_state");
        public static final Codec<State> CODEC = Codec.STRING.xmap(State::new, state -> state.id.toString()
                    + (state.properties.isEmpty() ? "" : "{"
                            + state.properties.stream().map(PropertyOp::toString).collect(Collectors.joining(","))
                    + "}"));
        public static final MapCodec<State> MAP_CODEC = CODEC.fieldOf("state");

        public State(Identifier id, List<PropertyOp> properties) {
            this(id, properties, Optional.empty());
        }

        public State(String state) {
            this(Identifier.of(state.split("\\{")[0]), Optional.of(state)
                .filter(s -> s.contains("{"))
                .stream()
                .flatMap(s -> Stream.of(s.split("\\{")[1].split("\\}")[0].split(",")))
                .map(PropertyOp::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList(), Optional.empty());
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public boolean test(BlockState state) {
            return Registries.BLOCK.getOptionalValue(id).filter(state::isOf).isPresent()
                    && (properties.isEmpty() || properties.stream().allMatch(p -> p.test(state)));
        }

        public BlockState applyTo(World world, BlockState state) {
            for (PropertyOp prop : properties) {
                state = prop.applyTo(world, state);
            }
            return state;
        }

        @Override
        public Optional<ReversableStateChange> getInverse() {
            return inverse.or(() -> Optional.of(new ReversableStateChange.SetState(this)));
        }
    }

    record Union(List<StatePredicate> predicates, Combiner combiner) implements StatePredicate {
        public static final Identifier ID = Unicopia.id("union");
        public static final MapCodec<Union> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                StatePredicate.CODEC.listOf().fieldOf("predicates").forGetter(Union::predicates),
                Combiner.CODEC.fieldOf("combiner").forGetter(Union::combiner)
        ).apply(i, Union::new));
        public static final Codec<Union> CODEC = StatePredicate.CODEC.listOf().xmap(predicates -> new Union(predicates, Combiner.OR), Union::predicates);

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public boolean test(BlockState state) {
            return combiner.func.test(predicates.stream(), predicate -> predicate.test(state));
        }

        public enum Combiner implements StringIdentifiable {
            AND(Stream::allMatch),
            OR(Stream::anyMatch);

            public static final Codec<Combiner> CODEC = StringIdentifiable.createCodec(Combiner::values);

            private final String name = name().toLowerCase(Locale.ROOT);
            private final BiPredicate<Stream<StatePredicate>, Predicate<StatePredicate>> func;

            Combiner(BiPredicate<Stream<StatePredicate>, Predicate<StatePredicate>> func) {
                this.func = func;
            }

            @Override
            public String asString() {
                return name;
            }
        }
    }

    record PropertyOp (String name, String value, Comparison op) implements StatePredicate {
        public static final Identifier ID = Unicopia.id("block_state_property");
        public static final Codec<PropertyOp> CODEC = Codec.STRING.flatXmap(pattern -> {
            String[] splitten = pattern.split("[=<>]", 2);
            if (pattern.indexOf('=') == splitten[0].length()) {
                return DataResult.success(new PropertyOp(splitten[0], splitten[1], Comparison.EQUAL));
            }
            if (pattern.indexOf('<') == splitten[0].length()) {
                return DataResult.success(new PropertyOp(splitten[0], splitten[1], Comparison.LESS));
            }
            if (pattern.indexOf('>') == splitten[0].length()) {
                return DataResult.success(new PropertyOp(splitten[0], splitten[1], Comparison.GREATER));
            }
            return DataResult.error(() -> "Invalid pattern: " + pattern);
        }, property -> DataResult.success(property.toString()));
        public static final MapCodec<PropertyOp> MAP_CODEC = CODEC.fieldOf("property");

        public static Optional<PropertyOp> of(String pattern) {
            String[] splitten = pattern.split("[=<>]", 2);
            if (pattern.indexOf('=') == splitten[0].length()) {
                return Optional.of(new PropertyOp(splitten[0], splitten[1], Comparison.EQUAL));
            }
            if (pattern.indexOf('<') == splitten[0].length()) {
                return Optional.of(new PropertyOp(splitten[0], splitten[1], Comparison.LESS));
            }
            if (pattern.indexOf('>') == splitten[0].length()) {
                return Optional.of(new PropertyOp(splitten[0], splitten[1], Comparison.GREATER));
            }
            return Optional.empty();
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        public BlockState applyTo(World world, BlockState state) {
            return getProperty(state, name).flatMap(property -> {
                return property.parse(value).map(val -> {
                    return applyValidValue(world, property, val, state);
                });
            }).orElse(state);
        }

        @Override
        public String toString() {
            return name + op.getSymbol() + value;
        }

        private <T extends Comparable<T>> BlockState applyValidValue(World world, Property<T> property, T allowedValue, BlockState state) {
            if (op == Comparison.EQUAL) {
                return state.with(property, allowedValue);
            }
            if (op == Comparison.GREATER) {
                var allowedValues = property.getValues().stream().filter(v -> op.test(v.compareTo(allowedValue))).toList();
                if (!allowedValues.contains(state.get(property))) {
                    int index = world.random.nextInt(allowedValues.size());
                    return state.with(property, allowedValues.remove(index));
                }
            }
            return state;
        }

        @Override
        public boolean test(BlockState state) {
            return getProperty(state, name)
                    .flatMap(property -> property.parse(value)
                            .filter(v -> op.test(state.get(property).compareTo(v))))
                    .isPresent();
        }

        public enum Comparison implements IntPredicate, StringIdentifiable {
            LESS('<') {
                @Override
                public boolean test(int value) {
                    return value < 0;
                }
            },
            GREATER('>') {
                @Override
                public boolean test(int value) {
                    return value > 0;
                }
            },
            EQUAL('=') {
                @Override
                public boolean test(int value) {
                    return value == 0;
                }
            };

            public static final Codec<Comparison> CODEC = StringIdentifiable.createCodec(Comparison::values);

            private final String name = name().toLowerCase(Locale.ROOT);
            private final char symbol;

            Comparison(char symbol) {
                this.symbol = symbol;
            }

            public char getSymbol() {
                return symbol;
            }

            @Override
            public String asString() {
                return name;
            }

            @Override
            public abstract boolean test(int value);
        }
    }
}
