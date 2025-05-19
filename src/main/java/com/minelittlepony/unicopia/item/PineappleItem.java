package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

public class PineappleItem extends Item {
    public PineappleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        if (food != null) {
            user.eatFood(world, stack.copy(), food);
            if (!world.isClient) {
                stack.damage(1, (ServerWorld)world, user instanceof ServerPlayerEntity p ? p : null, i -> {
                    AwaitTickQueue.scheduleTask(world, w -> {
                        w.playSoundFromEntity(null, user, USounds.Vanilla.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1, 0.7F);
                    });
                });

                if (world.random.nextInt(20) == 0) {
                    RegistryUtils.pickRandomEntry(world, UTags.StatusEffects.PINEAPPLE_EFFECTS, e -> !user.hasStatusEffect(e)).ifPresent(effect -> {
                        user.addStatusEffect(new StatusEffectInstance(effect, 10, 1));
                    });
                }
            }
        }
        return stack;
    }
}
