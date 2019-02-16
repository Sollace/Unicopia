package com.minelittlepony.unicopia.power;


import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.IInAnimate;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellDisguise;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;

public class PowerDisguise extends PowerFeed {

    @Override
    public String getKeyName() {
        return "unicopia.power.disguise";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_P;
    }

    @Nullable
    @Override
    public Hit tryActivate(IPlayer player) {
        return new Hit();
    }

    @Override
    public void apply(IPlayer iplayer, Hit data) {
        EntityPlayer player = iplayer.getOwner();
        RayTraceResult trace = VecHelper.getObjectMouseOver(player, 10, 1);

        Entity looked = trace.entityHit;

        if (trace.typeOfHit == RayTraceResult.Type.BLOCK) {
            IBlockState state = player.getEntityWorld().getBlockState(trace.getBlockPos());

            if (!state.getBlock().isAir(state, player.getEntityWorld(), trace.getBlockPos())) {
                looked = new EntityFallingBlock(player.getEntityWorld(), 0, 0, 0, state);
            }
        }

        if (looked instanceof EntityWeatherEffect || (looked instanceof IInAnimate && !((IInAnimate)looked).canInteract(Race.CHANGELING))) {
            looked = null;
        }

        if (looked instanceof EntityPlayer) {
            IPlayer ilooked = PlayerSpeciesList.instance().getPlayer((EntityPlayer)looked);
            IMagicEffect ef = ilooked.getEffect();
            if (ef instanceof SpellDisguise && !ef.getDead()) {
                Entity e = ((SpellDisguise)ef).getDisguise();
                if (e != null) {
                    looked = e;
                }
            }
        }

        player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.E_PARROT_IM_POLAR_BEAR, SoundCategory.PLAYERS, 1.4F, 0.4F);

        IMagicEffect effect = iplayer.getEffect();
        if (effect instanceof SpellDisguise && !effect.getDead()) {
            ((SpellDisguise)effect).setDisguise(looked);
        } else if (looked != null) {
            iplayer.setEffect(new SpellDisguise().setDisguise(looked));
        }

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
