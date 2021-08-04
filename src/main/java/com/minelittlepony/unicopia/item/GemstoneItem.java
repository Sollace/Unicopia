package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class GemstoneItem extends Item {

    public GemstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext tooltipContext) {

        if (isEnchanted(stack)) {
            SpellType<?> key = getSpellKey(stack);

            MutableText line = new TranslatableText(key.getTranslationKey() + ".lore").formatted(key.getAffinity().getColor());

            if (!Unicopia.SIDE.getPlayerSpecies().canCast()) {
                line = line.formatted(Formatting.OBFUSCATED);
            }

            lines.add(line);
        }
    }

    @Override
    public void appendStacks(ItemGroup tab, DefaultedList<ItemStack> items) {
        super.appendStacks(tab, items);
        if (isIn(tab)) {
            for (Affinity i : Affinity.VALUES) {
                SpellType.byAffinity(i).forEach(type -> {
                    if (type.isObtainable()) {
                        items.add(enchanted(getDefaultStack(), type, i));
                    }
                });
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return super.hasGlint(stack) || (Unicopia.SIDE.getPlayerSpecies().canCast() && isEnchanted(stack));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (isEnchanted(stack)) {
            if (!Unicopia.SIDE.getPlayerSpecies().canCast()) {
                return new TranslatableText(getTranslationKey(stack) + ".obfuscated");
            }

            return new TranslatableText(getTranslationKey(stack) + ".enchanted", getSpellKey(stack).getName());
        }
        return super.getName();
    }

    public static TypedActionResult<SpellType<?>> consumeSpell(ItemStack stack, PlayerEntity player, @Nullable SpellType<?> exclude, Predicate<SpellType<?>> test) {

        if (!isEnchanted(stack)) {
            return TypedActionResult.pass(null);
        }

        SpellType<Spell> key = getSpellKey(stack);

        if (Objects.equals(key, exclude)) {
            return TypedActionResult.fail(null);
        }

        if (key == SpellType.EMPTY_KEY || !test.test(key)) {
            return TypedActionResult.fail(null);
        }

        if (!player.world.isClient) {
            player.swingHand(player.getStackInHand(Hand.OFF_HAND) == stack ? Hand.OFF_HAND : Hand.MAIN_HAND);

            if (stack.getCount() == 1) {
                unenchanted(stack);
            } else {
                player.giveItemStack(unenchanted(stack.split(1)));
            }
        }

        return TypedActionResult.consume(key);
    }

    public static boolean isEnchanted(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().contains("spell");
    }

    public static ItemStack enchanted(ItemStack stack, SpellType<?> type) {
        return enchanted(stack, type, type.getAffinity());
    }

    public static ItemStack enchanted(ItemStack stack, SpellType<?> type, Affinity affinity) {
        stack.getOrCreateTag().putString("spell", type.getId().toString());
        return stack;
    }

    public static ItemStack unenchanted(ItemStack stack) {
        stack.removeSubTag("spell");
        return stack;
    }

    public static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return SpellType.getKey(isEnchanted(stack) ? new Identifier(stack.getTag().getString("spell")) : SpellType.EMPTY_ID);
    }
}
