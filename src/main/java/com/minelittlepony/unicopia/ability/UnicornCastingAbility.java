package com.minelittlepony.unicopia.ability;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.ShieldSpell;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class UnicornCastingAbility implements Ability<Ability.Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.magic";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_P;
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canCast();
    }

    @Override
    public Hit tryActivate(Pony player) {
        return new Hit();
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(Pony player, Hit data) {
        // TODO: A way to pick the active effect
        if (player.getEffect() instanceof ShieldSpell) {
            player.setEffect(null);
        } else {
            player.setEffect(new ShieldSpell());
        }
    }

    @Override
    public void preApply(Pony player) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
