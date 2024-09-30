package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Enchantments;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;

public class CollaboratorEnchantment {
    private static final Identifier TEAM_STRENGTH_ID = Unicopia.id("team_strength");

    protected CollaboratorEnchantment() {
        //addModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, this::getModifier);
    }

    protected boolean shouldChangeModifiers(Living<?> user, int level) {
        return false;//super.shouldChangeModifiers(user, getTeamCollectiveLevel(user, 2 + (level * 2)));
    }

    private EntityAttributeModifier getModifier(Living<?> user, int level) {
        return new EntityAttributeModifier(TEAM_STRENGTH_ID, user.getEnchants().computeIfAbsent(UEnchantments.HERDS, Enchantments.Data::new).level / 2, Operation.ADD_VALUE);
    }

    private static int getTeamCollectiveLevel(Living<?> user, int radius) {
        return user.findAllEntitiesInRange(radius, e -> e instanceof LivingEntity)
                .mapToInt(e -> EnchantmentUtil.getLevel(UEnchantments.HERDS, (LivingEntity)e))
                .reduce((a, b) -> a + b)
                .orElse(0);
    }
}
