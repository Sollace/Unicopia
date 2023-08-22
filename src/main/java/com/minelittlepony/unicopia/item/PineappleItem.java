package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

public class PineappleItem extends Item {
    public PineappleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (isFood()) {
            user.eatFood(world, stack.copy());
            if (!world.isClient) {
                stack.damage(1, user, u -> {
                    AwaitTickQueue.scheduleTask(u.getWorld(), w -> {
                        w.playSoundFromEntity(null, u, USounds.Vanilla.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1, 0.7F);
                    });
                });

                if (world.random.nextInt(20) == 0) {
                    RegistryUtils.pickRandom(world, UTags.PINEAPPLE_EFFECTS, e -> !user.hasStatusEffect(e)).ifPresent(effect -> {
                        user.addStatusEffect(new StatusEffectInstance(effect, 10, 1));
                    });
                }
            }
        }
        return stack;
    }
}
