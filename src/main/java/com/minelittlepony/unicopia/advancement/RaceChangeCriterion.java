package com.minelittlepony.unicopia.advancement;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class RaceChangeCriterion extends AbstractCriterion<RaceChangeCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            trigger((ServerPlayerEntity)player, c -> c.test((ServerPlayerEntity)player));
        }
    }

    public record Conditions (
            Optional<LootContextPredicate> player,
            Race race
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Race.REGISTRY.getCodec().fieldOf("race").forGetter(Conditions::race)
            ).apply(instance, Conditions::new));

        public boolean test(ServerPlayerEntity player) {
            return Pony.of(player).getCompositeRace().includes(race);
        }
    }
}
