package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.FriendlyCreeperEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

/**
 * Ability to hug mobs. Not all of them are receptive to your advances though, so be careful!
 */
public class HugAbility extends CarryAbility {

    @Override
    public boolean canUse(Race race) {
        return race.canUseEarth();
    }

    @Override
    public boolean apply(Pony pony, Hit data) {
        PlayerEntity player = pony.asEntity();
        LivingEntity rider = findRider(player, pony.asWorld());

        dropAllPassengers(player);

        pony.setAnimation(Animation.ARMS_FORWARD, Animation.Recipient.ANYONE);

        if (rider instanceof CreeperEntity creeper) {
            FriendlyCreeperEntity friendlyCreeper = creeper.convertTo(UEntities.FRIENDLY_CREEPER, true);
            player.getWorld().spawnEntity(friendlyCreeper);

            friendlyCreeper.startRiding(player, true);
            Living.getOrEmpty(friendlyCreeper).ifPresent(living -> living.setCarrier(player));
        } else if (rider instanceof FriendlyCreeperEntity creeper) {
            creeper.startRiding(player, true);
            Living.getOrEmpty(creeper).ifPresent(living -> living.setCarrier(player));
        } else if (rider != null) {
            rider.teleport(player.getX(), player.getY() + 0.5, player.getZ());
            rider.setYaw(player.getYaw() + 180);

            if (rider instanceof FriendlyCreeperEntity) {
                pony.spawnParticles(ParticleTypes.HEART, 10);
            }
        }

        Living.transmitPassengers(player);
        return true;
    }
}
