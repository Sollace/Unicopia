package com.minelittlepony.unicopia.ability;


import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Changeling ability to disguise themselves as other players.
 */
public class ChangelingDisguiseAbility extends ChangelingFeedAbility {

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.getOwner();

        RayTraceHelper.Trace trace = RayTraceHelper.doTrace(player, 10, 1, EntityPredicates.EXCEPT_SPECTATOR.and(e -> !(e instanceof LightningEntity)));

        Entity looked = trace.getEntity().map(e -> {
            return e instanceof PlayerEntity ? Pony.of((PlayerEntity)e)
                    .getSpellOrEmpty(DisguiseSpell.class)
                    .map(DisguiseSpell::getDisguise)
                    .map(Disguise::getAppearance)
                    .orElse(e) : e;
        }).orElseGet(() -> trace.getBlockPos().map(pos -> {
            if (!iplayer.getWorld().isAir(pos)) {
                return new FallingBlockEntity(player.getEntityWorld(), 0, 0, 0, iplayer.getWorld().getBlockState(pos));
            }
            return null;
        }).orElse(null));

        player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PARROT_IMITATE_RAVAGER, SoundCategory.PLAYERS, 1.4F, 0.4F);

        iplayer.getSpellOrEmpty(DisguiseSpell.class).orElseGet(() -> {
            DisguiseSpell disc = new DisguiseSpell();

            iplayer.setSpell(disc);
            return disc;
        }).setDisguise(looked);

        player.calculateDimensions();
        iplayer.setDirty();
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().addEnergy(20);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().setEnergy(0);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }
}
