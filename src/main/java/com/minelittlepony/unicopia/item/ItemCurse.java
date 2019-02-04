package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.spell.IDispenceable;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellAffinity;
import com.minelittlepony.unicopia.spell.SpellCastResult;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCurse extends ItemSpell {

    public ItemCurse(String domain, String name) {
        super(domain, name);
    }

    @Override
    public SpellCastResult onDispenseSpell(IBlockSource source, ItemStack stack, IDispenceable effect) {

        BlockPos pos = source.getBlockPos();
        World world = source.getWorld();

        SpellCastResult result = super.onDispenseSpell(source, stack, effect);

        if (result != SpellCastResult.NONE) {
            if (world.rand.nextInt(200) == 0) {
                float strength = world.rand.nextFloat() * 100;

                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), strength, true);

                return SpellCastResult.NONE;
            }
        }

        return result;
    }

    @Override
    public SpellCastResult onCastSpell(EntityPlayer player, World world, BlockPos pos, ItemStack stack, IMagicEffect effect, EnumFacing side, float hitX, float hitY, float hitZ) {
        SpellCastResult result = super.onCastSpell(player, world, pos, stack, effect, side, hitX, hitY, hitZ);

        if (result != SpellCastResult.NONE) {
            player.attackEntityFrom(MagicalDamageSource.causePlayerDamage("corruption", player), 1);
        }

        return result;
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }
}
