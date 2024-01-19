package com.minelittlepony.unicopia.block.state;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public abstract class StateChange {
    private static final Map<Identifier, Function<JsonObject, StateChange>> SERIALIZERS = new HashMap<>();

    static {
        SERIALIZERS.put(Unicopia.id("set_state"), json -> {
            final String sstate = JsonHelper.getString(json, "state");
            final Identifier id = new Identifier(sstate);
            final float chance = JsonHelper.getFloat(json, "chance", -1);

            return new StateChange() {
                @Override
                public Optional<Predicate<BlockState>> getInverse() {
                    final StateChange self = this;
                    final Predicate<BlockState> test = StatePredicate.ofState(sstate);
                    return Optional.of(new StatePredicate() {
                        @Override
                        public StateChange getInverse() {
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
                    return Registries.BLOCK.getOrEmpty(id).map(Block::getDefaultState)
                            .map(newState -> StateUtil.copyState(state, newState))
                            .orElse(state);
                }
            };
        });
        SERIALIZERS.put(Unicopia.id("set_property"), json -> {
            final String name = JsonHelper.getString(json, "property");
            final String value = json.get("value").getAsString();
            final float chance = JsonHelper.getFloat(json, "chance", -1);

            return new StateChange() {
                @Override
                public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                    if (chance > 0 && world.random.nextFloat() > chance) {
                        return state;
                    }
                    return StatePredicate.getProperty(state, name).flatMap(property -> {
                        return property.parse(value).map(v -> state.with(property, v));
                    }).orElse(state);
                }

            };
        });
        SERIALIZERS.put(Unicopia.id("cycle_property"), json -> {
            final String name = JsonHelper.getString(json, "property");
            final float chance = JsonHelper.getFloat(json, "chance", -1);

            return new StateChange() {
                @Override
                public @NotNull BlockState getConverted(World world, @NotNull BlockState state) {
                    if (chance > 0 && world.random.nextFloat() > chance) {
                        return state;
                    }
                    return StatePredicate.getProperty(state, name).map(property -> state.cycle(property)).orElse(state);
                }
            };
        });
    }

    public Optional<Predicate<BlockState>> getInverse() {
        return Optional.empty();
    }

    public abstract @NotNull BlockState getConverted(World world, @NotNull BlockState state);

    public static StateChange fromJson(JsonObject json) {
        String action = JsonHelper.getString(json, "action");
        return Optional.of(SERIALIZERS.get(new Identifier(action))).map(serializer -> {
            return serializer.apply(json);
        }).orElseThrow(() -> new IllegalArgumentException("Invalid action " + action));
    }
}
