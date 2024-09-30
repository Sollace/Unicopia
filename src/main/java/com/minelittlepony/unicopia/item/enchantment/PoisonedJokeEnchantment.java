package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.Enchantments;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;

public class PoisonedJokeEnchantment {

    public void onUserTick(Living<?> user, int level) {
        if (user.asWorld().isClient) {
            return;
        }

        int light = user.asWorld().getLightLevel(user.asEntity().getRootVehicle().getBlockPos());
        Random rng = user.asWorld().random;
        Enchantments.Data data = user.getEnchants().computeIfAbsent(UEnchantments.POISONED_JOKE, Enchantments.Data::new);

        data.level -= rng.nextFloat() * 0.8F;
        if (rng.nextInt(Math.max(1, (light * 9) + (int)data.level)) == 0) {
            data.level = rng.nextInt(5000);

            RegistryUtils.pickRandom(user.asWorld(), UTags.Sounds.POISON_JOKE_EVENTS).ifPresent(event -> {
                user.asWorld().playSoundFromEntity(
                        null,
                        user.asEntity(),
                        event, SoundCategory.HOSTILE,
                        0.5F + rng.nextFloat() * 0.5F,
                        0.5F + rng.nextFloat() * 0.5F
                );
            });
        }
    }
}
