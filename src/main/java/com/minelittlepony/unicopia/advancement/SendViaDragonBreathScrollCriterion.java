package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;

public class SendViaDragonBreathScrollCriterion extends AbstractRepeatingCriterion<SendViaDragonBreathScrollCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void triggerSent(PlayerEntity player, ItemStack payload, String recipient, BiConsumer<String, Integer> counterCallback) {
        if (player instanceof ServerPlayerEntity spe) {
            trigger(spe, c -> c.test(spe, payload, recipient, false), (count, c) -> {
                c.counter.ifPresent(counter -> counterCallback.accept(counter, count));
                return true;
            });
        }
    }

    public void triggerReceived(LivingEntity recipient, ItemStack payload) {
        if (recipient instanceof ServerPlayerEntity spe) {
            trigger(spe, c -> c.test(spe, payload, recipient.getDisplayName().getString(), true), (count, c) -> true);
        }
    }

    public record Conditions (
            Optional<LootContextPredicate> player,
            Optional<ItemPredicate> item,
            boolean isReceivingEnd,
            Optional<String> recipientName,
            TriState recipientPresent,
            Optional<String> counter,
            RacePredicate races
    ) implements AbstractRepeatingCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item),
                Codec.BOOL.optionalFieldOf("is_receiving_end", false).forGetter(Conditions::isReceivingEnd),
                Codec.STRING.optionalFieldOf("recipient_name").forGetter(Conditions::recipientName),
                CodecUtils.tristateOf("recipient_present").forGetter(Conditions::recipientPresent),
                Codec.STRING.optionalFieldOf("counter").forGetter(Conditions::counter),
                RacePredicate.CODEC.optionalFieldOf("races", RacePredicate.EMPTY).forGetter(Conditions::races)
            ).apply(instance, Conditions::new));
        public boolean test(ServerPlayerEntity player, ItemStack payload, String recipient, boolean receiving) {
            return isReceivingEnd == receiving
                    && races.test(player)
                    && (item.isEmpty() || item.get().test(payload))
                    && recipientName.map(expectedRecipientname -> recipient.equalsIgnoreCase(expectedRecipientname)).orElse(true)
                    && (recipientPresent == TriState.DEFAULT || isRecipientAbsent(player.getServerWorld(), recipient) != recipientPresent.get());
        }

        private boolean isRecipientAbsent(ServerWorld world, String recipient) {
            return world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), e -> e.hasCustomName() && e.getCustomName().getString().equalsIgnoreCase(recipient)).isEmpty();
        }
    }
}
