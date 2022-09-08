package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicates;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.*;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;

public abstract class StatePredicate implements Predicate<BlockState> {

    public abstract StateChange getInverse();

    @Override
    public abstract boolean test(BlockState state);

    public static Optional<StateChange> getInverse(Predicate<BlockState> predicate) {
        if (predicate instanceof StatePredicate p) {
            return Optional.of(p.getInverse());
        }
        return Optional.empty();
    }

    public static Predicate<BlockState> of(JsonElement json) {

        List<Predicate<BlockState>> predicates = new ArrayList<>();

        if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(element -> predicates.add(of(element)));
            if (predicates.isEmpty()) {
                return Predicates.alwaysFalse();
            }
            return state -> predicates.stream().anyMatch(pred -> pred.test(state));
        }

        JsonObject o = json.getAsJsonObject();
        if (o.has("state")) {
            predicates.add(ofState(JsonHelper.getString(o, "state")));
        }
        if (o.has("tag")) {
            Optional.of(JsonHelper.getString(o, "tag")).map(s -> TagKey.of(Registry.BLOCK_KEY, new Identifier(s))).ifPresent(tag -> {
                predicates.add(new StatePredicate() {
                    @Override
                    public StateChange getInverse() {
                        final Optional<Predicate<BlockState>> self = Optional.of(this);
                        return new StateChange() {
                            @Override
                            public Optional<Predicate<BlockState>> getInverse() {
                                return self;
                            }

                            @Override
                            public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                                return Registry.BLOCK.getOrCreateEntryList(tag)
                                        .getRandom(world.random)
                                        .map(RegistryEntry::value)
                                        .map(Block::getDefaultState)
                                        .orElse(state);
                            }
                        };
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
            return Predicates.alwaysFalse();
        }

        if (predicates.size() == 1) {
            return predicates.get(0);
        }

        return allOf(predicates);
    }

    private static Predicate<BlockState> allOf(List<Predicate<BlockState>> predicates) {
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

    public static boolean isPlant(BlockState s) {
        return s.getBlock() instanceof PlantBlock;
    }

    static boolean isOre(BlockState s) {
        return s.getBlock() instanceof OreBlock;
    }

    static boolean isWater(BlockState s) {
        return s.getMaterial() == Material.WATER;
    }

    static boolean isLava(BlockState s) {
        return s.getMaterial() == Material.LAVA;
    }

    public static Predicate<BlockState> ofState(String state) {
        Identifier id = new Identifier(state.split("\\{")[0]);
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
                public StateChange getInverse() {
                    final Optional<Predicate<BlockState>> self = Optional.of(this);
                    return new StateChange() {
                        @Override
                        public Optional<Predicate<BlockState>> getInverse() {
                            return self;
                        }

                        @Override
                        public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                            return Registry.BLOCK.getOrEmpty(id).map(Block::getDefaultState).orElse(state);
                        }
                    };
                }

                @Override
                public boolean test(BlockState state) {
                    return Registry.BLOCK.getOrEmpty(id).filter(state::isOf).isPresent();
                }
            };
        }

        return new StatePredicate() {
            @Override
            public StateChange getInverse() {
                final Optional<Predicate<BlockState>> self = Optional.of(this);
                return new StateChange() {
                    @Override
                    public Optional<Predicate<BlockState>> getInverse() {
                        return self;
                    }

                    @Override
                    public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                        return Registry.BLOCK.getOrEmpty(id).map(Block::getDefaultState).map(newState -> {
                            for (PropertyOp prop : properties) {
                                newState = prop.applyTo(world, newState);
                            }
                            return newState;
                        }).orElse(state);
                    }
                };
            }

            @Override
            public boolean test(BlockState state) {
                return Registry.BLOCK.getOrEmpty(id).filter(state::isOf).isPresent() && properties.stream().allMatch(p -> p.test(state));
            }
        };
    }

    private record PropertyOp (String name, String value, Comparison op) implements Predicate<BlockState> {
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
    public static <T extends Comparable<T>> Optional<Property<T>> getProperty(BlockState state, String name) {
        return (Optional<Property<T>>)(Object)state.getProperties().stream()
                .filter(property -> property.getName().contentEquals(name))
                .findFirst();
    }
}
