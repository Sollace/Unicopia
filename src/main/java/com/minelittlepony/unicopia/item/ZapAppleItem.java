package com.minelittlepony.unicopia.item;


import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ZapAppleItem extends AppleItem {

    public ZapAppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        HitResult mop = VecHelper.getObjectMouseOver(player, 5, 0);

        if (mop != null && mop.getType() == HitResult.Type.ENTITY) {
            ItemStack stack = player.getStackInHand(hand);

            EntityHitResult ehr = ((EntityHitResult)mop);

            if (canFeedTo(stack, ehr.getEntity())) {
                return onFedTo(stack, player, ehr.getEntity());
            }
        }

        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity player) {
        stack = super.finishUsing(stack, w, player);

        player.damage(MagicalDamageSource.ZAP_APPLE, 120);

        if (w instanceof ServerWorld) {
            ((ServerWorld) w).addLightning(new LightningEntity(w, player.getX(), player.getY(), player.getZ(), false));
        }

        return stack;
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof VillagerEntity
                || e instanceof CreeperEntity
                || e instanceof PigEntity;
    }

    public TypedActionResult<ItemStack> onFedTo(ItemStack stack, PlayerEntity player, Entity e) {
        e.onStruckByLightning(new LightningEntity(e.world, e.getX(), e.getY(), e.getZ(), false));

        if (!player.abilities.creativeMode) {
            stack.decrement(1);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public void appendStacks(ItemGroup tab, DefaultedList<ItemStack> items) {
        super.appendStacks(tab, items);
        if (isIn(tab)) {
            UTags.APPLES.values().forEach(item -> {
                if (item != this) {
                    ItemStack stack = new ItemStack(this);
                    stack.getOrCreateTag().putString("appearance", Registry.ITEM.getId(item).toString());
                    items.add(stack);
                }
            });
        }
    }

    public Item getAppearance(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("appearance")) {
            return Registry.ITEM.get(new Identifier(stack.getTag().getString("appearance")));
        }

        return Items.AIR;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Item appearance = getAppearance(stack);
        return appearance == Items.AIR ? super.getTranslationKey() : appearance.getTranslationKey(stack);
    }

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return getAppearance(stack) == Items.AIR ? Toxicity.SEVERE : Toxicity.SAFE;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (getAppearance(stack) == Items.AIR) {
            return Rarity.EPIC;
        }

        return Rarity.RARE;
    }
}
