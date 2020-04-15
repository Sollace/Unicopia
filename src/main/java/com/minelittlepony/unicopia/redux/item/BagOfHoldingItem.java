package com.minelittlepony.unicopia.redux.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.EquinePredicates;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.IMagicalItem;
import com.minelittlepony.unicopia.core.util.VecHelper;
import com.minelittlepony.unicopia.redux.UContainers;
import com.minelittlepony.unicopia.redux.container.BagOfHoldingContainer;
import com.minelittlepony.unicopia.redux.container.BagOfHoldingInventory;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BagOfHoldingItem extends Item implements IMagicalItem {

    public BagOfHoldingItem() {
        super(new Settings().maxCount(1).group(ItemGroup.TRANSPORTATION));
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

        for (Text name : counts.keySet()) {
            tooltip.add(name.append(" ").append(counts.get(name).toString()));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        if (!EquinePredicates.MAGI.test(player)) {
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
                        BagOfHoldingInventory inventory = BagOfHoldingInventory.getInventoryFromStack(stack);

                        inventory.addBlockEntity(world, pos, (BlockEntity & Inventory)tile);
                        inventory.writeTostack(stack);
                        inventory.onInvClose(player);

                        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                    }

                    Box box = new Box(pos.offset(bhit.getSide())).expand(0.5);

                    List<Entity> itemsAround = world.getEntities(player, box, EquinePredicates.ITEMS);

                    if (itemsAround.size() > 0) {
                        BagOfHoldingInventory inventory = BagOfHoldingInventory.getInventoryFromStack(stack);

                        inventory.addItem((ItemEntity)itemsAround.get(0));
                        inventory.writeTostack(stack);
                        inventory.onInvClose(player);

                        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
                    }
                }
            }

            return new TypedActionResult<>(ActionResult.FAIL, stack);
        }

        ContainerProviderRegistry.INSTANCE.openContainer(UContainers.BAG_OF_HOLDING, player, o -> {});
        player.openContainer(new ContainerProvider(stack));

        player.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN, 0.5F, 1);

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public boolean hasInnerSpace() {
        return true;
    }

    public static class ContainerProvider implements NameableContainerProvider {

        private Text customname = null;

        ContainerProvider(ItemStack stack) {
            if (stack.hasCustomName()) {
                customname = stack.getName();
            }
        }

        @Override
        public Text getDisplayName() {
            if (customname != null) {
                return customname;
            }
            return new TranslatableText("unicopi.gui.title.itemofholding");
        }

        @Override
        public BagOfHoldingContainer createMenu(int id, PlayerInventory inv, PlayerEntity player) {
            return new BagOfHoldingContainer(id, null, player, null);
        }
    }
}
