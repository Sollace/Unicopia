package com.minelittlepony.unicopia.item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.client.FlowingText;
import com.minelittlepony.unicopia.entity.player.PlayerCharmTracker;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GemstoneItem extends Item implements MultiItem {

    public GemstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> result = super.use(world, user, hand);

        if (!result.getResult().isAccepted()) {
            ItemStack stack = user.getStackInHand(hand);
            PlayerCharmTracker charms = Pony.of(user).getCharms();

            TypedActionResult<CustomisedSpellType<?>> spell = consumeSpell(stack, user, ((Predicate<CustomisedSpellType<?>>)charms.getEquippedSpell(hand)::equals).negate());

            CustomisedSpellType<?> existing = charms.getEquippedSpell(hand);

            if (!existing.isEmpty()) {

                if (stack.getCount() == 1) {
                    stack = existing.traits().applyTo(enchant(stack, existing.type()));
                } else {
                    user.giveItemStack(existing.traits().applyTo(enchant(stack.split(1), existing.type())));
                }
            }

            if (spell.getResult().isAccepted()) {
                charms.equipSpell(hand, spell.getValue());
            } else {

                if (existing.isEmpty()) {
                    return result;
                }

                charms.equipSpell(hand, SpellType.EMPTY_KEY.withTraits());
            }
            return TypedActionResult.success(stack, true);
        }

        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, lines, tooltipContext);

        if (isEnchanted(stack)) {
            SpellType<?> key = getSpellKey(stack);

            MutableText line = Text.translatable(key.getTranslationKey() + ".lore").formatted(key.getAffinity().getColor());

            if (!Unicopia.SIDE.getPlayerSpecies().canCast()) {
                line = line.formatted(Formatting.OBFUSCATED);
            }

            lines.addAll(FlowingText.wrap(line, 180).toList());
        }
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Arrays.stream(Affinity.VALUES)
            .flatMap(i -> SpellType.byAffinity(i).stream()
                    .filter(type -> type.isObtainable())
                    .map(type -> enchant(getDefaultStack(), type, i))
            )
            .toList();
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return super.hasGlint(stack) || (Unicopia.SIDE.getPlayerSpecies().canCast() && isEnchanted(stack));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (isEnchanted(stack)) {
            if (!Unicopia.SIDE.getPlayerSpecies().canCast()) {
                return Text.translatable(getTranslationKey(stack) + ".obfuscated");
            }

            return Text.translatable(getTranslationKey(stack) + ".enchanted", getSpellKey(stack).getName());
        }
        return super.getName();
    }

    public static TypedActionResult<CustomisedSpellType<?>> consumeSpell(ItemStack stack, PlayerEntity player, @Nullable Predicate<CustomisedSpellType<?>> filter) {

        if (!isEnchanted(stack)) {
            return TypedActionResult.pass(null);
        }

        SpellType<Spell> key = getSpellKey(stack);

        if (key.isEmpty()) {
            return TypedActionResult.fail(null);
        }

        CustomisedSpellType<?> result = key.withTraits(SpellTraits.of(stack));

        if (filter != null && !filter.test(result)) {
            return TypedActionResult.fail(null);
        }

        if (!player.world.isClient) {
            player.swingHand(player.getStackInHand(Hand.OFF_HAND) == stack ? Hand.OFF_HAND : Hand.MAIN_HAND);

            if (stack.getCount() == 1) {
                unenchant(stack);
            } else {
                player.giveItemStack(unenchant(stack.split(1)));
            }
        }

        return TypedActionResult.consume(result);
    }

    public static boolean isEnchanted(ItemStack stack) {
        return !stack.isEmpty() && stack.hasNbt() && stack.getNbt().contains("spell");
    }

    public static ItemStack enchant(ItemStack stack, SpellType<?> type) {
        return enchant(stack, type, type.getAffinity());
    }

    public static ItemStack enchant(ItemStack stack, SpellType<?> type, Affinity affinity) {
        if (type.isEmpty()) {
            return unenchant(stack);
        }
        stack.getOrCreateNbt().putString("spell", type.getId().toString());
        return type.getTraits().applyTo(stack);
    }

    public static ItemStack unenchant(ItemStack stack) {
        stack.removeSubNbt("spell");
        return stack;
    }

    public static <T extends Spell> SpellType<T> getSpellKey(ItemStack stack) {
        return SpellType.getKey(isEnchanted(stack) ? new Identifier(stack.getNbt().getString("spell")) : SpellType.EMPTY_ID);
    }
}
