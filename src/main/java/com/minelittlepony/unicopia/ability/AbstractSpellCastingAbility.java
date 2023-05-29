package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

abstract class AbstractSpellCastingAbility implements Ability<Hit> {

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast();
    }

    @Override
    public Text getName(Pony player) {
        CustomisedSpellType<?> spell = player.getCharms().getEquippedSpell(player.getCharms().getHand());
        TypedActionResult<CustomisedSpellType<?>> gemSpell = player.getCharms().getSpellInHand(false);
        var active = !player.getAbilities().getStat(AbilitySlot.PRIMARY).getActiveAbility().isEmpty();
        if (!spell.isEmpty()) {
            if (active) {
                if (gemSpell.getResult().isAccepted()) {
                    return Text.translatable(getTranslationKey() + ".with_spell.hand",
                        gemSpell.getValue().type().getName().copy().formatted(gemSpell.getValue().type().getAffinity().getColor())
                    );
                }

                return Text.translatable(getTranslationKey() + ".with_spell.active",
                        spell.type().getName().copy().formatted(spell.type().getAffinity().getColor())
                );
            }

            return Text.translatable(getTranslationKey() + ".with_spell" + (gemSpell.getResult().isAccepted() ? ".replacing" : ""),
                spell.type().getName().copy().formatted(spell.type().getAffinity().getColor()),
                gemSpell.getValue().type().getName().copy().formatted(gemSpell.getValue().type().getAffinity().getColor())
            );
        }
        return getName();
    }

    @Override
    public int getColor(Pony player) {
        TypedActionResult<CustomisedSpellType<?>> newSpell = player.getCharms().getSpellInHand(false);

        if (newSpell.getResult() != ActionResult.FAIL) {
            return newSpell.getValue().type().getColor();
        }
        return -1;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
