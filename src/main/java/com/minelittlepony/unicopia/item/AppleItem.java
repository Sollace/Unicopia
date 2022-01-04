package com.minelittlepony.unicopia.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.item.toxin.Toxicity;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AppleItem extends Item implements ItemImpl.TickableItem {

    public AppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().getMaster();

        if (!entity.isRemoved() && item.getPickupDelay() == 0 && item.getAge() > 2030 && entity.world.random.nextInt(150) < 10) {

            if (!entity.world.isClient) {
                entity.remove(RemovalReason.KILLED);

                ItemEntity neu = EntityType.ITEM.create(entity.world);
                neu.copyPositionAndRotation(entity);
                neu.setStack(new ItemStack(UItems.ROTTEN_APPLE));

                entity.world.spawnEntity(neu);

                ItemEntity copy = EntityType.ITEM.create(entity.world);
                copy.copyPositionAndRotation(entity);
                copy.setStack(entity.getStack());
                copy.getStack().decrement(1);

                entity.world.spawnEntity(copy);
            }

            float bob = MathHelper.sin(((float)item.getAge() + 1) / 10F + entity.uniqueOffset) * 0.1F + 0.1F;

            for (int i = 0; i < 3; i++) {
                entity.world.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, entity.getX(), entity.getY() + bob, entity.getZ(),
                        entity.world.random.nextGaussian() - 0.5F,
                        entity.world.random.nextGaussian() - 0.5F,
                        entity.world.random.nextGaussian() - 0.5F);
            }
            entity.playSound(USounds.ITEM_APPLE_ROT, 0.5F, 1.5F);
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getToxicity(stack).getTooltip());
    }

    public Toxicity getToxicity(ItemStack stack) {
        return Toxicity.SAFE;
    }
}
