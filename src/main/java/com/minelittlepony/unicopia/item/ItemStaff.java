package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.util.lang.ClientLocale;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemStaff extends ItemSword implements ITossable {

    protected static final UUID ATTACK_REACH_MODIFIER = UUID.fromString("FA235E1C-4280-A865-B01B-CBAE9985ACA3");

    public ItemStaff(String domain, String name) {
        super(ToolMaterial.WOOD);

        setTranslationKey(name);
        setRegistryName(domain, name);

        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (canBeThrown(itemstack)) {
            toss(world, itemstack, player);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
        World w = player.getEntityWorld();

        for (int i = 0; i < 130; i++) {
            w.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    target.posX + (target.world.rand.nextFloat() - 0.5F) * (target.width + 1),
                    (target.posY + target.height/2) + (target.world.rand.nextFloat() - 0.5F) * target.height,
                    target.posZ + (target.world.rand.nextFloat() - 0.5F) * (target.width + 1),
                    0, 0, 0,
                    Block.getStateId(Blocks.LOG.getDefaultState())
            );
        }

        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ClientLocale.format(getTranslationKey(stack) + ".tagline"));
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        super.hitEntity(stack, target, attacker);

        if (Predicates.MAGI.test(attacker)) {
            return castContainedEffect(stack, target, attacker);
        }

        return false;
    }

    protected boolean castContainedEffect(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1, 1);

        target.knockBack(attacker, 2,
                MathHelper.sin(attacker.rotationYaw * 0.017453292F),
               -MathHelper.cos(attacker.rotationYaw * 0.017453292F)
        );

        return true;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public boolean canBeThrown(ItemStack stack) {
        return false;
    }

    @Override
    public void onImpact(World world, BlockPos pos, IBlockState state) {

    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();

        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(), 0));
            multimap.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(ATTACK_REACH_MODIFIER, "Weapon modifier", 3, 0));
        }

        return multimap;
    }
}
