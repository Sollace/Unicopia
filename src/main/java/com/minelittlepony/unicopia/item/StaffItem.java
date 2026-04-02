package com.minelittlepony.unicopia.item;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.USounds;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class StaffItem extends Item {
    public StaffItem(Settings settings) {
        super(settings.sword(ToolMaterial.WOOD, 2, -3));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        World w = player.getEntityWorld();

        EntityDimensions dims = target.getDimensions(target.getPose());

        for (int i = 0; i < 130; i++) {
            w.addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LOG.getDefaultState()),
                    target.getX() + (target.getWorld().random.nextFloat() - 0.5F) * (dims.width() + 1),
                    (target.getY() + dims.height() / 2) + (target.getWorld().random.nextFloat() - 0.5F) * dims.height(),
                    target.getZ() + (target.getWorld().random.nextFloat() - 0.5F) * (dims.width() + 1),
                    0, 0, 0
            );
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable(getTranslationKey() + ".lore").formatted(Formatting.GRAY));
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
        super.postHit(stack, entity, attacker);

        // TODO: Maybe swing?
        castContainedEffect(stack, entity, attacker);
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
