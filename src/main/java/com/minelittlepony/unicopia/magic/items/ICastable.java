package com.minelittlepony.unicopia.magic.items;

import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.IDispenceable;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.spells.CastResult;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICastable extends IMagicalItem, IDispensable {

    @Override
    default TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {
        IDispenceable effect = SpellRegistry.instance().getDispenseActionFrom(stack);

        if (effect == null) {
            return new TypedActionResult<>(ActionResult.FAIL, stack);
        }

        CastResult dispenceResult = onDispenseSpell(source, stack, effect);

        if (dispenceResult == CastResult.DEFAULT) {
            return new TypedActionResult<>(ActionResult.PASS, stack);
        }

        if (dispenceResult == CastResult.PLACE) {
            castContainedSpell(source.getWorld(), source.getBlockPos(), stack, effect);

            stack.decrement(1);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    CastResult onDispenseSpell(BlockPointer source, ItemStack stack, IDispenceable effect);

    CastResult onCastSpell(ItemUsageContext context, IMagicEffect effect);

    boolean canFeed(SpellcastEntity spell, ItemStack stack);

    /**
     * Called to cast a spell. The result is an entity spawned with the spell attached.
     */
    default SpellcastEntity castContainedSpell(World world, BlockPos pos, ItemStack stack, IMagicEffect effect) {
        SpellcastEntity spell = new SpellcastEntity(null, world);

        spell.setAffinity(getAffinity(stack));
        spell.setPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        spell.setEffect(effect);

        world.spawnEntity(spell);

        return spell;
    }
}
