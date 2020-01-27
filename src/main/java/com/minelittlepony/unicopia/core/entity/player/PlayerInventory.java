package com.minelittlepony.unicopia.core.entity.player;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.core.entity.Updatable;
import com.minelittlepony.unicopia.core.magic.IDependable;
import com.minelittlepony.unicopia.core.magic.IMagicalItem;
import com.minelittlepony.unicopia.core.util.InbtSerialisable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PlayerInventory implements Updatable, InbtSerialisable {
    private final Map<IDependable, Entry> dependencies = Maps.newHashMap();

    private final IPlayer player;

    PlayerInventory(IPlayer player) {
        this.player = player;
    }

    /**
     * Reinforces a players dependency on a certain magical artifact.
     * A dependency will slowly drop over time if not reinforced
     *
     * Bad things might happen when it's removed.
     */
    public synchronized void enforceDependency(IDependable item) {
        if (dependencies.containsKey(item)) {
            dependencies.get(item).reinforce();
        } else {
            dependencies.put(item, new Entry(item));
        }
    }

    /**
     * Returns how long the player has been wearing the given item.
     */
    public synchronized int getTicksAttached(IDependable item) {
        if (dependencies.containsKey(item)) {
            return dependencies.get(item).ticksAttached;
        }

        return 0;
    }

    /**
     * Returns how dependent the player has become on the given item.
     *
     * Zero means not dependent at all / not wearing.
     */
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

    /**
     * Checks if the player is wearing the specified magical artifact.
     */
    public boolean isWearing(IMagicalItem item) {
        for (ItemStack i : player.getOwner().getArmorItems()) {
            if (!i.isEmpty() && i.getItem() == item) {
                return true;
            }
        }

        return item instanceof Item;
    }

    public boolean matches(Tag<Item> tag) {
        for (ItemStack i : player.getOwner().getArmorItems()) {
            if (!i.isEmpty() && i.getItem().isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        ListTag items = new ListTag();

        for (Entry entry : dependencies.values()) {
            items.add(entry.toNBT());
        }

        compound.put("dependencies", items);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        dependencies.clear();

        compound.getList("dependencies", 10).forEach(res -> {
            Entry entry = new Entry();

            entry.fromNBT((CompoundTag)res);

            if (entry.item != null) {
                dependencies.put(entry.item, entry);
            }
        });
    }

    class Entry implements Updatable, InbtSerialisable {
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
        public void toNBT(CompoundTag compound) {
            compound.putInt("ticksAttached", ticksAttached);
            compound.putFloat("needfulness", needfulness);
            compound.putString("item", Registry.ITEM.getId(((Item)item)).toString());
        }

        @Override
        public void fromNBT(CompoundTag compound) {
            ticksAttached = compound.getInt("ticksAttached");
            needfulness = compound.getFloat("needfulness");

            Item item = Registry.ITEM.get(new Identifier(compound.getString("item")));

            this.item = item instanceof IDependable ? (IDependable)item : null;
        }
    }
}
