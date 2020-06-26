package com.minelittlepony.unicopia.equine.player;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.minelittlepony.unicopia.magic.AffineItem;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.world.container.HeavyInventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.registry.Registry;

public class PlayerInventory implements Tickable, NbtSerialisable {
    private final Map<AffineItem, Entry> dependencies = Maps.newHashMap();

    private final Pony player;

    private double carryingWeight;

    PlayerInventory(Pony player) {
        this.player = player;
    }

    /**
     * Reinforces a players dependency on a certain magical artifact.
     * A dependency will slowly drop over time if not reinforced
     *
     * Bad things might happen when it's removed.
     */
    public synchronized void enforceDependency(AffineItem item) {
        if (dependencies.containsKey(item)) {
            dependencies.get(item).reinforce();
        } else {
            dependencies.put(item, new Entry(item));
        }
    }

    /**
     * Returns how long the player has been wearing the given item.
     */
    public synchronized int getTicksAttached(AffineItem item) {
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
    public synchronized float getNeedfulness(AffineItem item) {
        if (dependencies.containsKey(item)) {
            return dependencies.get(item).needfulness;
        }

        return 0;
    }

    @Override
    public synchronized void tick() {
        carryingWeight = HeavyInventory.getContentsTotalWorth(player.getOwner().inventory, false);

        Iterator<Map.Entry<AffineItem, Entry>> iterator = dependencies.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<AffineItem, Entry> entry = iterator.next();

            Entry item = entry.getValue();

            item.tick();

            if (item.needfulness <= 0.001) {
                iterator.remove();
            }
        }
    }

    /**
     * Checks if the player is wearing the specified magical artifact.
     */
    public boolean isWearing(AffineItem item) {
        for (ItemStack i : player.getOwner().getArmorItems()) {
            if (!i.isEmpty() && i.getItem() == item) {
                return true;
            }
        }

        return item.alwaysActive();
    }

    public boolean matches(Tag<Item> tag) {
        for (ItemStack i : player.getOwner().getArmorItems()) {
            if (!i.isEmpty() && i.getItem().isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public double getCarryingWeight() {
        return carryingWeight / 100000D;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        ListTag items = new ListTag();

        for (Entry entry : dependencies.values()) {
            items.add(entry.toNBT());
        }

        compound.put("dependencies", items);
        compound.putDouble("weight", carryingWeight);
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
        carryingWeight = compound.getDouble("weight");
    }

    class Entry implements Tickable, NbtSerialisable {
        int ticksAttached = 0;

        float needfulness = 1;

        AffineItem item;

        Entry() {

        }

        Entry(AffineItem key) {
            this.item = key;
        }

        void reinforce() {
            needfulness = Math.min(30, needfulness + 1);
        }

        @Override
        public void tick() {
            if (isWearing(item)) {
                ticksAttached ++;
                needfulness *= 0.9F;
            } else if (ticksAttached > 0) {
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

            this.item = item instanceof AffineItem ? (AffineItem)item : null;
        }
    }
}
