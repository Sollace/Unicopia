package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FriendshipBraceletItem extends WearableItem implements DyeableItem, GlowableItem {

    public FriendshipBraceletItem(FabricItemSettings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!isSigned(stack) && EquinePredicates.PLAYER_UNICORN.test(player)) {
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {
        if (isSigned(stack)) {
            list.add(Text.translatable("item.unicopia.friendship_bracelet.issuer", getSignatorName(stack)));
        }
        if (isGlowing(stack)) {
            list.add(Text.translatable("item.unicopia.friendship_bracelet.glowing").formatted(Formatting.ITALIC, Formatting.GRAY));
        }
    }

    @Override
    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return isSigned(stack) ? EquipmentSlot.CHEST : super.getPreferredSlot(stack);
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

    public static boolean isComrade(Caster<?> caster, Entity entity) {
        if (entity instanceof LivingEntity) {
            return caster.getMasterId().filter(id -> {
                return isSignedBy(((LivingEntity)entity).getOffHandStack(), id)
                        || isSignedBy(((LivingEntity)entity).getEquippedStack(EquipmentSlot.CHEST), id);
            }).isPresent();
        }
        return false;
    }

    public static Stream<Pony> getPartyMembers(Caster<?> caster, double radius) {
        return Pony.stream(caster.findAllEntitiesInRange(radius, entity -> isComrade(caster, entity)));
    }
}
