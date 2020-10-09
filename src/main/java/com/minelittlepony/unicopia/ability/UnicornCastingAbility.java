package com.minelittlepony.unicopia.ability;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.Spell;
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
    public double getCostEstimate(Pony player) {
        if (player.hasSpell()) {
            String current = player.getSpell(true).getName();
            String replaced = Streams.stream(player.getMaster().getItemsHand())
                    .map(SpellRegistry::getKeyFromStack)
                    .filter(i -> i != null && !current.equals(i))
                    .findFirst()
                    .orElse(null);
            return replaced == null ? 2 : 4;
        }

        return 4;
    }

    @Override
    public void apply(Pony player, Hit data) {

        if (player.hasSpell()) {
            String current = player.getSpell(true).getName();
            Spell spell = Streams.stream(player.getMaster().getItemsHand())
                    .map(SpellRegistry::getKeyFromStack)
                    .filter(i -> i != null && !current.equals(i))
                    .map(SpellRegistry.instance()::getSpellFromName)
                    .filter(i -> i != null)
                    .findFirst()
                    .orElse(null);
            player.subtractEnergyCost(spell == null ? 2 : 4);
            player.setSpell(spell);
        } else {
            player.subtractEnergyCost(4);
            player.setSpell(Streams.stream(player.getMaster().getItemsHand())
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
