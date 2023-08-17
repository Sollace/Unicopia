package com.minelittlepony.unicopia.util;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class MagicalDamageSource extends DamageSource {
    @Nullable
    private final Caster<?> spell;

    public MagicalDamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Caster<?> caster) {
        super(type, source, attacker);
        spell = caster;
    }

    public MagicalDamageSource(RegistryEntry<DamageType> type, Vec3d position, @Nullable Caster<?> caster) {
        super(type, position);
        spell = caster;
    }

    public MagicalDamageSource(RegistryEntry<DamageType> type, @Nullable Entity attacker, @Nullable Caster<?> caster) {
        super(type, attacker);
        spell = caster;
    }

    public MagicalDamageSource(RegistryEntry<DamageType> type, @Nullable Caster<?> caster) {
        super(type);
        spell = caster;
    }

    @Nullable
    public Caster<?> getSpell() {
        return spell;
    }

    @Override
    public Text getDeathMessage(LivingEntity target) {

        String basic = "death.attack." + getName();

        List<Text> params = new ArrayList<>();
        params.add(target.getDisplayName());

        @Nullable
        Entity attacker = getSource() != null ? getSource() : target.getPrimeAdversary();
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
                return Text.translatable("death.attack.unicopia.generic.whilst_flying", message);
            }
            return message;
        }).orElse(message);
    }
}
