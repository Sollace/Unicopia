package com.minelittlepony.unicopia.mixin;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.toxin.Toxic;
import com.minelittlepony.unicopia.toxin.ToxicHolder;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

@Mixin(BlockItem.class)
abstract class MixinBlockItem extends Item implements ToxicHolder {
    public MixinBlockItem() {super(null); }

    @Nullable
    private Toxic toxic;

    @Override
    public void setToxic(Toxic toxic) {
        this.toxic = toxic;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (toxic != null) {
            return toxic.getUseAction(stack);
        }
        return super.getUseAction(stack);
    }

    @Inject(method = "appendTooltip", at = @At("RETURN"))
    private void onAppendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo into) {
        if (toxic != null) {
            tooltip.add(toxic.getTooltip(stack));
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        ItemStack result = super.finishUsing(stack, world, entity);
        if (toxic != null) {
            return toxic.finishUsing(stack, world, entity);
        }
        return result;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (toxic == null) {
            return super.use(world, player, hand);
        }
        return toxic.use(world, player, hand, () -> super.use(world, player, hand));
    }
}
