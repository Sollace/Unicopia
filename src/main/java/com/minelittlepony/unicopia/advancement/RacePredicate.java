package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.network.ServerPlayerEntity;

public record RacePredicate(Optional<Set<Race>> include, Optional<Set<Race>> exclude) implements Predicate<ServerPlayerEntity> {
    public static final RacePredicate EMPTY = new RacePredicate(Optional.empty(), Optional.empty());

    private static final Codec<Set<Race>> RACE_SET_CODEC = CodecUtils.setOf(Race.REGISTRY.getCodec());
    private static final Codec<RacePredicate> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RACE_SET_CODEC.optionalFieldOf("include").forGetter(RacePredicate::include),
            RACE_SET_CODEC.optionalFieldOf("exclude").forGetter(RacePredicate::exclude)
    ).apply(instance, RacePredicate::of));
    public static final Codec<RacePredicate> CODEC = CodecUtils.xor(BASE_CODEC, RACE_SET_CODEC.xmap(include -> of(Optional.of(include), Optional.empty()), a -> a.include().orElse(Set.of())));

    public static RacePredicate of(Set<Race> include, Set<Race> exclude) {
        return of(Optional.of(include).filter(s -> !s.isEmpty()), Optional.of(exclude).filter(s -> !s.isEmpty()));
    }

    public static RacePredicate of(Optional<Set<Race>> include, Optional<Set<Race>> exclude) {
        if (include.isEmpty() && exclude.isEmpty()) {
            return EMPTY;
        }
        return new RacePredicate(include, exclude);
    }

    @Override
    public boolean test(ServerPlayerEntity player) {
        Race race = Pony.of(player).getSpecies();
        return (include.isEmpty() || include.get().contains(race)) && !(!exclude.isEmpty() && exclude.get().contains(race));
    }

    public boolean isEmpty() {
        return include.isEmpty() && exclude.isEmpty();
    }
}
