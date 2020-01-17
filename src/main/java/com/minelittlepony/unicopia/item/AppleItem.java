package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.item.consumables.IEdible;
import com.minelittlepony.unicopia.item.consumables.Toxicity;
import com.minelittlepony.util.collection.Pool;
import com.minelittlepony.util.collection.Weighted;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AppleItem extends Item implements IEdible {

    private static final Pool<Object, Weighted<Supplier<ItemStack>>> typeVariantMap = Pool.of(PlanksBlock.Type.OAK,
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
    );

    public static ItemStack getRandomItemStack(Object variant) {
        return typeVariantMap.getOptional(variant)
                .flatMap(Weighted::get)
                .map(Supplier::get)
                .orElse(ItemStack.EMPTY);
    }

    public AppleItem(FoodComponent components) {
        super(new Item.Settings()
                .group(ItemGroup.FOOD)
                .food(components));
    }

    @Override
    public boolean onEntityItemUpdate(ItemEntity item) {

            if (!item.removed && item.age > item.pickupDelay) {

                if (!item.world.isClient) {
                    item.remove();

                    ItemEntity neu = EntityType.ITEM.create(item.world);
                    neu.copyPositionAndRotation(item);
                    neu.setStack(new ItemStack(UItems.rotten_apple));

                    item.world.spawnEntity(neu);

                    ItemEntity copy = EntityType.ITEM.create(item.world);
                    copy.copyPositionAndRotation(item);
                    copy.setStack(item.getStack());
                    copy.getStack().decrement(1);

                    item.world.spawnEntity(copy);
                } else {
                    float bob = MathHelper.sin(((float)item.getAge() + 1) / 10F + item.hoverHeight) * 0.1F + 0.1F;

                    for (int i = 0; i < 3; i++) {
                        item.world.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, item.x, item.y + bob, item.z,
                                item.world.random.nextGaussian() - 0.5F,
                                item.world.random.nextGaussian() - 0.5F,
                                item.world.random.nextGaussian() - 0.5F);
                    }
                }
            }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getToxicityLevel(stack).getTooltip());
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return Toxicity.SAFE;
    }
}
