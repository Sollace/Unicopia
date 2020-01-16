package com.minelittlepony.unicopia.magic.items;

import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.IDispenceable;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.spells.SpellCastResult;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICastable extends IMagicalItem, IDispensable {

    @Override
    default ActionResult<ItemStack> dispenseStack(IBlockSource source, ItemStack stack) {
        IDispenceable effect = SpellRegistry.instance().getDispenseActionFrom(stack);

        if (effect == null) {
            return new ActionResult<>(ActionResult.FAIL, stack);
        }

        SpellCastResult dispenceResult = onDispenseSpell(source, stack, effect);

        if (dispenceResult == SpellCastResult.DEFAULT) {
            return new ActionResult<>(ActionResult.PASS, stack);
        }

        if (dispenceResult == SpellCastResult.PLACE) {
            castContainedSpell(source.getWorld(), source.getBlockPos(), stack, effect);

            stack.shrink(1);
        }

        return new ActionResult(ActionResult.SUCCESS, stack);
    }

    SpellCastResult onDispenseSpell(BlockState source, ItemStack stack, IDispenceable effect);

    SpellCastResult onCastSpell(PlayerEntity player, World world, BlockPos pos, ItemStack stack, IMagicEffect effect, Direction side, float hitX, float hitY, float hitZ);

    boolean canFeed(SpellcastEntity spell, ItemStack stack);

    /**
     * Called to cast a spell. The result is an entity spawned with the spell attached.
     */
    default SpellcastEntity castContainedSpell(World world, BlockPos pos, ItemStack stack, IMagicEffect effect) {
        SpellcastEntity spell = new SpellcastEntity(world);

        spell.setAffinity(getAffinity(stack));
        spell.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        spell.setEffect(effect);

        world.spawnEntity(spell);

        return spell;
    }
}
