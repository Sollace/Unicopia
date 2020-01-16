package com.minelittlepony.unicopia.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.magic.items.IMagicalItem;
import com.minelittlepony.util.InbtSerialisable;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.LootContextParameters;

public class InventoryOfHolding extends BasicInventory implements InbtSerialisable {

    public static final int NBT_COMPOUND = 10;
    public static final int MIN_SIZE = 18;

    static InventoryOfHolding empty() {
        return new InventoryOfHolding(new ArrayList<>(), ItemStack.EMPTY);
    }

    public static InventoryOfHolding getInventoryFromStack(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();

        iterateContents(stack, (i, item) -> {
            items.add(item);
            return true;
        });

        return new InventoryOfHolding(items, stack);
    }

    public static void iterateContents(ItemStack stack, BiFunction<Integer, ItemStack, Boolean> itemConsumer) {
        if (stack.hasTag() && stack.getTag().containsKey("inventory")) {
            CompoundTag compound = stack.getSubTag("inventory");

            if (compound.containsKey("items")) {
                ListTag list = compound.getList("items", NBT_COMPOUND);

                for (int i = 0; i < list.size(); i++) {
                    ItemStack item = ItemStack.fromTag(list.getCompoundTag(i));
                    if (!item.isEmpty() && !itemConsumer.apply(i, item)) {
                        break;
                    }
                }
            }
        }
    }

    private final Optional<Text> name;

    private InventoryOfHolding(List<ItemStack> items, ItemStack source) {
        super(items.size() + 9 - (items.size() % 9));

        for (int i = 0; i < items.size(); i++) {
            setInvStack(i, items.get(i));
        }

        if (source.hasCustomName()) {
            name = Optional.of(source.getName());
        } else {
            name = Optional.empty();
        }
    }

    public Optional<Text> getName() {
        return name;
    }

    public <T extends BlockEntity & Inventory> void addBlockEntity(World world, BlockPos pos, T blockInventory) {
        BlockState state = world.getBlockState(pos);

        BlockEntity tile = state.getBlock().hasBlockEntity() ? world.getBlockEntity(pos) : null;
        LootContext.Builder context = new LootContext.Builder((ServerWorld)world)
                .setRandom(world.random).put(LootContextParameters.POSITION, pos)
                .put(LootContextParameters.TOOL, ItemStack.EMPTY)
                .putNullable(LootContextParameters.BLOCK_ENTITY, tile);

        ItemStack blockStack = state.getDroppedStacks(context).get(0);

        blockInventory.toTag(blockStack.getSubTag("BlockEntityTag"));

        for (int i = 0; i < blockInventory.getInvSize(); i++) {
            ItemStack stack = blockInventory.getInvStack(i);

            if (isIllegalItem(stack)) {
                blockStack.getSubTag("inventory").putBoolean("invalid", true);
                break;
            }
        }

        encodeStackWeight(blockStack, getContentsTotalWorth(blockInventory, true), true);

        world.removeBlockEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());

        add(blockStack);
        world.playSound(null, pos, SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 3.5F, 0.25F);
    }

    public void addItem(ItemEntity entity) {
        add(entity.getStack());
        entity.remove();

        entity.playSound(SoundEvents.UI_TOAST_IN, 3.5F, 0.25F);
    }

    @Override
    public void onInvClose(PlayerEntity player) {
        if (checkExplosionConditions()) {
            if (player instanceof ServerPlayerEntity) {
                UAdvancements.BOH_DEATH.trigger((ServerPlayerEntity)player);
            }
            player.damage(MagicalDamageSource.create("paradox"), 1000);
            player.world.createExplosion(player, player.x, player.y, player.z, 5, DestructionType.DESTROY);
        }
    }

    protected boolean checkExplosionConditions() {
        for (int i = 0; i < getInvSize(); i++) {
            if (isIllegalItem(getInvStack(i))) {
                return true;
            }
        }

        return false;
    }

    protected boolean isIllegalItem(ItemStack stack) {
        CompoundTag compound = stack.getSubTag("inventory");

        return isIllegalBlock(Block.getBlockFromItem(stack.getItem()))
                // TODO: tag for items that are invalid for the inventory of holding
                || stack.getItem() instanceof BlockItem && (((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
                || (compound != null && compound.containsKey("invalid"))
                || (stack.getItem() instanceof IMagicalItem && ((IMagicalItem) stack.getItem()).hasInnerSpace());
    }

    protected boolean isIllegalBlock(Block block) {
        return block instanceof EnderChestBlock;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        ListTag nbtItems = new ListTag();

        for (int i = 0; i < getInvSize(); i++) {
            CompoundTag comp = new CompoundTag();
            ItemStack stack = getInvStack(i);
            if (!isIllegalItem(stack)) {
                if (!stack.isEmpty()) {
                    stack.toTag(comp);
                    nbtItems.add(comp);
                }
            }
        }
        compound.put("items", nbtItems);
        compound.putDouble("weight", getContentsTotalWorth());
    }

    public double getContentsTotalWorth() {
        return getContentsTotalWorth(this, true);
    }

    public void writeTostack(ItemStack stack) {
        toNBT(stack.getOrCreateSubTag("inventory"));
    }

    public static double getContentsTotalWorth(Inventory inventory, boolean deep) {
        double total = 0;

        for (int i = 0; i < inventory.getInvSize(); i++) {
            ItemStack stack = inventory.getInvStack(i);

            double weightOfOne = decodeStackWeight(stack, deep);

            if (weightOfOne == 0) {
                total += stack.getCount();
            } else {
                total += weightOfOne * stack.getCount();
            }
        }

        return total;
    }

    public static void encodeStackWeight(ItemStack stack, double weight, boolean deep) {
        CompoundTag compound = stack.getSubTag("inventory");
        if (weight == 0 && compound != null) {
            compound.remove("weight");
            compound.remove("deep");
            if (compound.isEmpty()) {
                stack.removeSubTag("inventory");
            }
        } else {
            if (weight != 0) {
                if (compound == null) {
                    compound = stack.getOrCreateSubTag("inventory");
                }

                compound.putDouble("weight", weight);
                if (deep) {
                    compound.putBoolean("deep", deep);
                }
            }
        }
    }

    public static double decodeStackWeight(ItemStack stack, boolean deep) {
        if (!stack.isEmpty() && stack.hasTag()) {
            CompoundTag bet = stack.getSubTag("BlockEntityTag");
            CompoundTag compound = stack.getSubTag("inventory");

            boolean hasWeight = compound != null && compound.containsKey("weight");

            if (deep) {
                if (!hasWeight && bet != null) {
                    Block b = Block.getBlockFromItem(stack.getItem());
                    BlockEntity te = b.hasBlockEntity() ? ((BlockEntityProvider)b).createBlockEntity(EmptyBlockView.INSTANCE) : null;

                    double weight = 0;

                    if (te instanceof Inventory) {
                        te.fromTag(bet);

                        weight = getContentsTotalWorth((Inventory)te, deep);
                    }

                    encodeStackWeight(stack, weight, deep);

                    return weight;
                }
            }

            if (hasWeight && (deep || !compound.containsKey("deep"))) {
                return compound.getDouble("weight");
            }
        }

        return 0;
    }
}