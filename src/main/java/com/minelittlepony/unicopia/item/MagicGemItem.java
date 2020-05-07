package com.minelittlepony.unicopia.item;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.Castable;
import com.minelittlepony.unicopia.magic.DispenceableMagicEffect;
import com.minelittlepony.unicopia.magic.Dispensable;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.Useable;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MagicGemItem extends Item implements Castable {

    public MagicGemItem(Settings settings) {
        super(settings);
        Dispensable.setDispenseable(this, this::dispenseStack);
    }

    @Override
    public boolean hasEnchantmentGlint(ItemStack stack) {
        return super.hasEnchantmentGlint(stack) || SpellRegistry.stackHasEnchantment(stack);
    }

    @Override
    public CastResult onDispenseSpell(BlockPointer source, ItemStack stack, DispenceableMagicEffect effect) {
        Direction facing = source.getBlockState().get(DispenserBlock.FACING);
        BlockPos pos = source.getBlockPos().offset(facing);

        return effect.onDispenced(pos, facing, source, getAffinity(stack));
    }

    @Override
    public CastResult onCastSpell(ItemUsageContext context, MagicEffect effect) {
        if (effect instanceof Useable) {
            return ((Useable)effect).onUse(context, getAffinity(context.getStack()));
        }

        return CastResult.PLACE;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        BlockPos pos = context.getBlockPos();
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();

        if (hand != Hand.MAIN_HAND || !EquinePredicates.PLAYER_UNICORN.test(player)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);

        if (!SpellRegistry.stackHasEnchantment(stack)) {
            return ActionResult.FAIL;
        }

        MagicEffect effect = SpellRegistry.instance().getSpellFrom(stack);

        if (effect == null) {
            return ActionResult.FAIL;
        }

        CastResult result = onCastSpell(context, effect);

        if (!context.getWorld().isClient) {
            pos = pos.offset(context.getSide());

            if (result == CastResult.PLACE) {
                castContainedSpell(context.getWorld(), pos, stack, effect).setOwner(player);
            }
        }

        if (result != CastResult.NONE) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        if (!EquinePredicates.PLAYER_UNICORN.test(player)) {
            return new TypedActionResult<>(ActionResult.PASS, stack);
        }

        if (!SpellRegistry.stackHasEnchantment(stack)) {
            return new TypedActionResult<>(ActionResult.FAIL, stack);
        }

        Useable effect = SpellRegistry.instance().getUseActionFrom(stack);

        if (effect != null) {
            CastResult result = effect.onUse(stack, getAffinity(stack), player, world, VecHelper.getLookedAtEntity(player, 5));

            if (result != CastResult.NONE) {
                if (result == CastResult.PLACE && !player.isCreative()) {
                    stack.decrement(1);
                }

                return new TypedActionResult<>(ActionResult.SUCCESS, stack);
            }
        }

        return new TypedActionResult<>(ActionResult.PASS, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext context) {
        if (SpellRegistry.stackHasEnchantment(stack)) {
            Affinity affinity = getAffinity(stack);

            Text text = new TranslatableText(String.format("%s.%s.tagline",
                    affinity.getTranslationKey(),
                    SpellRegistry.getKeyFromStack(stack)
            ));
            text.getStyle().setColor(affinity.getColourCode());

            tooltip.add(text);
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        if (SpellRegistry.stackHasEnchantment(stack)) {
            return new TranslatableText(getTranslationKey(stack) + ".enchanted",
                    new TranslatableText(String.format("%s.%s", getAffinity(stack).getTranslationKey(), SpellRegistry.getKeyFromStack(stack)
            )));
        }

        return super.getName(stack);
    }

    @Override
    public void appendStacks(ItemGroup tab, DefaultedList<ItemStack> subItems) {
        super.appendStacks(tab, subItems);

        if (isIn(tab)) {
            SpellRegistry.instance().getAllNames(getAffinity()).forEach(name -> {
                subItems.add(SpellRegistry.instance().enchantStack(new ItemStack(this), name));
            });
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (SpellRegistry.stackHasEnchantment(stack)) {
            return Rarity.UNCOMMON;
        }

        return super.getRarity(stack);
    }

    @Override
    public boolean canFeed(SpellcastEntity entity, ItemStack stack) {
        MagicEffect effect = entity.getEffect();

        return effect != null
                && entity.getAffinity() == getAffinity()
                && effect.getName().equals(SpellRegistry.getKeyFromStack(stack));
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }
}
