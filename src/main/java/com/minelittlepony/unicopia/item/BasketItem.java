package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.util.Dispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

    private final AirBalloonEntity.BasketType type;

    public static final Map<AirBalloonEntity.BasketType, BasketItem> REGISTRY = new HashMap<>();

    public BasketItem(AirBalloonEntity.BasketType type, Item.Settings settings) {
        super(settings);
        this.type = type;
        DispenserBlock.registerBehavior(this, createDispenserBehaviour());
        REGISTRY.put(type, this);
    }

    @Override
    public ActionResult dispenseStack(BlockPointer source, ItemStack stack) {
        Direction facing = source.state().get(DispenserBlock.FACING);
        BlockPos pos = source.pos().offset(facing);
        float yaw = facing.getOpposite().asRotation();
        return placeEntity(stack, source.world(), pos.getX(), pos.getY(), pos.getZ(), yaw, null);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult hit = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);

        if (hit.getType() == HitResult.Type.MISS) {
            return ActionResult.PASS;
        }

        Vec3d eyePos = user.getEyePos();
        if (world.getOtherEntities(user, user.getBoundingBox().stretch(user.getRotationVec(1).multiply(REACH)).expand(1), RIDERS).stream()
                .anyMatch(entity -> entity.getBoundingBox().expand(entity.getTargetingMargin()).contains(eyePos))) {
            return ActionResult.PASS;
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            return placeEntity(stack, world, hit.getPos().x, hit.getPos().y, hit.getPos().z, user.getHorizontalFacing().asRotation(), user);
        }

        return ActionResult.PASS;
    }

    private ActionResult placeEntity(ItemStack stack, World world, double x, double y, double z, float yaw, @Nullable PlayerEntity user) {
        AirBalloonEntity entity = UEntities.AIR_BALLOON.create(world, SpawnReason.SPAWN_ITEM_USE);
        yaw += 180;
        entity.updatePositionAndAngles(x, y, z, yaw, 0);
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);
        entity.setYaw(yaw);
        entity.setBasketType(type);
        if (!world.isSpaceEmpty(entity, entity.getBoundingBox())) {
            return ActionResult.FAIL;
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
        return ActionResult.SUCCESS;
    }
}