package com.minelittlepony.unicopia.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomEventCriterion extends AbstractRepeatingCriterion<CustomEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public CustomEventCriterion.Trigger createTrigger(String name) {
        return player -> {
            if (player instanceof ServerPlayerEntity p) {
                trigger(p,
                    condition -> condition.event().equalsIgnoreCase(name),
                    (count, condition) -> condition.test(count, p)
                );
            }
        };
    }

    public interface Trigger {
        void trigger(@Nullable Entity player);
    }

    public static AdvancementCriterion<?> create(String name) {
        return UCriteria.CUSTOM_EVENT.create(new Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.DEFAULT, 1));
    }

    public static AdvancementCriterion<?> create(String name, int count) {
        return UCriteria.CUSTOM_EVENT.create(new Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.DEFAULT, count));
    }

    public static AdvancementCriterion<?> createFlying(String name) {
        return UCriteria.CUSTOM_EVENT.create(new Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.TRUE, 1));
    }

    public static AdvancementCriterion<?> createFlying(String name, int count) {
        return UCriteria.CUSTOM_EVENT.create(new Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.TRUE, count));
    }

    public record Conditions (
            Optional<LootContextPredicate> player,
            String event,
            RacePredicate races,
            TriState flying,
            int repeatCount) implements AbstractRepeatingCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.fieldOf("event").forGetter(Conditions::event),
                RacePredicate.CODEC.optionalFieldOf("races", RacePredicate.EMPTY).forGetter(Conditions::races),
                CodecUtils.tristateOf("flying").forGetter(Conditions::flying),
                Codec.INT.optionalFieldOf("repeatCount", 0).forGetter(Conditions::repeatCount)
            ).apply(instance, Conditions::new));

        public boolean test(int count, ServerPlayerEntity player) {
            boolean isFlying = Pony.of(player).getPhysics().isFlying();
            return races.test(player)
                    && flying.orElse(isFlying) == isFlying
                    && (repeatCount < 0 || repeatCount <= count);
        }
    }
}
