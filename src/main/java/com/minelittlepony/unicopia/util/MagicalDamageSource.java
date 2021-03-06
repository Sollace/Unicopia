package com.minelittlepony.unicopia.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MagicalDamageSource extends EntityDamageSource {

    public static final DamageSource EXHAUSTION = new MagicalDamageSource("magical_exhaustion", null, true, true);
    public static final DamageSource FOOD_POISONING = mundane("food_poisoning");
    public static final DamageSource TRIBE_SWAP = mundane("tribe_swap");
    public static final DamageSource ZAP_APPLE = create("zap");

    public static DamageSource mundane(String type) {
        return new DamageSource(type) {};
    }

    public static DamageSource create(String type) {
        return new MagicalDamageSource(type, null, null, false, false);
    }

    public static DamageSource create(String type, LivingEntity source) {
        return new MagicalDamageSource(type, source, null, false, false);
    }

    public static DamageSource create(String type, Caster<?> caster) {
        return new MagicalDamageSource(type, caster.getMaster(), caster.getEntity(), false, false);
    }

    private Entity spell;

    protected MagicalDamageSource(String type, @Nullable Entity spell, boolean direct, boolean unblockable) {
        this(type, null, spell, direct, unblockable);
    }

    protected MagicalDamageSource(String type, @Nullable Entity source, @Nullable Entity spell, boolean direct, boolean unblockable) {
        super(type, source);
        this.spell = spell;
        setUsesMagic();
        if (direct) {
            setBypassesArmor();
        }
        if (unblockable) {
            setUnblockable();
        }
    }

    @Nullable
    public Entity getSpell() {
        return spell;
    }

    @Override
    public Text getDeathMessage(LivingEntity target) {

        String basic = "death.attack." + name;

        List<Text> params = new ArrayList<>();
        params.add(target.getDisplayName());

        @Nullable
        Entity attacker = source != null ? source : target.getPrimeAdversary();

        if (attacker != null) {
            if (attacker == target) {
                basic += ".self";
            } else {
                basic += ".attacker";
                params.add(attacker.getDisplayName());
            }
        }

        ItemStack item = attacker instanceof LivingEntity ? ((LivingEntity)attacker).getMainHandStack() : ItemStack.EMPTY;

        if (!item.isEmpty() && item.hasCustomName()) {
            basic += ".item";
            params.add(item.toHoverableText());
        }

        return new TranslatableText(basic, params.toArray());
    }
}
