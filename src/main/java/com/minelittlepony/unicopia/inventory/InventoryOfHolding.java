package com.minelittlepony.unicopia.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.advancements.UAdvancements;
import com.minelittlepony.unicopia.item.IMagicalItem;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.advancements.AdvancementState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryOfHolding extends InventoryBasic implements InbtSerialisable {

    public static final int NBT_COMPOUND = 10;
    public static final int MIN_SIZE = 18;

    List<NBTTagCompound> entities;

    private boolean entitiesLoaded = false;

    List<EntityLiving> livingEntities = new ArrayList<>();

    static InventoryOfHolding empty() {
        List<ItemStack> items = new ArrayList<>();
        List<NBTTagCompound> entities = new ArrayList<>();

        return new InventoryOfHolding(items, entities);
    }

    public static InventoryOfHolding getInventoryFromStack(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();
        List<NBTTagCompound> entities = new ArrayList<>();

        iterateContents(stack, (i, item) -> {
            items.add(item);
            return true;
        }, tag -> {
            entities.add((NBTTagCompound)tag);
        });

        return new InventoryOfHolding(items, entities);
    }

    public static void iterateContents(ItemStack stack, BiFunction<Integer, ItemStack, Boolean> itemConsumer, Consumer<NBTTagCompound> entityConsumer) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("inventory")) {
            NBTTagCompound compound = stack.getOrCreateSubCompound("inventory");

            if (compound.hasKey("items")) {
                NBTTagList list = compound.getTagList("items", NBT_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    ItemStack item = new ItemStack(list.getCompoundTagAt(i));
                    if (!item.isEmpty()) {
                        if (!itemConsumer.apply(i, item)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private InventoryOfHolding(List<ItemStack> items, List<NBTTagCompound> entities) {
        super("unicopia.gui.title.bagofholding", false, items.size() + 9 - (items.size() % 9));

        this.entities = entities;

        for (int i = 0; i < items.size(); i++) {
            setInventorySlotContents(i, items.get(i));
        }
    }

    public List<EntityLiving> getEntities(World world) {
        return livingEntities;
    }

    public <T extends TileEntity & IInventory> void addBlockEntity(World world, BlockPos pos, IBlockState state, T blockInventory) {
        ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));

        blockInventory.writeToNBT(blockStack.getOrCreateSubCompound("BlockEntityTag"));

        for (int i = 0; i < blockInventory.getSizeInventory(); i++) {
            ItemStack stack = blockInventory.getStackInSlot(i);

            if (isIllegalItem(stack)) {
                blockStack.getOrCreateSubCompound("inventory").setBoolean("invalid", true);
                break;
            }
        }

        encodeStackWeight(blockStack, getContentsTotalWorth(blockInventory));

        world.removeTileEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());

        addItem(blockStack);
        world.playSound(null, pos, SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 3.5F, 0.25F);
    }

    public void addPrisoner(EntityLiving entity) {
        getEntities(entity.world).add(entity);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("id", EntityList.getKey(entity).toString());
        entity.writeToNBT(compound);
        entities.add(compound);

        entity.setDead();

        entity.playLivingSound();
        entity.playSound(SoundEvents.UI_TOAST_IN, 3.5F, 0.25F);
    }

    public void addItem(EntityItem entity) {
        addItem(entity.getItem());
        entity.setDead();

        entity.playSound(SoundEvents.UI_TOAST_IN, 3.5F, 0.25F);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (checkExplosionConditions()) {
            if (player instanceof EntityPlayerMP) {
                UAdvancements.BOH_DEATH.trigger((EntityPlayerMP)player);
            }
            player.attackEntityFrom(MagicalDamageSource.create("paradox"), 1000);
            player.world.createExplosion(player, player.posX, player.posY, player.posZ, 5, true);
        }
    }

    protected boolean checkExplosionConditions() {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (isIllegalItem(getStackInSlot(i))) {
                return true;
            }
        }

        return false;
    }

    protected boolean isIllegalItem(ItemStack stack) {
        NBTTagCompound compound = stack.getSubCompound("inventory");

        return isIllegalBlock(Block.getBlockFromItem(stack.getItem()))
                || stack.getItem() instanceof ItemShulkerBox
                || (compound != null && compound.hasKey("invalid"))
                || (stack.getItem() instanceof IMagicalItem && ((IMagicalItem) stack.getItem()).hasInnerSpace());
    }

    protected boolean isIllegalBlock(Block block) {
        return block instanceof BlockEnderChest;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList nbtItems = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); i++) {
            NBTTagCompound comp = new NBTTagCompound();
            ItemStack stack = getStackInSlot(i);
            if (!isIllegalItem(stack)) {
                if (!stack.isEmpty()) {
                    stack.writeToNBT(comp);
                    nbtItems.appendTag(comp);
                }
            }
        }
        compound.setTag("items", nbtItems);
        compound.setDouble("weight", getContentsTotalWorth());
    }

    public double getContentsTotalWorth() {
        return getContentsTotalWorth(this);
    }

    public void writeTostack(ItemStack stack) {
        writeToNBT(stack.getOrCreateSubCompound("inventory"));
    }

    public static double getContentsTotalWorth(IInventory inventory) {
        double total = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            total += stack.getCount();
            total += decodeStackWeight(stack);
        }

        return total;
    }

    public static void encodeStackWeight(ItemStack stack, double weight) {
        NBTTagCompound compound = stack.getSubCompound("inventory");
        if (weight == 0 && compound != null) {
            compound.removeTag("weight");
            if (compound.isEmpty()) {
                stack.removeSubCompound("inventory");
            }
        } else {
            if (weight != 0) {
                if (compound == null) {
                    compound = stack.getOrCreateSubCompound("inventory");
                }

                compound.setDouble("weight", weight);
            }
        }
    }

    public static double decodeStackWeight(ItemStack stack) {
        if (!stack.isEmpty()) {
            NBTTagCompound compound = stack.getSubCompound("inventory");
            if (compound != null && compound.hasKey("weight")) {
                return compound.getDouble("weight");
            }
        }

        return 0;
    }
}