package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.*;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

public interface StatePredicate extends Predicate<BlockState> {
    Codec<StatePredicate> CODEC = Codecs.JSON_ELEMENT.flatXmap(
            json -> DataResult.success(of(json)),
            predicate -> DataResult.error(() -> "Cannot serialize a predicate")
    );

    StatePredicate FALSE = state -> false;

    default Optional<StateChange> getInverse() {
        return Optional.empty();
    }

    static Optional<StateChange> getInverse(Predicate<BlockState> predicate) {
        if (predicate instanceof StatePredicate p) {
            return p.getInverse();
        }
        return Optional.empty();
    }

    static StatePredicate of(JsonElement json) {
        List<Predicate<BlockState>> predicates = new ArrayList<>();

        if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(element -> predicates.add(of(element)));
            if (predicates.isEmpty()) {
                return FALSE;
            }
            return state -> predicates.stream().anyMatch(pred -> pred.test(state));
        }

        JsonObject o = json.getAsJsonObject();
        if (o.has("state")) {
            predicates.add(ofState(JsonHelper.getString(o, "state")));
        }
        if (o.has("tag")) {
            Optional.of(JsonHelper.getString(o, "tag")).map(s -> TagKey.of(RegistryKeys.BLOCK, Identifier.of(s))).ifPresent(tag -> {
                predicates.add(new StatePredicate() {
                    @Override
                    public Optional<StateChange> getInverse() {
                        final Optional<StatePredicate> self = Optional.of(this);
                        return Optional.of(new StateChange() {
                            @Override
                            public Optional<StatePredicate> getInverse() {
                                return self;
                            }

                            @Override
                            public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                                return Registries.BLOCK.getOptional(tag)
                                        .flatMap(i -> i.getRandom(world.random))
                                        .map(RegistryEntry::value)
                                        .map(Block::getDefaultState)
                                        .orElse(state);
                            }

                            @Override
                            public Serializer<?> getSerializer() {
                                return null;
                            }
                        });
                    }

                    @Override
                    public boolean test(BlockState state) {
                        return state.isIn(tag);
                    }
                });
            });
        }
        if (o.has("builtin")) {
            predicates.add(ofBuiltIn(JsonHelper.getString(o, "builtin")));
        }

        if (predicates.isEmpty()) {
            return FALSE;
        }

        if (predicates.size() == 1) {
            return state -> predicates.get(0).test(state);
        }

        return allOf(predicates);
    }

    private static StatePredicate allOf(List<Predicate<BlockState>> predicates) {
        return state -> {
            return predicates.isEmpty() || predicates.stream().allMatch(p -> p.test(state));
        };
    }

    private static Predicate<BlockState> ofBuiltIn(String type) {
        switch (type) {
            case "plants": return StatePredicate::isPlant;
            case "ores": return StatePredicate::isOre;
            case "water": return StatePredicate::isWater;
            case "lava": return StatePredicate::isLava;
            default: throw new IllegalArgumentException("Invalid builtin type: " + type);
        }
    }

    static boolean isPlant(BlockState s) {
        return s.getBlock() instanceof PlantBlock;
    }

    static boolean isOre(BlockState s) {
        return s.isIn(ConventionalBlockTags.ORES);
    }

    static boolean isWater(BlockState s) {
        return isFluid(s) && s.getFluidState().isIn(FluidTags.WATER);
    }

    static boolean isLava(BlockState s) {
        return isFluid(s) && s.getFluidState().isIn(FluidTags.LAVA);
    }

    @SuppressWarnings("deprecation")
    static boolean isFluid(BlockState s) {
        return s.isLiquid();
    }

    static Predicate<BlockState> ofState(String state) {
        Identifier id = Identifier.of(state.split("\\{")[0]);
        List<PropertyOp> properties = Optional.of(state)
                .filter(s -> s.contains("{"))
                .stream()
                .flatMap(s -> Stream.of(s.split("\\{")[1].split("\\}")[0].split(",")))
                .map(PropertyOp::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (properties.isEmpty()) {
            return new StatePredicate() {
                @Override
                public Optional<StateChange> getInverse() {
                    final Optional<StatePredicate> self = Optional.of(this);
                    return Optional.of(new StateChange() {
                        @Override
                        public Optional<StatePredicate> getInverse() {
                            return self;
                        }

                        @Override
                        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                            return Registries.BLOCK.getOptionalValue(id).map(Block::getDefaultState).orElse(state);
                        }

                        @Override
                        public Serializer<?> getSerializer() {
                            return null;
                        }
                    });
                }

                @Override
                public boolean test(BlockState state) {
                    return Registries.BLOCK.getOptionalValue(id).filter(state::isOf).isPresent();
                }
            };
        }

        return new StatePredicate() {
            @Override
            public Optional<StateChange> getInverse() {
                final Optional<StatePredicate> self = Optional.of(this);
                return Optional.of(new StateChange() {
                    @Override
                    public Optional<StatePredicate> getInverse() {
                        return self;
                    }

                    @Override
                    public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                        return Registries.BLOCK.getOptionalValue(id).map(Block::getDefaultState).map(newState -> {
                            for (PropertyOp prop : properties) {
                                newState = prop.applyTo(world, newState);
                            }
                            return newState;
                        }).orElse(state);
                    }

                    @Override
                    public Serializer<?> getSerializer() {
                        return null;
                    }
                });
            }

            @Override
            public boolean test(BlockState state) {
                return Registries.BLOCK.getOptionalValue(id).filter(state::isOf).isPresent() && properties.stream().allMatch(p -> p.test(state));
            }
        };
    }

    record PropertyOp (String name, String value, Comparison op) implements Predicate<BlockState> {
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

        public BlockState applyTo(World world, BlockState state) {
            return getProperty(state, name).flatMap(property -> {
                return property.parse(value).map(val -> {
                    return applyValidValue(world, property, val, state);
                });
            }).orElse(state);
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

        enum Comparison implements IntPredicate {
            LESS {
                @Override
                public boolean test(int value) {
                    return value < 0;
                }
            },
            GREATER {
                @Override
                public boolean test(int value) {
                    return value > 0;
                }
            },
            EQUAL {
                @Override
                public boolean test(int value) {
                    return value == 0;
                }
            };

            @Override
            public abstract boolean test(int value);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<T>> Optional<Property<T>> getProperty(BlockState state, String name) {
        return (Optional<Property<T>>)(Object)state.getProperties().stream()
                .filter(property -> property.getName().contentEquals(name))
                .findFirst();
    }
}
