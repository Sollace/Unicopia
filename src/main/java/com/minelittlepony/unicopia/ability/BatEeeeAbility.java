package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;

/**
 * An ability to screeeeeeeeEeEeEeeee!
 */
public class BatEeeeAbility extends ScreechAbility {
    public static final int SELF_SPOOK_PROBABILITY = 20000;

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    protected void playSounds(Pony player, Random rng, float strength) {
        int count = 1 + rng.nextInt(10) + (int)(strength * 10);

        for (int i = 0; i < count; i++) {
            player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                    (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                    1.6F + (rng.nextFloat() - 0.5F)
            );
        }
        player.asWorld().emitGameEvent(player.asEntity(), GameEvent.ENTITY_ACTION, player.asEntity().getEyePos());
        for (int j = 0; j < (int)(strength * 2); j++) {
            for (int k = 0; k < count; k++) {
                AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
                    player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                            (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                            1.6F + (rng.nextFloat() - 0.5F)
                    );
                    player.asWorld().emitGameEvent(player.asEntity(), GameEvent.ENTITY_ACTION, player.asEntity().getEyePos());
                }, rng.nextInt(3));
            }
        }

        if (strength > 0.5F && rng.nextInt(SELF_SPOOK_PROBABILITY) == 0) {
            player.asEntity().damage(player.damageOf(UDamageTypes.BAT_SCREECH, player), 0.1F);
            UCriteria.SCREECH_SELF.trigger(player.asEntity());
        }
    }
}
