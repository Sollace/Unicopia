package com.minelittlepony.unicopia.ability;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.ShieldSpell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

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

        if (player.hasSpell()) {
            String current = player.getSpell(true).getName();
            player.setSpell(Streams.stream(player.getOwner().getItemsHand())
                    .map(SpellRegistry::getKeyFromStack)
                    .filter(i -> i != null && !current.equals(i))
                    .map(SpellRegistry.instance()::getSpellFromName)
                    .filter(i -> i != null)
                    .findFirst()
                    .orElse(null));
        } else {
            player.setSpell(Streams.stream(player.getOwner().getItemsHand())
                    .map(SpellRegistry.instance()::getSpellFrom)
                    .filter(i -> i != null)
                    .findFirst()
                    .orElseGet(ShieldSpell::new));
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getEnergy().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
