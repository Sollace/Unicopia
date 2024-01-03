package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CuringJokeItem extends BlockItem {
    static final List<Predicate<LivingEntity>> EFFECTS = List.of(
            CuringJokeItem::restoreAir,
            CuringJokeItem::restoreHunger,
            CuringJokeItem::restoreHealth,
            CuringJokeItem::removeEffect,
            CuringJokeItem::uncurseItem,
            CuringJokeItem::repairItem
    );

    public CuringJokeItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        var items = new ArrayList<>(EFFECTS);
        while (!items.isEmpty()) {
            if (items.remove(world.random.nextInt(items.size())).test(user)) {
                ParticleUtils.spawnParticles(new MagicParticleEffect(0x3388EE), user, 25);
                world.playSound(null, user.getBlockPos(), USounds.ITEM_CURING_JOKE_CURE, user.getSoundCategory(), 1, 1);
                break;
            }
        }

        return super.finishUsing(stack, world, user);
    }

    static boolean restoreAir(LivingEntity user) {
        if (user.getAir() < user.getMaxAir()) {
            user.setAir(user.getMaxAir());
            return true;
        }
        return false;
    }

    static boolean restoreHunger(LivingEntity user) {
        if (user instanceof PlayerEntity player && player.getHungerManager().getFoodLevel() < 20) {
            player.getHungerManager().add(20, 0);
            return true;
        }
        return false;
    }

    static boolean restoreHealth(LivingEntity user) {
        if (user.getHealth() < user.getMaxHealth()) {
            user.setHealth(user.getMaxHealth());
            return true;
        }
        return false;
    }

    static boolean removeEffect(LivingEntity user) {
        return user.getStatusEffects().stream().filter(effect -> {
            return !effect.getEffectType().isBeneficial();
        }).findAny().filter(effect -> {
            user.removeStatusEffect(effect.getEffectType());
            return true;
        }).isPresent();
    }

    static boolean uncurseItem(LivingEntity user) {
        return getInventory(user)
                .filter(s -> EnchantmentHelper.get(s).keySet().stream().anyMatch(Enchantment::isCursed))
                .findAny()
                .filter(s -> {
                    var enchantments = EnchantmentHelper.get(s);
                    return enchantments.keySet().stream().filter(Enchantment::isCursed).findAny().filter(e -> {
                        enchantments.remove(e);
                        EnchantmentHelper.set(enchantments, s);
                        return true;
                    }).isPresent();
                }).isPresent();
    }

    static boolean repairItem(LivingEntity user) {
        return getInventory(user)
                .filter(s -> s.getDamage() < s.getMaxDamage())
                .findAny().filter(s -> {
                    s.setDamage(0);
                    return true;
                }).isPresent();
    }

    static Stream<ItemStack> getInventory(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            return InventoryUtil.stream(player.getInventory());
        }

        if (entity instanceof InventoryOwner owner) {
            return InventoryUtil.stream(owner.getInventory());
        }

        return StreamSupport.stream(entity.getItemsEquipped().spliterator(), false);
    }
}
