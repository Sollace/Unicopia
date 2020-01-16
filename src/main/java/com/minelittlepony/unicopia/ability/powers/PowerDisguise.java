package com.minelittlepony.unicopia.ability.powers;


import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.ability.Hit;
import com.minelittlepony.unicopia.entity.IInAnimate;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.spells.SpellDisguise;
import com.minelittlepony.util.VecHelper;

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

public class PowerDisguise extends PowerFeed {

    @Override
    public String getKeyName() {
        return "unicopia.power.disguise";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_P;
    }

    @Nullable
    @Override
    public Hit tryActivate(IPlayer player) {
        return new Hit();
    }

    @Override
    public void apply(IPlayer iplayer, Hit data) {
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
                looked = SpeciesList.instance().getPlayer((PlayerEntity)looked)
                        .getEffect(SpellDisguise.class)
                        .map(SpellDisguise::getDisguise)
                        .orElse(looked);
            }

            if (looked instanceof LightningEntity
            || (looked instanceof IInAnimate && !((IInAnimate)looked).canInteract(Race.CHANGELING))) {
                looked = null;
            }
        }

        player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PARROT_IMITATE_POLAR_BEAR, SoundCategory.PLAYERS, 1.4F, 0.4F);

        iplayer.getEffect(SpellDisguise.class).orElseGet(() -> {
            SpellDisguise disc = new SpellDisguise();

            iplayer.setEffect(disc);
            return disc;
        }).setDisguise(looked);

        iplayer.sendCapabilities(true);
    }

    @Override
    public void preApply(IPlayer player) {
        player.addEnergy(2);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }

    @Override
    public void postApply(IPlayer player) {
        player.setEnergy(0);
        player.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
    }
}
