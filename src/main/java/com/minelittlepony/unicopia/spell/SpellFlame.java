package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;

public class SpellFlame extends AbstractSpell implements IHeldEffect {

    @Override
    public String getName() {
        return "flame";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public int getTint() {
        return 0xFF5D00;
    }

    @Override
    public boolean update(ICaster<?> source) {
        return false;
    }

    @Override
    public void render(ICaster<?> source) {
    }

    @Override
    public void updateInHand(IPlayer caster, SpellAffinity affinity) {
        EntityPlayer player = caster.getOwner();

        if (player.ticksExisted % 15 == 0) {
            player.attackEntityFrom(DamageSource.ON_FIRE, 1);
            player.playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 0.5F, 1);

            player.setFire(1);
        }
    }
}
