package com.minelittlepony.unicopia.item;

import java.util.List;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;

import net.minecraft.block.Blocks;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StaffItem extends SwordItem {

    protected static final Identifier ATTACK_REACH_MODIFIER_ID = Unicopia.id("attack_reach");

    public StaffItem(Settings settings) {
        super(ToolMaterials.WOOD, settings.attributeModifiers(createAttributeModifiers(2, 3)));
    }

    public static AttributeModifiersComponent createAttributeModifiers(int attackDamage, float attackReach) {
        return AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(
                    BASE_ATTACK_DAMAGE_MODIFIER_ID, (attackDamage), EntityAttributeModifier.Operation.ADD_VALUE
                ),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                UEntityAttributes.EXTENDED_ATTACK_DISTANCE,
                new EntityAttributeModifier(ATTACK_REACH_MODIFIER_ID, attackReach, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND
            )
            .build();
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        World w = player.getEntityWorld();

        EntityDimensions dims = target.getDimensions(target.getPose());

        for (int i = 0; i < 130; i++) {
            w.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LOG.getDefaultState()),
                    target.getX() + (target.getWorld().random.nextFloat() - 0.5F) * (dims.width() + 1),
                    (target.getY() + dims.height() / 2) + (target.getWorld().random.nextFloat() - 0.5F) * dims.height(),
                    target.getZ() + (target.getWorld().random.nextFloat() - 0.5F) * (dims.width() + 1),
                    0, 0, 0
            );
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(getTranslationKey(stack) + ".lore").formatted(Formatting.GRAY));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
        super.postHit(stack, entity, attacker);

        return castContainedEffect(stack, entity, attacker);
    }

    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.getEntityWorld().playSound(null, target.getBlockPos(), USounds.ITEM_STAFF_STRIKE, attacker.getSoundCategory(), 1, 1);

        target.takeKnockback(attacker.getVelocity().subtract(target.getVelocity()).horizontalLength(),
                MathHelper.sin(attacker.getYaw() * 0.017453292F),
               -MathHelper.cos(attacker.getYaw() * 0.017453292F)
        );

        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
