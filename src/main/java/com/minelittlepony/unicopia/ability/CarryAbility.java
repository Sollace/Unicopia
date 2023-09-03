package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Pegasi ability to pick up and carry other players
 */
public class CarryAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canFly();
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    protected LivingEntity findRider(PlayerEntity player, World w) {
        return TraceHelper.<LivingEntity>findEntity(player, 10, 1, hit -> {
            return EquinePredicates.VALID_LIVING_AND_NOT_MAGIC_IMMUNE.test(hit) && !player.isConnectedThroughVehicle(hit);
        }).orElse(null);
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Hit> data) {

        if (type == ActivationType.TAP && player.getPhysics().isFlying()) {
            player.getPhysics().dashForward((float)player.asWorld().random.nextTriangular(1, 0.3F));
            return true;
        }

        return false;
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.asEntity();
        LivingEntity rider = findRider(player, iplayer.asWorld());

        if (player.hasPassengers()) {
            List<Entity> passengers = StreamSupport.stream(player.getPassengersDeep().spliterator(), false).toList();
            player.removeAllPassengers();
            for (Entity passenger : passengers) {
                passenger.refreshPositionAfterTeleport(player.getPos());
                Living<?> l = Living.living(passenger);
                if (l != null) {
                    l.setCarrier((UUID)null);
                }
            }
        }

        if (rider != null) {
            rider.startRiding(player, true);
            Living.getOrEmpty(rider).ifPresent(living -> living.setCarrier(player));
        }

        Living.transmitPassengers(player);
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
