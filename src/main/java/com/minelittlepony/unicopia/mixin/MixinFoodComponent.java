package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(FoodComponent.class)
abstract class MixinFoodComponent {
    @Inject(method = "onConsume", at = @At("TAIL"))
    public void onOnConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable, CallbackInfo info) {
        Pony.of(user).ifPresent(pony -> pony.onEat(stack, stack.get(DataComponentTypes.FOOD)));
    }
}
