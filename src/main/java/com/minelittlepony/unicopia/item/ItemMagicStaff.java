package com.minelittlepony.unicopia.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.spell.CasterUtils;
import com.minelittlepony.unicopia.spell.IAligned;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.ITossedEffect;
import com.minelittlepony.unicopia.spell.SpellAffinity;
import com.minelittlepony.unicopia.tossable.ITossableItem;
import com.minelittlepony.util.lang.ClientLocale;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMagicStaff extends ItemStaff implements IAligned, ITossableItem {

    @Nonnull
    private final ITossedEffect effect;

    public ItemMagicStaff(String domain, String name, @Nonnull ITossedEffect effect) {
        super(domain, name);

        this.effect = effect;
        setMaxDamage(500);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        SpellAffinity affinity = getAffinity();
        tooltip.add(affinity.getColourCode() + ClientLocale.format(affinity.getUnlocalizedName()));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (Predicates.MAGI.test(player) && hand == EnumHand.MAIN_HAND) {
            ItemStack itemstack =  player.getHeldItem(hand);

            player.setActiveHand(hand);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entity, int timeLeft) {
        if (Predicates.MAGI.test(entity) && entity instanceof EntityPlayer) {

            int i = getMaxItemUseDuration(itemstack) - timeLeft;

            if (i > 10) {
                if (canBeThrown(itemstack)) {
                    toss(world, itemstack, (EntityPlayer)entity);
                }
            }
        }
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (attacker.isSneaking()) {
            stack.damageItem(50, attacker);

            CasterUtils.toCaster(attacker).ifPresent(c -> c.subtractEnergyCost(4));

            onImpact(
                    CasterUtils.near(target),
                    target.getPosition(),
                    target.getEntityWorld().getBlockState(target.getPosition())
            );

            return true;
        }

        return false;
    }

    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;

            if (living.getActiveItemStack().getItem() == this) {
                Vec3d eyes = entity.getPositionEyes(1);

                float i = getMaxItemUseDuration(stack) - living.getItemInUseCount();

                world.spawnParticle(i > 150 ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.rand.nextGaussian() - 0.5) / 10,
                        (world.rand.nextGaussian() - 0.5) / 10,
                        (world.rand.nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 1, i / 20);

                if (i > 200) {
                    living.resetActiveHand();
                    living.attackEntityFrom(DamageSource.MAGIC, 1200);
                    onImpact(
                            CasterUtils.toCaster(entity).orElseGet(() -> CasterUtils.near(entity)),
                            entity.getPosition(),
                            entity.getEntityWorld().getBlockState(entity.getPosition())
                    );
                }
            }
        }

    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canBeThrown(ItemStack stack) {
        return true;
    }

    @Override
    public void toss(World world, ItemStack stack, EntityPlayer player) {
        IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(player);

        iplayer.subtractEnergyCost(4);
        effect.toss(iplayer);

        stack.damageItem(1, player);
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state) {
        effect.onImpact(caster, pos, state);
    }

    @Override
    public SpellAffinity getAffinity() {
        return effect.getAffinity();
    }

}
