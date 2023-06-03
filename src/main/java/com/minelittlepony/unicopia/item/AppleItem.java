package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemImpl;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;

public class AppleItem {
    private static final ItemImpl.GroundTickCallback TICK_CALLBACK = AppleItem::onGroundTick;

    private AppleItem() { }

    public static <T extends Item> T registerTickCallback(T item) {
        return ItemImpl.registerTickCallback(item, TICK_CALLBACK);
    }

    private static ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().asEntity();

        if (!entity.isRemoved() && item.getPickupDelay() == 0 && item.getAge() > 2030 && entity.getWorld().random.nextInt(150) < 10) {

            if (!entity.getWorld().isClient) {
                entity.remove(RemovalReason.KILLED);

                ItemEntity neu = EntityType.ITEM.create(entity.getWorld());
                neu.copyPositionAndRotation(entity);
                neu.setStack(new ItemStack(UItems.ROTTEN_APPLE));

                entity.getWorld().spawnEntity(neu);

                ItemEntity copy = EntityType.ITEM.create(entity.getWorld());
                copy.copyPositionAndRotation(entity);
                copy.setStack(entity.getStack());
                copy.getStack().decrement(1);

                entity.getWorld().spawnEntity(copy);
            }

            float bob = MathHelper.sin(((float)item.getAge() + 1) / 10F + entity.uniqueOffset) * 0.1F + 0.1F;

            for (int i = 0; i < 3; i++) {
                entity.getWorld().addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, entity.getX(), entity.getY() + bob, entity.getZ(),
                        entity.getWorld().random.nextGaussian() - 0.5F,
                        entity.getWorld().random.nextGaussian() - 0.5F,
                        entity.getWorld().random.nextGaussian() - 0.5F);
            }
            entity.playSound(USounds.ITEM_APPLE_ROT, 0.5F, 1.5F);
        }

        return ActionResult.PASS;
    }
}
