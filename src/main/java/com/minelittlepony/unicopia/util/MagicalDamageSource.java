package com.minelittlepony.unicopia.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MagicalDamageSource extends EntityDamageSource {

    public static final DamageSource FOOD_POISONING = mundane("food_poisoning");
    public static final DamageSource ZAP_APPLE = create("zap");

    public static DamageSource mundane(String type) {
        return new DamageSource(type) {};
    }

    public static DamageSource create(String type) {
        return new MagicalDamageSource(type, false, false);
    }

    public static DamageSource create(String type, LivingEntity source) {
        return new MagicalDamageSource(type, source, false, false);
    }

    protected MagicalDamageSource(String type, boolean direct, boolean unblockable) {
        this(type, null, direct, unblockable);
    }

    protected MagicalDamageSource(String type, Entity source, boolean direct, boolean unblockable) {
        super(type, source);
        setUsesMagic();
        if (direct) {
            setBypassesArmor();
        }
        if (unblockable) {
            setUnblockable();
        }
    }

    @Override
    public Text getDeathMessage(LivingEntity target) {

        String basic = "death.attack." + name;

        List<Text> params = new ArrayList<>();
        params.add(target.getDisplayName());

        @Nullable
        Entity attacker = source instanceof LivingEntity ? source : target.getPrimeAdversary();

        if (attacker instanceof LivingEntity) {
            if (attacker == target) {
                basic += ".self";
            } else {
                basic += ".attacker";
                params.add(((LivingEntity)attacker).getDisplayName());
            }

            ItemStack item = ((LivingEntity)attacker).getMainHandStack();
            if (!item.isEmpty() && item.hasCustomName()) {
                basic += ".item";
                params.add(item.toHoverableText());
            }
        }

        return new TranslatableText(basic, params.toArray());
    }
}
