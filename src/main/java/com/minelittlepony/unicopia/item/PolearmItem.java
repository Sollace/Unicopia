package com.minelittlepony.unicopia.item;

import java.util.List;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PolearmItem extends Item {
    public PolearmItem(ToolMaterial material, int damage, float speed, int range, Settings settings) {
        super(settings.maxDamage(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue())
                .attributeModifiers(createToolAttributeModifiers(material, damage, speed).with(
                UEntityAttributes.EXTENDED_REACH_DISTANCE, new EntityAttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
        ).with(
                UEntityAttributes.EXTENDED_ATTACK_DISTANCE, new EntityAttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
        )).component(DataComponentTypes.TOOL, createToolComponent()));
    }

    private static AttributeModifiersComponent createToolAttributeModifiers(ToolMaterial material, float attackDamage, float attackSpeed) {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                    new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, attackDamage + material.attackDamageBonus(), EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, attackSpeed, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @SuppressWarnings("deprecation")
    private static ToolComponent createToolComponent() {
        return new ToolComponent(
            List.of(
                ToolComponent.Rule.ofAlwaysDropping(RegistryEntryList.of(Blocks.COBWEB.getRegistryEntry()), 15),
                ToolComponent.Rule.of(Registries.createEntryLookup(Registries.BLOCK).getOrThrow(UTags.Blocks.POLEARM_MINEABLE), 1.5F)
            ), 1, 2
        );
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean tooNear = target.distanceTo(attacker) <= 2;
        target.takeKnockback(0.15, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        Living.updateVelocity(target);
        if (tooNear) {
            attacker.takeKnockback(attacker.getRandom().nextTriangular(0.4, 0.2), target.getX() - attacker.getX(), target.getZ() - attacker.getZ());
            Living.updateVelocity(attacker);
        }

        return true;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean tooNear = target.distanceTo(attacker) <= 2;
        stack.damage(tooNear ? 4 : 1, attacker, EquipmentSlot.MAINHAND);
    }
}
