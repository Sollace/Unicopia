package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.UEntityAttributes;

import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StaffItem extends SwordItem {

    protected static final UUID ATTACK_REACH_MODIFIER = UUID.fromString("FA235E1C-4280-A865-B01B-CBAE9985ACA3");

    public StaffItem(Settings settings) {
        super(ToolMaterials.WOOD, 2, 4, settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        World w = player.getEntityWorld();

        EntityDimensions dims = target.getDimensions(target.getPose());

        for (int i = 0; i < 130; i++) {
            w.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LOG.getDefaultState()),
                    target.getX() + (target.world.random.nextFloat() - 0.5F) * (dims.width + 1),
                    (target.getY() + dims.height / 2) + (target.world.random.nextFloat() - 0.5F) * dims.height,
                    target.getZ() + (target.world.random.nextFloat() - 0.5F) * (dims.width + 1),
                    0, 0, 0
            );
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey(stack) + ".lore").formatted(Formatting.GRAY));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
        super.postHit(stack, entity, attacker);

        return castContainedEffect(stack, entity, attacker);
    }

    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.getEntityWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1, 1);

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

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap = HashMultimap.create();

        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", getAttackDamage(), Operation.ADDITION));
            multimap.put(UEntityAttributes.EXTENDED_ATTACK_DISTANCE, new EntityAttributeModifier(ATTACK_REACH_MODIFIER, "Weapon modifier", 3, Operation.ADDITION));
        }

        return multimap;
    }
}
