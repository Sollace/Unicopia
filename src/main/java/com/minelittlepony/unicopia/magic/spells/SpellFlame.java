package com.minelittlepony.unicopia.magic.spells;

import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IHeldEffect;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;

public class SpellFlame extends AbstractSpell implements IHeldEffect {

    @Override
    public String getName() {
        return "flame";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
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
    public void updateInHand(IPlayer caster, Affinity affinity) {
        EntityPlayer player = caster.getOwner();

        if (player.ticksExisted % 15 == 0) {
            player.attackEntityFrom(DamageSource.ON_FIRE, 1);
            player.playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 0.5F, 1);

            player.setFire(1);
        }
    }
}
