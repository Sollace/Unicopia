package com.minelittlepony.unicopia.item;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public interface EnchantableItem extends ItemConvertible {

    default ItemStack getDefaultStack(SpellType<?> spell) {
        return enchant(asItem().getDefaultStack(), spell);
    }

    default CustomisedSpellType<?> getSpellEffect(ItemStack stack) {
        return EnchantableItem.getSpellKey(stack).withTraits(SpellTraits.of(stack));
    }

    static TypedActionResult<CustomisedSpellType<?>> consumeSpell(ItemStack stack, PlayerEntity player, @Nullable Predicate<CustomisedSpellType<?>> filter, boolean consume) {

        if (!isEnchanted(stack)) {
            return TypedActionResult.pass(SpellType.EMPTY_KEY.withTraits());
        }

        SpellType<Spell> key = EnchantableItem.getSpellKey(stack);

        if (key.isEmpty()) {
            return TypedActionResult.fail(SpellType.EMPTY_KEY.withTraits());
        }

        CustomisedSpellType<?> result = key.withTraits(SpellTraits.of(stack));

        if (filter != null && !filter.test(result)) {
            return TypedActionResult.fail(SpellType.EMPTY_KEY.withTraits());
        }

        if (!player.getWorld().isClient && consume) {
            player.swingHand(player.getStackInHand(Hand.OFF_HAND) == stack ? Hand.OFF_HAND : Hand.MAIN_HAND);
            player.getItemCooldownManager().set(stack.getItem(), 20);

            if (!player.isCreative()) {
                if (stack.getCount() == 1) {
                    unenchant(stack);
                } else {
                    player.giveItemStack(unenchant(stack.split(1)));
                }
            }
        }

        return TypedActionResult.consume(result);
    }

    static boolean isEnchanted(ItemStack stack) {
        return !getSpellKey(stack).isEmpty();
    }

    static ItemStack enchant(ItemStack stack, SpellType<?> type) {
        return enchant(stack, type, type.getAffinity());
    }

    static ItemStack enchant(ItemStack stack, SpellType<?> type, Affinity affinity) {
        if (type.isEmpty()) {
            return unenchant(stack);
        }
        stack.set(UDataComponentTypes.STORED_SPELL, type);
        return type.getTraits().applyTo(stack);
    }

    static ItemStack unenchant(ItemStack stack) {
        stack.remove(UDataComponentTypes.STORED_SPELL);
        stack.remove(UDataComponentTypes.SPELL_TRAITS);
        return stack;
    }

    static Optional<SpellType<?>> getSpellKeyOrEmpty(ItemStack stack) {
        SpellType<?> type = getSpellKey(stack);
        return type.isEmpty() ? Optional.empty() : Optional.of(type);
    }

    @SuppressWarnings("unchecked")
    static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return (SpellType<T>)stack.getOrDefault(UDataComponentTypes.STORED_SPELL, SpellType.empty());
    }
}
