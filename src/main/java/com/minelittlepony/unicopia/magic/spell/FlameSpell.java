package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IHeldEffect;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;

public class FlameSpell extends AbstractSpell implements IHeldEffect {

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
        PlayerEntity player = caster.getOwner();

        if (player.age % 15 == 0) {
            player.damage(DamageSource.ON_FIRE, 1);
            player.playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 0.5F, 1);
            player.setOnFireFor(1);
        }
    }
}
