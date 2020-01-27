package com.minelittlepony.unicopia.core.ability;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.core.magic.spell.ShieldSpell;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class PowerMagic implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.magic";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_P;
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
        // TODO: A way to pick the active effect
        if (player.getEffect() instanceof ShieldSpell) {
            player.setEffect(null);
        } else {
            player.setEffect(new ShieldSpell());
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
