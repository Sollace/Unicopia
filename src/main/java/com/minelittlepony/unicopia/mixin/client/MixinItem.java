package com.minelittlepony.unicopia.mixin.client;

import java.util.List;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(Item.class)
abstract class MixinItem implements ToxicHolder {
    @Inject(method = "appendTooltip", at = @At("RETURN"))
    private void onAppendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo into) {
        getToxic().ifPresent(t -> tooltip.add(t.getTooltip(stack)));
    }
}
