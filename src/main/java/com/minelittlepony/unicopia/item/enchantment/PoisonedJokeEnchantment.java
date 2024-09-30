package com.minelittlepony.unicopia.item.enchantment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registries;

public class PoisonedJokeEnchantment extends SimpleEnchantment {

    protected PoisonedJokeEnchantment(Options options) {
        super(options);
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (user.asWorld().isClient) {
            return;
        }

        int light = user.asWorld().getLightLevel(user.asEntity().getRootVehicle().getBlockPos());
        Random rng = user.asWorld().random;
        Data data = user.getEnchants().computeIfAbsent(this, Data::new);

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
