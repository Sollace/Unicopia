package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

/**
 * Dispells an active spell
 */
public class UnicornDispellAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 4;
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
    public Pos.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type) {

        if (type.getTapCount() > 1) {
            player.getSpellSlot().clear();
            return true;
        }
        if (type == ActivationType.TAP) {
            // TODO: gui to remove spells
        }

        return false;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return getTarget(player)
                .filter(caster -> caster.getMaster() != player.getMaster())
                .isPresent() ? 10 : 0;
    }

    @Override
    public Pos tryActivate(Pony player) {
        return getTarget(player).map(Caster::getOrigin).map(Pos::new).orElse(null);
    }

    @Override
    public void apply(Pony player, Pos data) {
        Caster.stream(VecHelper.findInRange(player.getEntity(), player.getWorld(), data.vec(), 2, EquinePredicates.IS_PLACED_SPELL).stream()).forEach(target -> {
            target.getSpellSlot().clear();
        });
    }

    private Optional<Caster<?>> getTarget(Pony player) {
        int maxDistance = player.getMaster().isCreative() ? 1000 : 100;
        return RayTraceHelper.doTrace(player.getMaster(), maxDistance, 1,
                EquinePredicates.IS_PLACED_SPELL)
                .getEntity()
            .flatMap(Caster::of);
    }



    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
