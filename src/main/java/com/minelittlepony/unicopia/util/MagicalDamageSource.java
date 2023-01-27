package com.minelittlepony.unicopia.util;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class MagicalDamageSource extends EntityDamageSource {
    public static final DamageSource EXHAUSTION = new MagicalDamageSource("magical_exhaustion", null, true, true);
    public static final DamageSource ALICORN_AMULET = new MagicalDamageSource("alicorn_amulet", null, true, true);
    public static final DamageSource FOOD_POISONING = new DamageSource("food_poisoning");
    public static final DamageSource TRIBE_SWAP = new DamageSource("tribe_swap").setOutOfWorld().setUnblockable();
    public static final DamageSource ZAP_APPLE = create("zap");
    public static final DamageSource KICK = create("kick");
    public static final DamageSource SUN = new DamageSource("sun").setBypassesArmor().setFire();
    public static final DamageSource SUNLIGHT = new DamageSource("sunlight").setBypassesArmor().setFire();
    public static final DamageSource PETRIFIED = new DamageSource("petrified").setBypassesArmor().setFire();

    public static MagicalDamageSource create(String type) {
        return new MagicalDamageSource(type, null, null, false, false);
    }

    public static MagicalDamageSource create(String type, @Nullable LivingEntity source) {
        return new MagicalDamageSource(type, source, null, false, false);
    }

    public static MagicalDamageSource create(String type, Caster<?> caster) {
        return new MagicalDamageSource(type, caster.getMaster(), caster, false, false);
    }

    @Nullable
    private Caster<?> spell;

    private boolean breakSunglasses;

    protected MagicalDamageSource(String type, @Nullable Caster<?> spell, boolean direct, boolean unblockable) {
        this(type, null, spell, direct, unblockable);
    }

    protected MagicalDamageSource(String type, @Nullable Entity source, @Nullable Caster<?> spell, boolean direct, boolean unblockable) {
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

    public MagicalDamageSource setBreakSunglasses() {
        breakSunglasses = true;
        return this;
    }

    public boolean breaksSunglasses() {
        return breakSunglasses;
    }

    @Nullable
    public Caster<?> getSpell() {
        return spell;
    }

    @Override
    public Text getDeathMessage(LivingEntity target) {

        String basic = "death.attack." + name;

        List<Text> params = new ArrayList<>();
        params.add(target.getDisplayName());

        @Nullable
        Entity attacker = source != null ? source : target.getPrimeAdversary();
        ItemStack item = attacker instanceof LivingEntity ? ((LivingEntity)attacker).getMainHandStack() : ItemStack.EMPTY;

        if (attacker == target) {
            basic += ".self";

            if (!item.isEmpty() && item.hasCustomName()) {
                basic += ".item";
                params.add(item.toHoverableText());
            }
        } else if (!item.isEmpty() && item.hasCustomName()) {
            basic += ".item";
            params.add(attacker.getDisplayName());
            params.add(item.toHoverableText());
        } else if (attacker != null) {
            basic += ".player";
            params.add(attacker.getDisplayName());
        }

        Text message = Text.translatable(basic, params.toArray());
        return Pony.of(target).filter(e -> e.getSpecies().canFly()).map(pony -> {

            if (pony.getPhysics().isFlying()) {
                return Text.translatable("death.attack.generic.whilst_flying", message);
            }
            return message;
        }).orElse(message);
    }
}
