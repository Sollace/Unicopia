package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.container.BagOfHoldingInventory;
import com.minelittlepony.unicopia.container.UContainers;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.MagicalItem;
import com.minelittlepony.unicopia.util.VecHelper;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BagOfHoldingItem extends Item implements MagicalItem {

    public BagOfHoldingItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        super.appendTooltip(stack, worldIn, tooltip, flagIn);

        Map<Text, Integer> counts = new HashMap<>();

        BagOfHoldingInventory.iterateContents(stack, (i, itemstack) -> {
            Text name = itemstack.getName();

            counts.put(name, counts.getOrDefault(name, 0) + itemstack.getCount());
            return true;
        });

        counts.forEach((line, count) -> {
            tooltip.add(line.deepCopy().append(" ").append(count.toString()));
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        if (!EquinePredicates.PLAYER_UNICORN.test(player)) {
            return super.use(world, player, hand);
        }

        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            HitResult hit = VecHelper.getObjectMouseOver(player, 5, 0);

            if (hit != null) {
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhit = (BlockHitResult)hit;
                    BlockPos pos = bhit.getBlockPos();

                    BlockEntity tile = world.getBlockEntity(pos);

                    if (tile instanceof Inventory) {
                        if (!world.isClient) {
                            BagOfHoldingInventory inventory = BagOfHoldingInventory.getInventoryFromStack(stack);

                            inventory.addBlockEntity(world, pos, (BlockEntity & Inventory)tile);
                            inventory.writeTostack(stack);
                            inventory.onInvClose(player);
                        }
                        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                    }

                    Box box = new Box(pos.offset(bhit.getSide())).expand(0.5);

                    List<Entity> itemsAround = world.getEntities(player, box, EquinePredicates.IS_VALID_ITEM);

                    if (itemsAround.size() > 0) {
                        if (!world.isClient) {
                            BagOfHoldingInventory inventory = BagOfHoldingInventory.getInventoryFromStack(stack);

                            inventory.addItem((ItemEntity)itemsAround.get(0));
                            inventory.writeTostack(stack);
                            inventory.onInvClose(player);
                        }
                        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                    }
                } else if (hit.getType() == HitResult.Type.ENTITY) {

                    Entity e = ((EntityHitResult)hit).getEntity();

                    if (e instanceof LivingEntity && !(e instanceof PlayerEntity) && !(e instanceof IMagicals)) {
                        if (!world.isClient) {
                            BagOfHoldingInventory inventory = BagOfHoldingInventory.getInventoryFromStack(stack);

                            inventory.addEntity(world, e);
                            inventory.writeTostack(stack);
                            inventory.onInvClose(player);
                        }
                        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                    }
                }
            }

            return new TypedActionResult<>(ActionResult.FAIL, stack);
        }

        if (player instanceof ServerPlayerEntity) {
            ContainerProviderRegistry.INSTANCE.openContainer(UContainers.BAG_OF_HOLDING, player, o -> {
                if (stack.hasCustomName()) {
                    o.writeText(stack.getName());
                } else {
                    o.writeText(new TranslatableText("unicopi.gui.title.itemofholding"));
                }
            });
            // player.openContainer(new ContainerProvider(stack));
        }
        player.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN, 0.5F, 1);

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }
}
