package com.minelittlepony.unicopia.item;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FriendshipBraceletItem extends Item implements DyeableItem, Wearable, GlowableItem {

    public FriendshipBraceletItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!isSigned(stack) && EquinePredicates.PLAYER_UNICORN.test(player)) {
            player.setCurrentHand(hand);

            ItemStack result = stack.copy();
            result.setCount(1);
            result.getOrCreateTag().putString("issuer", player.getName().asString());

            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(this));
            player.playSound(SoundEvents.ITEM_BOOK_PUT, 1, 1);

            if (stack.isEmpty()) {
                return TypedActionResult.consume(result);
            }
            if (!player.giveItemStack(result)) {
                player.dropStack(result);
            }
            return TypedActionResult.consume(stack);
        }

        EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
        ItemStack currentArmor = player.getEquippedStack(slot);

        if (currentArmor.isEmpty()) {
            ItemStack result = stack.copy();
            result.setCount(1);

            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }

            player.equipStack(slot, result);
            return TypedActionResult.success(stack, world.isClient());
        }

        return TypedActionResult.fail(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {
        if (isSigned(stack)) {
            list.add(new TranslatableText("item.unicopia.friendship_bracelet.issuer", getSignature(stack)));
        }
        if (isGlowing(stack)) {
            list.add(new TranslatableText("item.unicopia.friendship_bracelet.glowing").formatted(Formatting.ITALIC, Formatting.GRAY));
        }
    }

    private boolean checkSignature(ItemStack stack, PlayerEntity player) {
        return player.getName().asString().contentEquals(getSignature(stack));
    }

    @Nullable
    public static String getSignature(ItemStack stack) {
        return isSigned(stack) ? stack.getTag().getString("issuer") : null;
    }

    public static boolean isSigned(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("issuer");
    }

    public static boolean isSignedBy(ItemStack stack, PlayerEntity player) {
        return stack.getItem() instanceof FriendshipBraceletItem
                && ((FriendshipBraceletItem)stack.getItem()).checkSignature(stack, player);
    }

    public static boolean isComrade(Caster<?> caster, Entity entity) {
        Entity master = caster.getMaster();
        if (master instanceof PlayerEntity && entity instanceof LivingEntity) {
            return isSignedBy(((LivingEntity)entity).getOffHandStack(), (PlayerEntity)master)
                    || isSignedBy(((LivingEntity)entity).getEquippedStack(EquipmentSlot.CHEST), (PlayerEntity)master);
        }

        return false;
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        return isSigned(stack) ? EquipmentSlot.CHEST : EquipmentSlot.OFFHAND;
    }
}
