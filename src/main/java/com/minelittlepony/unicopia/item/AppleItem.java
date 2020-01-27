package com.minelittlepony.unicopia.item;

import java.util.List;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ducks.IItemEntity;
import com.minelittlepony.unicopia.entity.capabilities.ItemEntityCapabilities;
import com.minelittlepony.unicopia.item.consumables.Toxic;
import com.minelittlepony.unicopia.item.consumables.Toxicity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AppleItem extends Item implements Toxic, ItemEntityCapabilities.TickableItem {

    // TODO: Move this to a datapack
    /*private static final Pool<Object, Weighted<Supplier<ItemStack>>> TYPE_VARIANT_POOL = Pool.of(PlanksBlock.Type.OAK,
            PlanksBlock.Type.OAK, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.green_apple))
                    .put(3, () -> new ItemStack(UItems.red_apple)),
            PlanksBlock.Type.SPRUCE, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.sour_apple))
                    .put(2, () -> new ItemStack(UItems.green_apple))
                    .put(3, () -> new ItemStack(UItems.sweet_apple))
                    .put(4, () -> new ItemStack(UItems.rotten_apple)),
            PlanksBlock.Type.BIRCH, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.green_apple)),
            PlanksBlock.Type.JUNGLE, new Weighted<Supplier<ItemStack>>()
                    .put(5, () -> new ItemStack(UItems.green_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(1, () -> new ItemStack(UItems.sour_apple)),
            PlanksBlock.Type.ACACIA, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.green_apple)),
            PlanksBlock.Type.DARK_OAK, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.zap_apple)
            )
    );*/

    public static ItemStack getRandomItemStack(Object variant) {
        return new ItemStack(UItems.VanillaOverrides.red_apple);
        /*return TYPE_VARIANT_POOL.getOptional(variant)
                .flatMap(Weighted::get)
                .map(Supplier::get)
                .orElse(ItemStack.EMPTY);*/
    }

    public AppleItem(FoodComponent components) {
        super(new Item.Settings()
                .group(ItemGroup.FOOD)
                .food(components));
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.getRaceContainer().getOwner();

        if (!entity.removed && item.getAge() > item.getPickupDelay()) {

            if (!entity.world.isClient) {
                entity.remove();

                ItemEntity neu = EntityType.ITEM.create(entity.world);
                neu.copyPositionAndRotation(entity);
                neu.setStack(new ItemStack(UItems.rotten_apple));

                entity.world.spawnEntity(neu);

                ItemEntity copy = EntityType.ITEM.create(entity.world);
                copy.copyPositionAndRotation(entity);
                copy.setStack(entity.getStack());
                copy.getStack().decrement(1);

                entity.world.spawnEntity(copy);
            } else {
                float bob = MathHelper.sin(((float)item.getAge() + 1) / 10F + entity.hoverHeight) * 0.1F + 0.1F;

                for (int i = 0; i < 3; i++) {
                    entity.world.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, entity.x, entity.y + bob, entity.z,
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

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return Toxicity.SAFE;
    }
}
