package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class TraitDiscoveredCriterion extends AbstractCriterion<TraitDiscoveredCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public static AdvancementCriterion<?> create(Set<Trait> traits) {
        return UCriteria.TRAIT_DISCOVERED.create(new Conditions(Optional.empty(), traits));
    }

    public void trigger(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            trigger((ServerPlayerEntity)player, c -> c.test((ServerPlayerEntity)player));
        }
    }

    public record Conditions(Optional<LootContextPredicate> player, Set<Trait> traits)  implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Trait.SET_CODEC.fieldOf("traits").forGetter(Conditions::traits)
            ).apply(instance, Conditions::new));

        public boolean test(ServerPlayerEntity player) {
            return Pony.of(player).getDiscoveries().isKnown(traits);
        }
    }
}
