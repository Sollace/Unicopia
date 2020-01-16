package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Predicates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityConstructionCloud extends EntityCloud {

    public EntityConstructionCloud(World world) {
        super(world);
    }

    @Override
    public boolean getStationary() {
        return true;
    }

    @Override
    public boolean getOpaque() {
        return true;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (!(isBeingRidden() || isRidingOrBeingRiddenBy(player)) && hand == EnumHand.MAIN_HAND) {
            if (Predicates.INTERACT_WITH_CLOUDS.test(player)) {

                if (player.getItemInUseCount() > 0) {
                    return EnumActionResult.FAIL;
                }

                ItemStack stack = player.getStackInHand(hand);

                if (stack != null) {
                    if (stack.getItem() instanceof ItemBlock || stack.getItem() == Items.SPAWN_EGG && stack.getItemDamage() == EntityList.getID(EntityCloud.class)) {
                        placeBlock(player, stack, hand);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        return EnumActionResult.FAIL;
    }

    private void placeBlock(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (!world.isClient || !(player instanceof EntityPlayerSP)) {
            return;
        }

        Minecraft mc = MinecraftClient.getInstance();

        double distance = mc.playerController.getBlockReachDistance();

        float ticks = mc.getRenderPartialTicks();

        Vec3d eye = player.getPositionEyes(ticks);
        Vec3d look = player.getLook(ticks);
        Vec3d ray = eye.add(look.x * distance, look.y * distance, look.z * distance);

        Box bounds = getEntityBoundingBox();

        float s = 0.5F;
        RayTraceResult trace = bounds
                    .contract(0, s, 0).contract(0, -s, 0)
                    .calculateIntercept(eye, ray);

        if (trace == null) {
            return;
        }

        EnumFacing direction = trace.sideHit;

        BlockPos blockPos = new BlockPos(trace.hitVec);

        mc.objectMouseOver = new RayTraceResult(trace.hitVec, direction, blockPos);

        int oldCount = stack.getCount();
        EnumActionResult result = mc.playerController.processRightClickBlock(((EntityPlayerSP)player), (WorldClient)player.world, blockPos, direction, trace.hitVec, hand);

        if (result == EnumActionResult.SUCCESS) {
            player.swingArm(hand);

            if (!stack.isEmpty() && (stack.getCount() != oldCount || mc.playerController.isInCreativeMode())) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress(hand);
            }
        }
    }
}
