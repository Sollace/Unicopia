package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.unicopia.spell.SpellShield;

public class PowerMagic implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.magic";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_P;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 20;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 0;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canCast();
    }

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
        if (player.getEffect() instanceof SpellShield) {
            player.setEffect(null);
        } else {
            player.setEffect(new SpellShield());
        }
    }

    @Override
    public void preApply(IPlayer player) {
        player.spawnParticles(UParticles.UNICORN_MAGIC, 5);
    }

    @Override
    public void postApply(IPlayer player) {
        player.spawnParticles(UParticles.UNICORN_MAGIC, 5);
    }
}
