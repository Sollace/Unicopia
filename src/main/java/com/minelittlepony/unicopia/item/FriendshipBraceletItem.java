package com.minelittlepony.unicopia.item;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.Issuer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class FriendshipBraceletItem extends WearableItem {

    public FriendshipBraceletItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!Issuer.isSigned(stack) && (
                   EquinePredicates.PLAYER_UNICORN.test(player)
                || EquinePredicates.RACE_CAN_INFLUENCE_WEATHER.test(player)
                || AmuletSelectors.PEARL_NECKLACE.test(player)
        )) {
            player.setCurrentHand(hand);

            ItemStack result = Issuer.set(stack.copyWithCount(1), player);

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(this));
            player.playSound(USounds.ITEM_BRACELET_SIGN, 1, 1);

            if (stack.isEmpty()) {
                return TypedActionResult.consume(result);
            }
            if (!player.giveItemStack(result)) {
                player.dropStack(result);
            }
            return TypedActionResult.consume(stack);
        }

        return super.use(world, player, hand);
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return Issuer.isSigned(stack) ? EquipmentSlot.CHEST : super.getSlotType(stack);
    }

    @Nullable
    public static String getSignatorName(ItemStack stack) {
        return Issuer.getSignatorName(stack);
    }

    @Nullable
    public static UUID getSignatorId(ItemStack stack) {
        return Issuer.getSignatorId(stack);
    }

    @Deprecated
    public static boolean isSigned(ItemStack stack) {
        return Issuer.isSigned(stack);
    }

    @Deprecated
    public static boolean isSignedBy(ItemStack stack, PlayerEntity player) {
        return Issuer.isSignedBy(stack, player);
    }

    @Deprecated
    public static boolean isSignedBy(ItemStack stack, UUID player) {
        return Issuer.isSignedBy(stack, player);
    }

    public static boolean isComrade(Owned<?> caster, Entity entity) {
        return entity instanceof LivingEntity l && caster.getMasterId()
                .filter(id -> getWornBangles(l).anyMatch(stack -> Issuer.isSignedBy(stack.stack(), id)))
                .isPresent();
    }

    public static boolean isComrade(UUID signator, Entity entity) {
        return entity instanceof LivingEntity l && getWornBangles(l, stack -> Issuer.isSignedBy(stack, signator)).findAny().isPresent();
    }

    public static Stream<Pony> getPartyMembers(Caster<?> caster, double radius) {
        return Pony.stream(caster.findAllEntitiesInRange(radius, entity -> isComrade(caster, entity)));
    }

    private static final Predicate<ItemStack> IS_BANGLE = stack -> stack.isOf(UItems.FRIENDSHIP_BRACELET);

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity) {
        return Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.MAIN_GLOVE, IS_BANGLE),
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.SECONDARY_GLOVE, IS_BANGLE)
        );
    }

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity, @Nullable Predicate<ItemStack> predicate) {
        predicate = predicate == null ? IS_BANGLE : IS_BANGLE.and(predicate);
        return Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.MAIN_GLOVE, predicate),
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.SECONDARY_GLOVE, predicate)
        );
    }

    public static Stream<TrinketsDelegate.EquippedStack> getWornBangles(LivingEntity entity, Identifier slot) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, slot, IS_BANGLE);
    }
}
