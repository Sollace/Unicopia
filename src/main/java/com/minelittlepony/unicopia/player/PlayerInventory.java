package com.minelittlepony.unicopia.player;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.extern.Baubles;
import com.minelittlepony.unicopia.item.IDependable;
import com.minelittlepony.unicopia.item.IMagicalItem;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerInventory implements IInventory, IUpdatable, InbtSerialisable {
    private final Map<IDependable, Entry> dependencies = Maps.newHashMap();

    private final IPlayer player;

    PlayerInventory(IPlayer player) {
        this.player = player;
    }

    @Override
    public synchronized void enforceDependency(IDependable item) {
        if (dependencies.containsKey(item)) {
            dependencies.get(item).reinforce();
        } else {
            dependencies.put(item, new Entry(item));
        }
    }

    @Override
    public synchronized int getTicksAttached(IDependable item) {
        if (dependencies.containsKey(item)) {
            return dependencies.get(item).ticksAttached;
        }

        return 0;
    }

    @Override
    public synchronized float getNeedfulness(IDependable item) {
        if (dependencies.containsKey(item)) {
            return dependencies.get(item).needfulness;
        }

        return 0;
    }

    @Override
    public synchronized void onUpdate() {

        Iterator<Map.Entry<IDependable, Entry>> iterator = dependencies.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<IDependable, Entry> entry = iterator.next();

            Entry item = entry.getValue();

            item.onUpdate();

            if (item.needfulness <= 0.001) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean isWearing(IMagicalItem item) {
        for (ItemStack i : player.getOwner().getArmorInventoryList()) {
            if (!i.isEmpty() && i.getItem() == item) {
                return true;
            }
        }

        return item instanceof Item && Baubles.isBaubleEquipped(player.getOwner(), (Item)item) != -1;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (Entry entry : dependencies.values()) {
            items.appendTag(entry.toNBT());
        }

        compound.setTag("dependencies", items);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        dependencies.clear();

        compound.getTagList("dependencies", 10).forEach(res -> {
            Entry entry = new Entry();

            entry.readFromNBT((NBTTagCompound)res);

            if (entry.item != null) {
                dependencies.put(entry.item, entry);
            }
        });
    }

    class Entry implements IUpdatable, InbtSerialisable {
        int ticksAttached = 0;

        float needfulness = 1;

        IDependable item;

        Entry() {

        }

        Entry(IDependable key) {
            this.item = key;
        }

        void reinforce() {
            needfulness = Math.min(30, needfulness + 1);
        }

        @Override
        public void onUpdate() {
            if (isWearing(item)) {
                ticksAttached ++;
                needfulness *= 0.9F;
            } else {
                item.onRemoved(player, needfulness);
                needfulness = 0;
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound compound) {
            compound.setInteger("ticksAttached", ticksAttached);
            compound.setFloat("needfulness", needfulness);
            compound.setString("item", ((Item)item).getRegistryName().toString());
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            ticksAttached = compound.getInteger("ticksAttached");
            needfulness = compound.getFloat("needfulness");

            Item item = Item.getByNameOrId(compound.getString("item"));

            this.item = item instanceof IDependable ? (IDependable)item : null;
        }
    }
}
