package com.minelittlepony.unicopia.item.consumables;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UEffects;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class EdibleItem extends Item implements IEdible {

    private final Toxicity toxicity;

    public EdibleItem(Item.Settings settings, Toxicity toxicity) {
        super(settings);
        this.toxicity = toxicity;
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return toxicity;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getToxicityLevel(stack).getTooltip());
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        super.finishUsing(stack, world, entity);

        if (entity instanceof PlayerEntity) {
            Race race = SpeciesList.instance().getPlayer((PlayerEntity)entity).getSpecies();
            Toxicity toxicity = (race.isDefault() || race == Race.CHANGELING) ? Toxicity.LETHAL : getToxicityLevel(stack);

            addSecondaryEffects((PlayerEntity)entity, toxicity, stack);
        }

        return new ItemStack(getRecipeRemainder());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Race race = SpeciesList.instance().getPlayer(player).getSpecies();

        if (race.isDefault() || race == Race.CHANGELING) {
            return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
        }

        return super.use(world, player, hand);
    }

    @Override
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {

        if (toxicity.toxicWhenRaw()) {
            player.addPotionEffect(toxicity.getPoisonEffect());
        }

        if (toxicity.isLethal()) {
            player.addPotionEffect(new StatusEffectInstance(UEffects.FOOD_POISONING, 300, 7, false, false));
        } else if (toxicity.toxicWhenCooked()) {
            player.addPotionEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 3, 1, false, false));
        }
    }
}
