package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public int getColor(Pony player) {
        return SpellType.PORTAL.getColor();
    }

    @Override
    public Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + (player.getSpecies() == Race.CHANGELING ? "_changeling" : "") + ".png");
    }

    @Override
    public Pos.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Pos> data) {

        if (player.getSpecies() != Race.CHANGELING) {
            if (type.getTapCount() > 1) {
                player.setAnimation(Animation.WOLOLO, Animation.Recipient.ANYONE, 10);
                if (player.getSpellSlot().clear()) {
                    player.asEntity().sendMessage(Text.translatable("gui.unicopia.action.spells_cleared"), true);
                } else {
                    player.asEntity().sendMessage(Text.translatable("gui.unicopia.action.no_spells_cleared"), true);
                }
                return true;
            }

            if (type == ActivationType.TAP && player.isClient()) {
                InteractionManager.getInstance().openScreen(InteractionManager.SCREEN_DISPELL_ABILITY);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean acceptsQuickAction(Pony player, ActivationType type) {
        return type == ActivationType.NONE || (player.getSpecies() != Race.CHANGELING && type == ActivationType.TAP);
    }

    @Override
    public double getCostEstimate(Pony player) {
        return getTarget(player)
                .filter(caster -> !caster.hasCommonOwner(player))
                .isPresent() ? 10 : 0;
    }

    @Override
    public Optional<Pos> prepare(Pony player) {
        return getTarget(player).map(Caster::getOrigin).map(Pos::new);
    }

    @Override
    public boolean apply(Pony player, Pos data) {
        player.setAnimation(Animation.WOLOLO, Animation.Recipient.ANYONE);
        Caster.stream(VecHelper.findInRange(player.asEntity(), player.asWorld(), data.vec(), 3, EquinePredicates.IS_PLACED_SPELL).stream()).forEach(target -> {
            target.getSpellSlot().clear(false);
        });
        return true;
    }

    private Optional<Caster<?>> getTarget(Pony player) {
        int maxDistance = player.asEntity().isCreative() ? 1000 : 100;
        return TraceHelper.findEntity(player.asEntity(), maxDistance, 1, EquinePredicates.IS_PLACED_SPELL).flatMap(Caster::of);
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
