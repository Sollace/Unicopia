package com.minelittlepony.unicopia.item.enchantment;

import java.util.UUID;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;

public class CollaboratorEnchantment extends AttributedEnchantment {
    private static final UUID TEAM_STRENGTH_UUID = UUID.fromString("5f08c02d-d959-4763-ac84-16e2acfd4b62");

    protected CollaboratorEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, false, 3, EquipmentSlot.MAINHAND);
        this.addModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, this::getModifier);
    }

    @Override
    protected boolean shouldChangeModifiers(Living<?> user, int level) {
        return super.shouldChangeModifiers(user, getTeamCollectiveLevel(user, 2 + (level * 2)));
    }

    private EntityAttributeModifier getModifier(Living<?> user, int level) {
        return new EntityAttributeModifier(TEAM_STRENGTH_UUID, "Team Strength", user.getEnchants().computeIfAbsent(this, Data::new).level / 2, Operation.ADDITION);
    }

    private int getTeamCollectiveLevel(Living<?> user, int radius) {
        return user.findAllEntitiesInRange(radius, e -> e instanceof LivingEntity)
                .mapToInt(e -> EnchantmentHelper.getEquipmentLevel(this, (LivingEntity)e))
                .reduce((a, b) -> a + b)
                .orElse(0);
    }
}
