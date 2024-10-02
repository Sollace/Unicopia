package com.minelittlepony.unicopia.item;

import java.util.List;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PolearmItem extends ToolItem {
    public PolearmItem(ToolMaterial material, int damage, float speed, int range, Settings settings) {
        super(material, settings.attributeModifiers(SwordItem.createAttributeModifiers(material, damage, speed).with(
                UEntityAttributes.EXTENDED_REACH_DISTANCE, new EntityAttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
        ).with(
                UEntityAttributes.EXTENDED_ATTACK_DISTANCE, new EntityAttributeModifier(UItemModifierIds.ATTACK_RANGE_MODIFIER_ID, range, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
        )).component(DataComponentTypes.TOOL, createToolComponent()));
    }

    private static ToolComponent createToolComponent() {
        return new ToolComponent(
            List.of(ToolComponent.Rule.ofAlwaysDropping(List.of(Blocks.COBWEB), 15.0F), ToolComponent.Rule.of(UTags.Blocks.POLEARM_MINEABLE, 1.5F)), 1.0F, 2
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
