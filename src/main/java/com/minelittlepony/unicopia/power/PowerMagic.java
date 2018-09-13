package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.spell.SpellShield;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PowerMagic implements IPower<PowerMagic.Magic> {

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
       // if (player.hasEffect() && "shield".contentEquals(player.getEffect().getName())) {
       //     return 0;
       // }

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
    public Magic tryActivate(EntityPlayer player, World w) {
        return new Magic(0);
    }

    @Override
    public Class<Magic> getPackageType() {
        return Magic.class;
    }

    @Override
    public void apply(EntityPlayer player, Magic data) {
        IPlayer prop = PlayerSpeciesList.instance().getPlayer(player);

        if (prop.getEffect() instanceof SpellShield) {
            prop.setEffect(null);
        } else {
            prop.setEffect(new SpellShield(data.type));
        }
    }

    @Override
    public void preApply(IPlayer player) {

    }

    @Override
    public void postApply(IPlayer player) {

    }

    class Magic implements IData {

        @Expose
        public int type;

        public Magic(int strength) {
            type = strength;
        }
    }
}
