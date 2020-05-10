package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.ShieldSpell;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class UnicornCastingAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast();
    }

    @Override
    public Hit tryActivate(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Hit data) {
        // XXX: A way for the player to select which effect they want
        if (player.getEffect() instanceof ShieldSpell) {
            player.setEffect(null);
        } else {
            player.setEffect(new ShieldSpell());
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
