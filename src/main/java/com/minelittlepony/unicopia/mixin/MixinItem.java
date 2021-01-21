package com.minelittlepony.unicopia.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder {

    private Optional<Toxic> toxic = Optional.empty();

    @Override
    @Accessor("foodComponent")
    public abstract void setFood(FoodComponent food);

    @Override
    public void setToxic(Toxic toxic) {
        this.toxic = Optional.of(toxic);
    }

    @Override
    public Optional<Toxic> getToxic() {
        return toxic;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> info) {
        if (getToxic().isPresent()) {
            getToxic().get().finishUsing(stack, world, entity);
        }
    }
}
