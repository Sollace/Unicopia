package com.minelittlepony.util;

import javax.annotation.Nullable;

import com.minelittlepony.util.lang.ServerLocale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class MagicalDamageSource extends EntityDamageSource {

    public static DamageSource create(String type) {
        return new MagicalDamageSource(type);
    }

    public static DamageSource causePlayerDamage(String type, EntityPlayer player) {
        return causeMobDamage(type, player);
    }

    public static DamageSource causeMobDamage(String type, EntityLivingBase source) {
        return new MagicalDamageSource(type, source);
    }

    public static DamageSource causeIndirect(String type, EntityArrow source, @Nullable Entity instigator) {
        return new EntityDamageSourceIndirect(type, source, instigator).setProjectile();
    }

    protected MagicalDamageSource(String type) {
        this(type, null);
    }

    protected MagicalDamageSource(String type, Entity source) {
        super(type, source);
        setMagicDamage();
    }

    public ITextComponent getDeathMessage(EntityLivingBase target) {
        Entity attacker = damageSourceEntity instanceof EntityLivingBase ? (EntityLivingBase)damageSourceEntity : target.getRidingEntity();
        String basic = "death.attack." + this.damageType;

        if (attacker != null && attacker instanceof EntityLivingBase) {
            String withAttecker = basic + ".player";
            ItemStack held = attacker instanceof EntityLivingBase ? ((EntityLivingBase)attacker).getHeldItemMainhand() : ItemStack.EMPTY;

            String withItem = withAttecker + ".item";
            if (held != null && held.hasDisplayName() && ServerLocale.hasKey(withItem)) {
                return new TextComponentTranslation(withItem, target.getDisplayName(), attacker.getDisplayName(), held.getTextComponent());
            }

            if (ServerLocale.hasKey(withAttecker)) {
                return new TextComponentTranslation(withAttecker, target.getDisplayName(), attacker.getDisplayName());
            }
        }

        return new TextComponentTranslation(basic, target.getDisplayName());
    }
}
