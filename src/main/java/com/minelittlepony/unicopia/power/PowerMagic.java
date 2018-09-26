package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.unicopia.spell.SpellShield;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PowerMagic implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.magic";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_P;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 20;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 0;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canCast();
    }

    @Override
    public Hit tryActivate(EntityPlayer player, World w) {
        return new Hit();
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(EntityPlayer player, Hit data) {
        IPlayer prop = PlayerSpeciesList.instance().getPlayer(player);

        if (prop.getEffect() instanceof SpellShield) {
            prop.setEffect(null);
        } else {
            prop.setEffect(new SpellShield());
        }
    }

    @Override
    public void preApply(IPlayer player) {

    }

    @Override
    public void postApply(IPlayer player) {

    }
}
