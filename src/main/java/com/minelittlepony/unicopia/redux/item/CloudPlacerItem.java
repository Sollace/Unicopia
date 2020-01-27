package com.minelittlepony.unicopia.redux.item;

import com.minelittlepony.unicopia.core.magic.IDispensable;
import com.minelittlepony.unicopia.redux.entity.CloudEntity;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class CloudPlacerItem extends Item implements IDispensable {

    private final EntityType<? extends CloudEntity> cloudSupplier;

    public CloudPlacerItem(EntityType<? extends CloudEntity> spawner) {
        super(new Item.Settings()
                .group(ItemGroup.MATERIALS)
                .maxCount(16)
        );
        this.cloudSupplier = spawner;

        setDispenseable();
    }

    public void placeCloud(World world, BlockPos pos) {
        CloudEntity cloud = cloudSupplier.create(world);
        cloud.setPositionAndAngles(pos, 0, 0);
        world.spawnEntity(cloud);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            HitResult mop = rayTrace(world, player, RayTraceContext.FluidHandling.NONE);

            BlockPos pos;

            if (mop.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult)mop;
                pos = bhr.getBlockPos().offset(bhr.getSide());
            } else {
                pos = player.getBlockPos();
            }

            placeCloud(world, pos);

            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {
        Position pos = DispenserBlock.getOutputLocation(source);

        placeCloud(source.getWorld(), new BlockPos(pos.getX(), pos.getY(), pos.getZ()));

        stack.decrement(1);

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }
}
