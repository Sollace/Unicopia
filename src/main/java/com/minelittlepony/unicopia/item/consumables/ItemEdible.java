package com.minelittlepony.unicopia.item.consumables;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UEffects;

import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public abstract class ItemEdible extends Item implements IEdible {

    private UseAction useAction = UseAction.EAT;

    public ItemEdible(Item.Settings settings) {
        super(settings);
    }

    public Item setUseAction(UseAction action) {
        useAction = action;

        return this;
    }

    public UseAction getItemUseAction(ItemStack stack) {
        return useAction;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, PlayerEntity player) {
        Race race = SpeciesList.instance().getPlayer(player).getSpecies();
        Toxicity toxicity = (race.isDefault() || race == Race.CHANGELING) ? Toxicity.LETHAL : getToxicityLevel(stack);

        addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getToxicityLevel(stack).getTooltip());
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity)entity : null;

        if (player != null) {
            player.getHungerManager().eat(this, stack);

            world.playSound(null, player.x, player.y, player.z, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);

            onFoodEaten(stack, world, player);

            // replaced "this" with "stack.getItem()"
            player.addStat(StatList.getObjectUseStats(stack.getItem()));

            if (player instanceof ServerPlayerEntity) {
                Criterions.CONSUME_ITEM.handle((ServerPlayerEntity)player, stack);
            }
        }

        if (player == null || !player.abilities.creativeMode) {
            stack.decrement(1);
        }

        ItemStack container = getContainerItem(stack);

        if (!container.isEmpty() && player != null && !player.abilities.creativeMode) {
            if (stack.isEmpty()) {
                return getContainerItem(stack);
            }

            player.inventory.addItemStackToInventory(getContainerItem(stack));
        }

        return stack;
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
            player.addStatusEffectInstance(toxicity.getPoisonEffect());
        }

        if (toxicity.isLethal()) {
            player.addStatusEffectInstance(new StatusEffectInstance(UEffects.FOOD_POISONING, 300, 7, false, false));
        } else if (toxicity.toxicWhenCooked()) {
            player.addStatusEffectInstance(new StatusEffectInstance(StatusEffects.NAUSEA, 3, 1, false, false));
        }
    }
}
