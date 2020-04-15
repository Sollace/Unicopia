package com.minelittlepony.unicopia.ability;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.ChangelingTrapSpell;

public class ChangelingTrapAbility implements Ability<Ability.Hit> {

    @Override
    public String getKeyName() {
        return "engulf";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_L;
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 30;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        return new Hit();
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(Pony player, Hit data) {
        new ChangelingTrapSpell().toss(player);
    }

    @Override
    public void preApply(Pony player) {

    }

    @Override
    public void postApply(Pony player) {

    }
}
