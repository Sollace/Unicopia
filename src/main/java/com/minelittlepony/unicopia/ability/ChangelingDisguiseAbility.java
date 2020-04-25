package com.minelittlepony.unicopia.ability;


import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.InAnimate;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.particles.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
        HitResult trace = VecHelper.getObjectMouseOver(player, 10, 1);

        Entity looked = null;

        if (trace.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)trace).getBlockPos();

            if (!iplayer.getWorld().isAir(pos)) {
                BlockState state = iplayer.getWorld().getBlockState(pos);

                looked = new FallingBlockEntity(player.getEntityWorld(), 0, 0, 0, state);
            }
        } else if (trace.getType() == HitResult.Type.ENTITY) {
            looked = ((EntityHitResult)trace).getEntity();

            if (looked instanceof PlayerEntity) {
                looked = Pony.of((PlayerEntity)looked)
                        .getEffect(DisguiseSpell.class)
                        .map(DisguiseSpell::getDisguise)
                        .orElse(looked);
            }

            if (looked instanceof LightningEntity
            || (looked instanceof InAnimate && !((InAnimate)looked).canInteract(Race.CHANGELING))) {
                looked = null;
            }
        }

        player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PARROT_IMITATE_RAVAGER, SoundCategory.PLAYERS, 1.4F, 0.4F);

        iplayer.getEffect(DisguiseSpell.class).orElseGet(() -> {
            DisguiseSpell disc = new DisguiseSpell();

            iplayer.setEffect(disc);
            return disc;
        }).setDisguise(looked);

        iplayer.sendCapabilities(true);
    }

    @Override
    public void preApply(Pony player) {
        player.addEnergy(2);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }

    @Override
    public void postApply(Pony player) {
        player.setEnergy(0);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }
}
