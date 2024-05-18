package com.minelittlepony.unicopia.item;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.TextHelper;
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, lines, tooltipContext);

        if (EnchantableItem.isEnchanted(stack)) {
            CustomisedSpellType<?> type = getSpellEffect(stack);

            MutableText line = Text.translatable(type.type().getTranslationKey() + ".lore").formatted(type.type().getAffinity().getColor());

            if (!InteractionManager.getInstance().getClientSpecies().canCast()) {
                line = line.formatted(Formatting.OBFUSCATED);
            }
            lines.addAll(TextHelper.wrap(line, 180).toList());
            lines.add(Text.empty());
            float corruption = ((int)type.traits().getCorruption() * 10) + type.type().getAffinity().getCorruption();
            if (corruption != 0) {
                lines.add(Text.translatable("affinity.unicopia.when_cast").formatted(Formatting.GRAY));
                lines.add(Text.translatable("affinity.unicopia.corruption", corruption > 0 ? "+" : "-", ItemStack.MODIFIER_FORMAT.format(Math.abs(corruption))).formatted(corruption < 0 ? Formatting.DARK_GREEN : Formatting.RED));
            }
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
