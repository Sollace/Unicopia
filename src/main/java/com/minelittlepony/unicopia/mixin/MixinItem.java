package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder {

    @Nullable
    private FoodComponent originalFoodComponent;

    @Override
    @Accessor("foodComponent")
    public abstract void setFood(FoodComponent food);

    @Override
    public Optional<Toxic> getToxic() {
        if (originalFoodComponent == null) {
            originalFoodComponent = ((Item)(Object)this).getFoodComponent();
        }
        setFood(originalFoodComponent);

        Optional<Toxic> toxic = Toxics.REGISTRY.stream().filter(i -> i.matches((Item)(Object)this)).map(t -> {
            t.getFoodComponent().ifPresent(this::setFood);
            return t;
        }).findFirst();

        if (!toxic.isPresent() && ((Item)(Object)this).getFoodComponent() != null) {
            return Optional.of(Toxics.EDIBLE);
        }
        return toxic;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> info) {
        if (getToxic().isPresent()) {
            getToxic().get().finishUsing(stack, world, entity);
        }
    }
}
