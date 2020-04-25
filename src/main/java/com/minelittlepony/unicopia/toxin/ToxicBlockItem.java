package com.minelittlepony.unicopia.toxin;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ToxicBlockItem extends BlockItem {

    private final UseAction action;
    private final Toxicity toxicity;
    private final Toxin toxin;

    public ToxicBlockItem(Block block, Item.Settings settings, int hunger, float saturation, UseAction action, Toxicity toxicity, Toxin toxin) {
        super(block, settings
                .group(ItemGroup.FOOD)
                .food(new FoodComponent.Builder()
                        .hunger(hunger)
                        .saturationModifier(saturation)
                        .build()));
        this.toxicity = toxicity;
        this.action = action;
        this.toxin = toxin;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return action;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(toxicity.getTooltip());
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        super.finishUsing(stack, world, entity);

        if (entity instanceof PlayerEntity) {
            Race race = Pony.of((PlayerEntity)entity).getSpecies();
            Toxicity toxicity = (race.isDefault() || race == Race.CHANGELING) ? Toxicity.LETHAL : this.toxicity;

            toxin.addSecondaryEffects((PlayerEntity)entity, toxicity, stack);
        }

        return new ItemStack(getRecipeRemainder());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Race race = Pony.of(player).getSpecies();

        if (race.isDefault() || race == Race.CHANGELING) {
            return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
        }

        return super.use(world, player, hand);
    }
}
