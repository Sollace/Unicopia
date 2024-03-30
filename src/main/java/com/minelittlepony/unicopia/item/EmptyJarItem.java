package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemImpl;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class EmptyJarItem extends BlockItem implements ItemImpl.GroundTickCallback {
    public EmptyJarItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().asEntity();

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), UBlocks.CLOUD_JAR, UBlocks.STORM_JAR, UBlocks.LIGHTNING_JAR, UBlocks.ZAP_JAR);

        entity.setInvulnerable(true);

        if (!entity.getWorld().isClient
                && !entity.isRemoved()
                && entity.getItemAge() > 100
                && entity.getWorld().isThundering()
                && entity.getWorld().isSkyVisible(entity.getBlockPos())
                && entity.getWorld().random.nextInt(130) == 0) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
            lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());

            entity.remove(RemovalReason.DISCARDED);
            entity.getWorld().spawnEntity(lightning);

            ItemEntity neu = EntityType.ITEM.create(entity.getWorld());
            neu.copyPositionAndRotation(entity);
            neu.setStack(new ItemStack(this == UItems.RAIN_CLOUD_JAR ? UItems.STORM_CLOUD_JAR : UItems.LIGHTNING_JAR));
            neu.setInvulnerable(true);

            entity.getWorld().spawnEntity(neu);

            ItemEntity copy = EntityType.ITEM.create(entity.getWorld());
            copy.copyPositionAndRotation(entity);
            copy.setInvulnerable(true);
            copy.setStack(entity.getStack());
            copy.getStack().decrement(1);

            entity.getWorld().spawnEntity(copy);
        }
        return ActionResult.PASS;
    }
}
