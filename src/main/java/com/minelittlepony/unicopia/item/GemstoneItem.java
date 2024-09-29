package com.minelittlepony.unicopia.item;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.PlayerCharmTracker;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GemstoneItem extends Item implements MultiItem, EnchantableItem {

    public GemstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> result = super.use(world, user, hand);

        if (!result.getResult().isAccepted()) {
            ItemStack stack = user.getStackInHand(hand);
            PlayerCharmTracker charms = Pony.of(user).getCharms();

            if (!Pony.of(user).getCompositeRace().canCast()) {
                return result;
            }

            hand = user.isSneaking() ? Hand.OFF_HAND : Hand.MAIN_HAND;

            TypedActionResult<CustomisedSpellType<?>> spell = EnchantableItem.consumeSpell(stack, user, ((Predicate<CustomisedSpellType<?>>)charms.getEquippedSpell(hand)::equals).negate(), true);

            CustomisedSpellType<?> existing = charms.getEquippedSpell(hand);

            if (!existing.isEmpty()) {

                if (stack.getCount() == 1) {
                    stack = existing.traits().applyTo(EnchantableItem.enchant(stack, existing.type()));
                } else {
                    user.giveItemStack(existing.traits().applyTo(EnchantableItem.enchant(stack.split(1), existing.type())));
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

            user.getItemCooldownManager().set(this, 20);
            return TypedActionResult.success(stack, true);
        }

        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> lines, TooltipType type) {
        super.appendTooltip(stack, context, lines, type);

        if (EnchantableItem.isEnchanted(stack)) {
            getSpellEffect(stack).appendTooltip(lines);
        }
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return SpellType.REGISTRY.stream()
                .filter(SpellType::isObtainable)
                .sorted(
                        Comparator.<SpellType<?>, GemstoneItem.Shape>comparing(SpellType::getGemShape).thenComparing(Comparator.comparing(SpellType::getAffinity))
                )
                .map(type -> EnchantableItem.enchant(getDefaultStack(), type))
                .toList();
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return super.hasGlint(stack) || (InteractionManager.getInstance().getClientSpecies().canCast() && EnchantableItem.isEnchanted(stack));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (EnchantableItem.isEnchanted(stack)) {
            if (!InteractionManager.getInstance().getClientSpecies().canCast()) {
                return Text.translatable(getTranslationKey(stack) + ".obfuscated");
            }

            return Text.translatable(getTranslationKey(stack) + ".enchanted", EnchantableItem.getSpellKey(stack).getName());
        }
        return super.getName();
    }

    public enum Shape {
        ARROW,
        BRUSH,
        CROSS,
        DONUT,
        FLAME,
        ICE,
        LAMBDA,
        RING,
        ROCKET,
        ROUND,
        SHIELD,
        SKULL,
        SPLINT,
        STAR,
        TRIANGLE,
        VORTEX,
        WAVE;

        public static final int LENGTH = values().length;

        public float getId() {
            return ordinal() / (float)LENGTH;
        }
    }
}
