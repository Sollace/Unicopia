package com.minelittlepony.unicopia.power;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellDisguise;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

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
    public Hit tryActivate(EntityPlayer player, World w) {
        return new Hit();
    }

    @Override
    public void apply(EntityPlayer player, Hit data) {
        Entity looked = VecHelper.getLookedAtEntity(player, 17);

        IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(player);

        player.world.playSound(null, player.getPosition(), SoundEvents.E_PARROT_IM_POLAR_BEAR, SoundCategory.PLAYERS, 1.4F, 0.4F);

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
        IPower.spawnParticles(UParticles.MAGIC_PARTICLE, player, 5);
    }

    @Override
    public void postApply(IPlayer player) {
        player.setEnergy(0);
        IPower.spawnParticles(UParticles.MAGIC_PARTICLE, player, 5);
    }
}
