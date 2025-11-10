package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.data.Numeric;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.Trace;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Hand;

/**
 * Unicorn telekinesis ability
 */
public class UnicornTelekinesisAbility implements Ability<Numeric> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 3;
    }

    @Override
    public int getColor(Pony player) {
        return SpellType.PORTAL.getColor();
    }

    @Override
    public Optional<Numeric> prepare(Pony player) {

        if (!player.canCast()) {
            return Optional.empty();
        }

        int maxDistance = (int)((player.asEntity().isCreative() ? 1000 : 100) + (player.getLevel().get() * 0.25F));

        Trace trace = Trace.create(player.asEntity(), maxDistance, 1, hit -> (EquinePredicates.VALID_LIVING_AND_NOT_MAGIC_IMMUNE.test(hit) || hit instanceof ItemEntity) && !player.asEntity().isConnectedThroughVehicle(hit));

        return trace.getEntityResult().map(e -> e.getEntity().getBlockPos()).map(p -> Numeric.valueOf(1)).or(() -> {
            ItemStack stack = player.asEntity().getStackInHand(Hand.MAIN_HAND);

            return stack.isEmpty() ? Optional.empty() : Numeric.of(0);
        });
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, Numeric> getSerializer() {
        return Numeric.CODEC;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Numeric> data) {
        return (type == ActivationType.TAP || type == ActivationType.DOUBLE_TAP) && (player.isClient() || data.filter(d -> apply(player, d)).isPresent());
    }

    @Override
    public boolean acceptsQuickAction(Pony player, ActivationType type) {
        return type == ActivationType.NONE || type == ActivationType.TAP || type == ActivationType.DOUBLE_TAP;
    }

    @Override
    public Optional<Numeric> prepareQuickAction(Pony player, ActivationType type) {
        return type == ActivationType.DOUBLE_TAP ? Numeric.of(2) : type == ActivationType.TAP ? prepare(player) : Optional.empty();
    }

    @Override
    public boolean apply(Pony player, Numeric data) {

        if (data.type() == 0) {
            ItemStack stack = player.asEntity().getStackInHand(Hand.MAIN_HAND);
            if (!stack.isEmpty()) {
                player.asEntity().setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                return player.getLevitatingItems().addStack(stack);
            }
            return true;
        }
        if (data.type() == 1) {
            int maxDistance = (int)((player.asEntity().isCreative() ? 1000 : 100) + (player.getLevel().get() * 0.25F));

            Trace trace = Trace.create(player.asEntity(), maxDistance, 1, hit -> (EquinePredicates.VALID_LIVING_AND_NOT_MAGIC_IMMUNE.test(hit) || hit instanceof ItemEntity) && !player.asEntity().isConnectedThroughVehicle(hit));

            return trace.getEntity().filter(entity -> player.getLevitatingItems().addPassenger(entity)).isPresent();
        }
        if (data.type() == 2) {
            player.getLevitatingItems().dropEverything();
            player.sendUpdatePacket();
            return true;
        }

        return false;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
