package com.minelittlepony.unicopia.world.item;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.Dispensable;
import com.minelittlepony.unicopia.world.TossableItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RottenTomatoItem extends TomatoItem implements TossableItem {

    public RottenTomatoItem(Settings settings) {
        super(settings);
        Dispensable.setDispenseable(this, this::dispenseStack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);

        if (canBeThrown(itemstack) && !player.canConsume(false)) {
            toss(world, itemstack, player);

            return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
        }

        return super.use(world, player, hand);
    }

    protected boolean isSickening(ItemStack stack, PlayerEntity player) {
        return canBeThrown(stack)
                && !Pony.of(player).getSpecies().canUseEarth();
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity && isSickening(stack, (PlayerEntity)entity)) {
            int duration = 7000;

            StatusEffectInstance effect = entity.getStatusEffect(StatusEffects.NAUSEA);

            if (effect != null) {
                duration += Math.max(0, effect.getDuration());
            }

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, 4));

        }
        return entity.eatFood(world, stack);
    }

    @Override
    public void onImpact(Caster<?> caster, BlockPos pos, BlockState state) {
        if (caster.isLocal() && state.getMaterial() == Material.GLASS) {
            caster.getWorld().breakBlock(pos, true);
        }
    }
}
