package com.minelittlepony.unicopia.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.UEntityAttributes;

import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class PolearmItem extends SwordItem {
    protected static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.fromString("A7B3659C-AA74-469C-963A-09A391DCAA0F");

    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    private final int attackRange;

    public PolearmItem(ToolMaterial material, int damage, float speed, int range, Settings settings) {
        super(material, damage, speed, settings);
        this.attackRange = range;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(EquipmentSlot.MAINHAND));
        builder.put(UEntityAttributes.EXTENDED_REACH_DISTANCE, new EntityAttributeModifier(ATTACK_RANGE_MODIFIER_ID, "Weapon modifier", attackRange, EntityAttributeModifier.Operation.ADDITION));
        attributeModifiers = builder.build();
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isOf(Blocks.COBWEB)) {
            return 1;
        }
        return super.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return false;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean tooNear = target.distanceTo(attacker) <= 2;
        stack.damage(tooNear ? 4 : 1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        target.takeKnockback(0.15, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        if (tooNear) {
            attacker.takeKnockback(attacker.getRandom().nextTriangular(0.4, 0.2), target.getX() - attacker.getX(), target.getZ() - attacker.getZ());
            if (attacker instanceof ServerPlayerEntity ply) {
                ply.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ply));
            }
        }

        return true;
    }
}
