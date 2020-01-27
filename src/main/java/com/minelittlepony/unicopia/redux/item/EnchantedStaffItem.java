package com.minelittlepony.unicopia.redux.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.EquinePredicates;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.CasterUtils;
import com.minelittlepony.unicopia.core.magic.IAffine;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.redux.magic.ITossedEffect;
import com.minelittlepony.unicopia.redux.util.projectile.ITossableItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnchantedStaffItem extends StaffItem implements IAffine, ITossableItem {

    @Nonnull
    private final ITossedEffect effect;

    public EnchantedStaffItem(Settings settings, @Nonnull ITossedEffect effect) {
        super(settings.maxDamage(500));

        this.effect = effect;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getAffinity().getName());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (EquinePredicates.MAGI.test(player) && hand == Hand.MAIN_HAND) {
            ItemStack itemstack =  player.getStackInHand(hand);

            player.swingHand(hand);

            return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
        }

        return super.use(world, player, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack itemstack, World world, LivingEntity entity, int timeLeft) {
        if (EquinePredicates.MAGI.test(entity) && entity instanceof PlayerEntity) {

            int i = getMaxUseTime(itemstack) - timeLeft;

            if (i > 10 && canBeThrown(itemstack)) {
                toss(world, itemstack, (PlayerEntity)entity);
            }
        }
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isSneaking()) {
            stack.damage(50, attacker, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));

            CasterUtils.toCaster(attacker).ifPresent(c -> c.subtractEnergyCost(4));
            CasterUtils.toCaster(target).ifPresent(c -> onImpact(
                    c,
                    target.getBlockPos(),
                    target.getEntityWorld().getBlockState(target.getBlockPos())
            ));

            return true;
        }

        return false;
    }

    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;

            if (living.getActiveItem().getItem() == this) {
                Vec3d eyes = entity.getCameraPosVec(1);

                float i = getMaxUseTime(stack) - living.getItemUseTimeLeft();

                world.addParticle(i > 150 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 1, i / 20);

                if (i > 200) {
                    living.clearActiveItem();
                    living.damage(DamageSource.MAGIC, 1200);
                    CasterUtils.toCaster(entity).ifPresent(c -> onImpact(
                            c,
                            entity.getBlockPos(),
                            entity.getEntityWorld().getBlockState(entity.getBlockPos())
                    ));
                }
            }
        }

    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void toss(World world, ItemStack stack, PlayerEntity player) {
        IPlayer iplayer = SpeciesList.instance().getPlayer(player);

        iplayer.subtractEnergyCost(4);
        effect.toss(iplayer);

        stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, BlockState state) {
        effect.onImpact(caster, pos, state);
    }

    @Override
    public Affinity getAffinity() {
        return effect.getAffinity();
    }

}
