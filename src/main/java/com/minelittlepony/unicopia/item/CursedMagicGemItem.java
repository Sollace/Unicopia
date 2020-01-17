package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.IDispenceable;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.spells.SpellCastResult;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class CursedMagicGemItem extends MagicGemItem {

    @Override
    public SpellCastResult onDispenseSpell(BlockPointer source, ItemStack stack, IDispenceable effect) {

        BlockPos pos = source.getBlockPos();
        World world = source.getWorld();

        SpellCastResult result = super.onDispenseSpell(source, stack, effect);

        if (result != SpellCastResult.NONE) {
            if (world.random.nextInt(200) == 0) {
                float strength = world.random.nextFloat() * 100;

                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), strength, DestructionType.DESTROY);

                return SpellCastResult.NONE;
            }
        }

        return result;
    }

    @Override
    public SpellCastResult onCastSpell(ItemUsageContext context, IMagicEffect effect) {
        SpellCastResult result = super.onCastSpell(context, effect);

        if (result != SpellCastResult.NONE) {
            context.getPlayer().damage(MagicalDamageSource.causePlayerDamage("corruption", context.getPlayer()), 1);
        }

        return result;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }
}
