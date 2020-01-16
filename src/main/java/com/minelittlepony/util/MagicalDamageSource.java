package com.minelittlepony.util;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;

public class MagicalDamageSource extends EntityDamageSource {

    public static DamageSource mundane(String type) {
        return new DamageSource(type) {};
    }

    public static DamageSource create(String type) {
        return new MagicalDamageSource(type);
    }

    public static DamageSource causePlayerDamage(String type, PlayerEntity player) {
        return causeMobDamage(type, player);
    }

    public static DamageSource causeMobDamage(String type, LivingEntity source) {
        return new MagicalDamageSource(type, source);
    }

    public static DamageSource causeIndirect(String type, ArrowEntity source, @Nullable Entity instigator) {
        return new ProjectileDamageSource(type, source, instigator).setProjectile();
    }

    protected MagicalDamageSource(String type) {
        this(type, null);
    }

    protected MagicalDamageSource(String type, Entity source) {
        super(type, source);
        setUsesMagic();
    }

    @Override
    public Text getDeathMessage(LivingEntity target) {
        Entity attacker = source instanceof LivingEntity ? (LivingEntity)source : target.getVehicle();
        String basic = "death.attack." + name;

        if (attacker != null && attacker instanceof LivingEntity) {
            String withAttecker = basic + ".player";
            ItemStack held = ((LivingEntity)attacker).getMainHandStack();

            if (!held.isEmpty() && held.hasCustomName()) {
                return new TranslatableText(withAttecker + ".item", target.getDisplayName(), attacker.getDisplayName(), held.toHoverableText());
            }

            if (Language.getInstance().hasTranslation(withAttecker)) {
                return new TranslatableText(withAttecker, target.getDisplayName(), attacker.getDisplayName());
            }
        }

        return new TranslatableText(basic, target.getDisplayName(), source.getDisplayName());
    }
}
