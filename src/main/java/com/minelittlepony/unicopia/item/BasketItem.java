package com.minelittlepony.unicopia.item;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.util.Dispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BasketItem extends Item implements Dispensable {
    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);
    private static final double REACH = 5;

    public BasketItem(Item.Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, createDispenserBehaviour());
    }

    @Override
    public TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {
        Direction facing = source.getBlockState().get(DispenserBlock.FACING);
        BlockPos pos = source.getPos().offset(facing);
        float yaw = facing.getOpposite().asRotation();
        return placeEntity(stack, source.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw, null);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult hit = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);

        if (hit.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        }

        Vec3d eyePos = user.getEyePos();
        if (world.getOtherEntities(user, user.getBoundingBox().stretch(user.getRotationVec(1).multiply(REACH)).expand(1), RIDERS).stream()
                .anyMatch(entity -> entity.getBoundingBox().expand(entity.getTargetingMargin()).contains(eyePos))) {
            return TypedActionResult.pass(stack);
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            return placeEntity(stack, world, hit.getPos().x, hit.getPos().y, hit.getPos().z, user.getYaw() + 180, user);
        }

        return TypedActionResult.pass(stack);
    }

    private TypedActionResult<ItemStack> placeEntity(ItemStack stack, World world, double x, double y, double z, float yaw, @Nullable PlayerEntity user) {
        AirBalloonEntity entity = UEntities.AIR_BALLOON.create(world);
        entity.updatePositionAndAngles(x, y, z, 0, 0);
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);
        if (!world.isSpaceEmpty(entity, entity.getBoundingBox())) {
            return TypedActionResult.fail(stack);
        }
        if (!world.isClient) {
            world.spawnEntity(entity);
            if (user != null) {
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
            if (user == null || !user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}