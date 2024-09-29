package com.minelittlepony.unicopia.item;

import java.util.List;
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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class FriendshipBraceletItem extends WearableItem {

    public FriendshipBraceletItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!isSigned(stack) && (
                   EquinePredicates.PLAYER_UNICORN.test(player)
                || EquinePredicates.RACE_CAN_INFLUENCE_WEATHER.test(player)
                || AmuletSelectors.PEARL_NECKLACE.test(player)
        )) {
            player.setCurrentHand(hand);

            ItemStack result = stack.copy();
            result.setCount(1);
            result.getOrCreateNbt().putString("issuer", player.getName().getString());
            result.getOrCreateNbt().putUuid("issuer_id", player.getUuid());

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
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> lines, TooltipType type) {
        super.appendTooltip(stack, context, lines, type);
        if (isSigned(stack)) {
            lines.add(Text.translatable("item.unicopia.friendship_bracelet.issuer", getSignatorName(stack)));
        }
        if (GlowableItem.isGlowing(stack)) {
            lines.add(Text.translatable("item.unicopia.friendship_bracelet.glowing").formatted(Formatting.ITALIC, Formatting.GRAY));
        }
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return isSigned(stack) ? EquipmentSlot.CHEST : super.getSlotType(stack);
    }

    private boolean checkSignature(ItemStack stack, PlayerEntity player) {
        return checkSignature(stack, player.getUuid());
    }

    private boolean checkSignature(ItemStack stack, UUID player) {
        return player.equals(getSignatorId(stack));
    }

    @Nullable
    public static String getSignatorName(ItemStack stack) {
        return isSigned(stack) ? stack.getNbt().getString("issuer") : null;
    }

    @Nullable
    public static UUID getSignatorId(ItemStack stack) {
        return isSigned(stack) ? stack.getNbt().getUuid("issuer_id") : null;
    }

    public static boolean isSigned(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("issuer_id");
    }

    public static boolean isSignedBy(ItemStack stack, PlayerEntity player) {
        return stack.getItem() instanceof FriendshipBraceletItem
                && ((FriendshipBraceletItem)stack.getItem()).checkSignature(stack, player);
    }

    public static boolean isSignedBy(ItemStack stack, UUID player) {
        return stack.getItem() instanceof FriendshipBraceletItem
                && ((FriendshipBraceletItem)stack.getItem()).checkSignature(stack, player);
    }

    public static boolean isComrade(Owned<?> caster, Entity entity) {
        return entity instanceof LivingEntity l && caster.getMasterId()
                .filter(id -> getWornBangles(l).anyMatch(stack -> isSignedBy(stack.stack(), id)))
                .isPresent();
    }

    public static boolean isComrade(UUID signator, Entity entity) {
        return entity instanceof LivingEntity l && getWornBangles(l, stack -> isSignedBy(stack, signator)).findAny().isPresent();
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
