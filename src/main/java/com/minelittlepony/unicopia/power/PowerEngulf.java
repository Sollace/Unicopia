package com.minelittlepony.unicopia.power;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.unicopia.spell.ITossedEffect;
import com.minelittlepony.unicopia.spell.SpellChangelingTrap;

public class PowerEngulf implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "engulf";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_L;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 0;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 30;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(IPlayer player) {
        return new Hit();
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(IPlayer player, Hit data) {
        ITossedEffect effect = new SpellChangelingTrap();

        effect.toss(player);
    }

    @Override
    public void preApply(IPlayer player) {

    }

    @Override
    public void postApply(IPlayer player) {

    }

}
