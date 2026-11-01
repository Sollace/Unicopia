package com.minelittlepony.unicopia.item;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface EnchantableItem {
    static CustomisedSpellType<?> getSpellEffect(ItemStack stack) {
        var key = getSpellKey(stack);
        return key.withTraits(SpellTraits.getEmbeddedTraits(stack).orElse(key.getTraits()));
    }

    static TypedActionResult<CustomisedSpellType<?>> consumeSpell(ItemStack stack, PlayerEntity player, @Nullable Predicate<CustomisedSpellType<?>> filter, boolean consume) {
        return getSpellKey(stack).toOptional().map(key -> {
            CustomisedSpellType<?> result = key.withTraits(SpellTraits.getEmbeddedTraits(stack).orElse(key.getTraits()));

            if (filter != null && !filter.test(result)) {
                return TypedActionResult.<CustomisedSpellType<?>>fail(SpellType.EMPTY_KEY.withTraits());
            }

            if (!player.getWorld().isClient && consume) {
                player.swingHand(player.getStackInHand(Hand.OFF_HAND) == stack ? Hand.OFF_HAND : Hand.MAIN_HAND, true);
                player.getItemCooldownManager().set(stack, 20);

                if (!player.isCreative()) {
                    if (stack.getCount() == 1) {
                        unenchant(stack);
                    } else {
                        player.giveItemStack(unenchant(stack.split(1)));
                    }
                }
            }

            return TypedActionResult.<CustomisedSpellType<?>>success(result);
        }).orElseGet(() -> TypedActionResult.pass(SpellType.EMPTY_KEY.withTraits()));
    }

    static boolean isEnchanted(ItemStack stack) {
        return !getSpellKey(stack).isEmpty();
    }

    static ItemStack enchant(ItemStack stack, SpellType<?> type) {
        return type.toOptional().map(t -> {
            stack.set(UDataComponentTypes.STORED_SPELL, type);
            return type.getTraits().applyTo(stack);
        }).orElseGet(() -> unenchant(stack));
    }

    static ItemStack unenchant(ItemStack stack) {
        stack.remove(UDataComponentTypes.STORED_SPELL);
        stack.remove(UDataComponentTypes.SPELL_TRAITS);
        return stack;
    }

    static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return Untyped.cast(stack.getOrDefault(UDataComponentTypes.STORED_SPELL, SpellType.empty()));
    }
}
