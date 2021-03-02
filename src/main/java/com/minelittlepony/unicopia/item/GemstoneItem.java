package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class GemstoneItem extends Item {

    public GemstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {

        if (isEnchanted(stack)) {
            SpellType<?> key = getSpellKey(stack);
            Affinity affinity = getAffinity(stack);

            list.add(new TranslatableText(key.getTranslationKey(affinity) + ".lore").formatted(affinity.getColor()));
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
            return new TranslatableText(getTranslationKey(stack) + ".enchanted", getSpellKey(stack).getName(getAffinity(stack)));
        }
        return super.getName();
    }

    public static Stream<Spell> consumeSpell(ItemStack stack, PlayerEntity player, @Nullable SpellType<?> exclude, Predicate<Spell> test) {
        SpellType<Spell> key = GemstoneItem.getSpellKey(stack);

        if (Objects.equals(key, exclude)) {
            return Stream.empty();
        }

        Spell spell = key.create(getAffinity(stack));

        if (spell == null || !test.test(spell)) {
            return Stream.empty();
        }

        if (!player.world.isClient) {
            if (stack.getCount() == 1) {
                GemstoneItem.unenchanted(stack);
            } else {
                player.giveItemStack(GemstoneItem.unenchanted(stack.split(1)));
            }
        }

        return Stream.of(spell);
    }

    public static boolean isEnchanted(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().contains("spell");
    }

    public static ItemStack enchanted(ItemStack stack, SpellType<?> type) {
        return enchanted(stack, type, type.getAffinity());
    }

    public static ItemStack enchanted(ItemStack stack, SpellType<?> type, Affinity affinity) {
        stack.getOrCreateTag().putString("spell", type.getId().toString());
        stack.getOrCreateTag().putInt("affinity", affinity.ordinal());
        return stack;
    }

    public static ItemStack unenchanted(ItemStack stack) {
        stack.removeSubTag("spell");
        stack.removeSubTag("affinity");
        return stack;
    }

    public static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return SpellType.getKey(isEnchanted(stack) ? new Identifier(stack.getTag().getString("spell")) : SpellType.EMPTY_ID);
    }

    public static Affinity getAffinity(ItemStack stack) {
        Affinity fallback = getSpellKey(stack).getAffinity();

        if (stack.hasTag() && stack.getTag().contains("affinity")) {
            return Affinity.of(stack.getTag().getInt("affinity"), fallback);
        }
        return fallback;
    }
}
