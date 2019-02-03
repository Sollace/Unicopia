package com.minelittlepony.unicopia.power;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;

public class PowerFeed implements IPower<Hit> {

    @Override
    public String getKeyName() {
        return "unicopia.power.feed";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_N;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 20;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 50;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(IPlayer player) {
        if (player.getOwner().getHealth() < player.getOwner().getMaxHealth() || player.getOwner().canEat(false)) {
            Entity i = VecHelper.getLookedAtEntity(player.getOwner(), 15);
            if (i != null && canDrain(i)) {
                return new Hit();
            }
        }

        return null;
    }

    private boolean canDrain(Entity e) {
        return e instanceof EntityCow
            || e instanceof EntityVillager
            || e instanceof EntityPlayer
            || e instanceof EntitySheep
            || e instanceof EntityPig
            || EnumCreatureType.MONSTER.getCreatureClass().isAssignableFrom(e.getClass());
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    @Override
    public void apply(IPlayer iplayer, Hit data) {
        EntityPlayer player = iplayer.getOwner();
        List<Entity> list = VecHelper.getWithinRange(player, 3, this::canDrain);

        Entity looked = VecHelper.getLookedAtEntity(player, 17);
        if (looked != null && !list.contains(looked)) {
            list.add(looked);
        }

        float lostHealth = player.getMaxHealth() - player.getHealth();

        if (lostHealth > 0 || player.canEat(false)) {
            float totalDrained = (lostHealth < 2 ? lostHealth : 2);
            float drained = totalDrained / list.size();

            for (Entity i : list) {
                DamageSource d = MagicalDamageSource.causePlayerDamage("feed", player);

                if (iplayer.getWorld().rand.nextFloat() > 0.95f) {
                    i.attackEntityFrom(d, Integer.MAX_VALUE);
                } else {
                    i.attackEntityFrom(d, drained);
                }
            }

            if (lostHealth > 0) {
                player.getFoodStats().addStats(3, 0.125f);
                player.heal(totalDrained);
            } else {
                player.getFoodStats().addStats(3, 0.25f);
            }

            if (iplayer.getWorld().rand.nextFloat() > 0.9f) {
                player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20, 1));
            }

            player.removePotionEffect(MobEffects.NAUSEA);
        }
    }

    @Override
    public void preApply(IPlayer player) {
        player.addExertion(6);
    }

    @Override
    public void postApply(IPlayer player) {
        EntityPlayer entity = player.getOwner();

        for (int i = 0; i < 10; i++) {
            Particles.instance().spawnParticle(EnumParticleTypes.HEART.getParticleID(), false,
                    entity.posX + entity.world.rand.nextFloat() * 2 - 1,
                    entity.posY + entity.world.rand.nextFloat() * 2 - 1,
                    entity.posZ + entity.world.rand.nextFloat() * 2 - 1,
                    0, 0.25, 0);
        }
    }

}
