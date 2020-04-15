package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.DispenceableMagicEffect;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class CursedMagicGemItem extends MagicGemItem {

    @Override
    public CastResult onDispenseSpell(BlockPointer source, ItemStack stack, DispenceableMagicEffect effect) {

        BlockPos pos = source.getBlockPos();
        World world = source.getWorld();

        CastResult result = super.onDispenseSpell(source, stack, effect);

        if (result != CastResult.NONE) {
            if (world.random.nextInt(200) == 0) {
                float strength = world.random.nextFloat() * 100;

                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), strength, DestructionType.DESTROY);

                return CastResult.NONE;
            }
        }

        return result;
    }

    @Override
    public CastResult onCastSpell(ItemUsageContext context, MagicEffect effect) {
        CastResult result = super.onCastSpell(context, effect);

        if (result != CastResult.NONE) {
            context.getPlayer().damage(MagicalDamageSource.causePlayerDamage("corruption", context.getPlayer()), 1);
        }

        return result;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }
}
