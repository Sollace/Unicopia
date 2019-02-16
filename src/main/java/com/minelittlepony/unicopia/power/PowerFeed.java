package com.minelittlepony.unicopia.power;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Hit;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
        return Keyboard.KEY_O;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 5;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return canFeed(player) ? 15 : 80;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(IPlayer player) {
        if (canFeed(player)) {
            if (!getTargets(player).isEmpty()) {
                return new Hit();
            }
        }

        return null;
    }

    private boolean canFeed(IPlayer player) {
        return player.getOwner().getHealth() < player.getOwner().getMaxHealth() || player.getOwner().canEat(false);
    }

    private boolean canDrain(Entity e) {
        return (e instanceof EntityLivingBase)
            && (e instanceof EntityCow
            || e instanceof EntityVillager
            || e instanceof EntityPlayer
            || e instanceof EntitySheep
            || e instanceof EntityPig
            || EnumCreatureType.MONSTER.getCreatureClass().isAssignableFrom(e.getClass()));
    }

    @Override
    public Class<Hit> getPackageType() {
        return Hit.class;
    }

    protected List<EntityLivingBase> getTargets(IPlayer player) {
        List<Entity> list = VecHelper.getWithinRange(player.getOwner(), 3, this::canDrain);

        Entity looked = VecHelper.getLookedAtEntity(player.getOwner(), 17);
        if (looked != null && !list.contains(looked) && canDrain(looked)) {
            list.add(looked);
        }

        return list.stream().map(i -> (EntityLivingBase)i).collect(Collectors.toList());
    }

    @Override
    public void apply(IPlayer iplayer, Hit data) {
        EntityPlayer player = iplayer.getOwner();

        float maximumHealthGain = player.getMaxHealth() - player.getHealth();
        int maximumFoodGain = player.canEat(false) ? (20 - player.getFoodStats().getFoodLevel()) : 0;

        if (maximumHealthGain > 0 || maximumFoodGain > 0) {

            float healAmount = 0;

            for (EntityLivingBase i : getTargets(iplayer)) {
                healAmount += drainFrom(player, i);
            }

            int foodAmount = (int)Math.floor(Math.min(healAmount / 3, maximumFoodGain));

            if (foodAmount > 0) {
                healAmount -= foodAmount;
                player.getFoodStats().addStats(foodAmount, 0.125f);
            }

            if (healAmount > 0) {
                player.heal(Math.min(healAmount, maximumHealthGain));
            }
        }
    }

    protected float drainFrom(EntityPlayer changeling, EntityLivingBase living) {
        DamageSource d = MagicalDamageSource.causePlayerDamage("feed", changeling);

        float damage = living.getHealth()/2;

        if (damage > 0) {
            living.attackEntityFrom(d, damage);
        }

        IPower.spawnParticles(UParticles.CHANGELING_MAGIC, living, 7);

        if (changeling.isPotionActive(MobEffects.NAUSEA)) {
            living.addPotionEffect(changeling.removeActivePotionEffect(MobEffects.NAUSEA));
        } else if (changeling.getEntityWorld().rand.nextInt(2300) == 0) {
            living.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20, 1));
        }

        if (living instanceof EntityPlayer) {
            damage ++;
            damage *= 1.6F;

            if (!changeling.isPotionActive(MobEffects.HEALTH_BOOST)) {
                changeling.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 13000, 1));
            }
        }

        return damage;
    }

    @Override
    public void preApply(IPlayer player) {
        player.addExertion(6);
    }

    @Override
    public void postApply(IPlayer player) {
        EntityPlayer entity = player.getOwner();

        IPower.spawnParticles(EnumParticleTypes.HEART.getParticleID(), entity, 1);
    }

}
