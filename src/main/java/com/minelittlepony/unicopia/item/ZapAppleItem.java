package com.minelittlepony.unicopia.item;


import java.util.Optional;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.toxin.Toxicity;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ZapAppleItem extends AppleItem implements ChameleonItem {

    public ZapAppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        Optional<Entity> entity = RayTraceHelper.doTrace(player, 5, 1, EntityPredicates.EXCEPT_SPECTATOR.and(e -> canFeedTo(stack, e))).getEntity();

        if (entity.isPresent()) {
            return onFedTo(stack, player, entity.get());
        }

        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity player) {
        stack = super.finishUsing(stack, w, player);

        player.damage(MagicalDamageSource.ZAP_APPLE, 120);

        if (w instanceof ServerWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(w);
            lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());

            w.spawnEntity(lightning);
        }

        return stack;
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof VillagerEntity
                || e instanceof CreeperEntity
                || e instanceof PigEntity;
    }

    public TypedActionResult<ItemStack> onFedTo(ItemStack stack, PlayerEntity player, Entity e) {

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(e.world);
        lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());

        if (e.world instanceof ServerWorld) {
            e.onStruckByLightning((ServerWorld)e.world, lightning);
        }

        if (!player.getAbilities().creativeMode) {
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

    @Override
    public Text getName(ItemStack stack) {
        return hasAppearance(stack) ? getAppearanceStack(stack).getName() : super.getName(stack);
    }

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return hasAppearance(stack) ? Toxicity.SEVERE : Toxicity.SAFE;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (hasAppearance(stack)) {
            return Rarity.EPIC;
        }

        return Rarity.RARE;
    }
}
