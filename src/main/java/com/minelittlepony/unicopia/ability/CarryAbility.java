package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

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
    public Hit tryActivate(Pony player) {
        return Hit.INSTANCE;
    }

    protected LivingEntity findRider(PlayerEntity player, World w) {
        return TraceHelper.<LivingEntity>findEntity(player, 10, 1, hit -> {
            return hit instanceof LivingEntity && !player.isConnectedThroughVehicle(hit) && !(hit instanceof IPickupImmuned);
        }).orElse(null);
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type) {

        if (type == ActivationType.TAP && player.getPhysics().isFlying()) {
            player.getPhysics().dashForward((float)player.getReferenceWorld().random.nextTriangular(1, 0.3F));
            return true;
        }

        return false;
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.asEntity();
        LivingEntity rider = findRider(player, iplayer.getReferenceWorld());

        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        if (rider != null) {
            rider.startRiding(player, true);
        }

        Living.transmitPassengers(player);
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }

    public interface IPickupImmuned {

    }
}
