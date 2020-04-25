package com.minelittlepony.unicopia.item;

import java.util.List;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ducks.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemEntityCapabilities;
import com.minelittlepony.unicopia.toxin.Toxicity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AppleItem extends Item implements ItemEntityCapabilities.TickableItem {

    public AppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().getOwner();

        if (!entity.removed && item.getAge() > item.getPickupDelay()) {

            if (!entity.world.isClient) {
                entity.remove();

                ItemEntity neu = EntityType.ITEM.create(entity.world);
                neu.copyPositionAndRotation(entity);
                neu.setStack(new ItemStack(UItems.ROTTEN_APPLE));

                entity.world.spawnEntity(neu);

                ItemEntity copy = EntityType.ITEM.create(entity.world);
                copy.copyPositionAndRotation(entity);
                copy.setStack(entity.getStack());
                copy.getStack().decrement(1);

                entity.world.spawnEntity(copy);
            } else {
                float bob = MathHelper.sin(((float)item.getAge() + 1) / 10F + entity.hoverHeight) * 0.1F + 0.1F;

                for (int i = 0; i < 3; i++) {
                    entity.world.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, entity.getX(), entity.getY() + bob, entity.getZ(),
                            entity.world.random.nextGaussian() - 0.5F,
                            entity.world.random.nextGaussian() - 0.5F,
                            entity.world.random.nextGaussian() - 0.5F);
                }
            }
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
