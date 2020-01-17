package com.minelittlepony.unicopia.entity;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConstructionCloudEntity extends CloudEntity {

    public ConstructionCloudEntity(EntityType<ConstructionCloudEntity> type, World world) {
        super(type, world);
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
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!(hasPassengers() || isConnectedThroughVehicle(player)) && hand == Hand.MAIN_HAND) {
            if (EquinePredicates.INTERACT_WITH_CLOUDS.test(player)) {

                if (player.getItemUseTime() > 0) {
                    return ActionResult.FAIL;
                }

                ItemStack stack = player.getStackInHand(hand);

                if (stack != null) {
                    if (stack.getItem() instanceof BlockItem || stack.getItem() instanceof SpawnEggItem) {
                        placeBlock(player, stack, hand);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return ActionResult.FAIL;
    }

    private void placeBlock(PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient || !(player instanceof ClientPlayerEntity)) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        double distance = mc.interactionManager.getReachDistance();

        float ticks = mc.getTickDelta();

        Vec3d eye = player.getCameraPosVec(ticks);
        Vec3d look = player.getOppositeRotationVector(ticks);
        Vec3d ray = eye.add(look.x * distance, look.y * distance, look.z * distance);

        float s = 0.5F;
        Box bounds = getBoundingBox().shrink(0, s, 0).shrink(0, -s, 0);

        BlockHitResult hit = Box.rayTrace(Lists.newArrayList(bounds), eye, ray, BlockPos.ORIGIN);

        if (hit != null) {
            Direction direction = hit.getSide();

            mc.hitResult = hit = new BlockHitResult(hit.getPos(), direction, new BlockPos(hit.getPos()), false);

            int oldCount = stack.getCount();
            ActionResult result = mc.interactionManager.interactBlock(((ClientPlayerEntity)player), (ClientWorld)player.world, hand, hit);

            if (result == ActionResult.SUCCESS) {
                player.swingHand(hand);

                if (!stack.isEmpty() && (stack.getCount() != oldCount || mc.interactionManager.hasCreativeInventory())) {
                    mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                }
            }
        }


    }
}
