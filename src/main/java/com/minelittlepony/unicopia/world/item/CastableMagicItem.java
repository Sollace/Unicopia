package com.minelittlepony.unicopia.world.item;

import com.minelittlepony.unicopia.magic.AffineItem;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.DispenceableSpell;
import com.minelittlepony.unicopia.magic.Dispensable;
import com.minelittlepony.unicopia.magic.Spell;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.world.entity.SpellcastEntity;
import com.minelittlepony.unicopia.world.entity.UEntities;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CastableMagicItem extends AffineItem, Dispensable {

    @Override
    default TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {
        DispenceableSpell effect = SpellRegistry.instance().getDispenseActionFrom(stack);

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

    CastResult onDispenseSpell(BlockPointer source, ItemStack stack, DispenceableSpell effect);

    CastResult onCastSpell(ItemUsageContext context, Spell effect);

    boolean canFeed(SpellcastEntity spell, ItemStack stack);

    /**
     * Called to cast a spell. The result is an entity spawned with the spell attached.
     */
    default SpellcastEntity castContainedSpell(World world, BlockPos pos, ItemStack stack, Spell effect) {
        SpellcastEntity spell = new SpellcastEntity(UEntities.MAGIC_SPELL, world);

        spell.setAffinity(getAffinity(stack));
        spell.updatePositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        world.spawnEntity(spell);
        spell.setSpell(effect);

        return spell;
    }
}
