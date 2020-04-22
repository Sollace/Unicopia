package com.minelittlepony.unicopia.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.advancement.BOHDeathCriterion;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class BagOfHoldingInventory extends BasicInventory implements NbtSerialisable {

    public static final int NBT_COMPOUND = 10;
    public static final int MIN_SIZE = 18;

    static BagOfHoldingInventory empty() {
        return new BagOfHoldingInventory(new ArrayList<>(), ItemStack.EMPTY);
    }

    public static BagOfHoldingInventory getInventoryFromStack(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();

        iterateContents(stack, (i, item) -> {
            items.add(item);
            return true;
        });

        return new BagOfHoldingInventory(items, stack);
    }

    public static void iterateContents(ItemStack stack, BiFunction<Integer, ItemStack, Boolean> itemConsumer) {
        if (stack.hasTag() && stack.getTag().contains("inventory")) {
            CompoundTag compound = stack.getSubTag("inventory");

            if (compound.contains("items")) {
                ListTag list = compound.getList("items", NBT_COMPOUND);

                for (int i = 0; i < list.size(); i++) {
                    ItemStack item = ItemStack.fromTag(list.getCompound(i));
                    if (!item.isEmpty() && !itemConsumer.apply(i, item)) {
                        break;
                    }
                }
            }
        }
    }

    private final Optional<Text> name;

    private BagOfHoldingInventory(List<ItemStack> items, ItemStack source) {
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

        HeavyInventory.encodeStackWeight(blockStack, HeavyInventory.getContentsTotalWorth(blockInventory, true), true);

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
                BOHDeathCriterion.INSTANCE.trigger((ServerPlayerEntity)player);
            }
            player.damage(MagicalDamageSource.create("paradox"), 1000);
            player.world.createExplosion(player, player.getX(), player.getY(), player.getZ(), 5, DestructionType.DESTROY);
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
                || stack.getItem().isIn(UTags.HAMMERPACE_IMMUNE)
                || (compound != null && compound.contains("invalid"));
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
        return HeavyInventory.getContentsTotalWorth(this, true);
    }

    public void writeTostack(ItemStack stack) {
        toNBT(stack.getOrCreateSubTag("inventory"));
    }

}