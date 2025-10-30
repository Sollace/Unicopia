package com.minelittlepony.unicopia.util;

import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public record ItemStackSet(Int2ObjectMap<Entry> stacks) implements Iterable<ItemStack> {
    static final ItemStackSet EMPTY = new ItemStackSet(new Int2ObjectOpenHashMap<>());

    public ItemStackSet(Inventory inventory) {
        this(new Int2ObjectOpenHashMap<>());
        InventoryUtil.stream(inventory).forEach(stack -> {
            stacks.computeIfAbsent(ItemStack.hashCode(stack), i -> new Entry(stack)).count += stack.getCount();
        });
    }

    public ItemStackSet(ItemStackSet set) {
        this(new Int2ObjectOpenHashMap<>(set.stacks));
    }

    public ItemStackSet {
        stacks = new Int2ObjectOpenHashMap<>(stacks);
    }

    public ItemStackSet add(ItemStackSet set) {
        if (isEmpty()) {
            return set;
        }
        if (set.isEmpty()) {
            return this;
        }

        ItemStackSet copy = new ItemStackSet(this);
        set.stacks.forEach((i, entry) -> {
            copy.stacks.compute(i, (key, copyEntry) -> copyEntry == null ? new Entry(entry) : copyEntry.add(entry.count));
        });
        return copy;
    }

    public ItemStackSet subtract(ItemStackSet set) {
        if (isEmpty()) {
            return set;
        }
        if (set.isEmpty()) {
            return this;
        }

        ItemStackSet copy = new ItemStackSet(this);
        set.stacks.forEach((i, entry) -> {
            copy.stacks.compute(i, (key, copyEntry) -> copyEntry == null ? new Entry(entry) : copyEntry.add(entry.count));
        });
        return copy;
    }

    public ItemStackSet add(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemStackSet copy = new ItemStackSet(this);
            copy.stacks.computeIfAbsent(ItemStack.hashCode(stack), i -> new Entry(stack)).count += stack.getCount();
            return copy;
        }
        return this;
    }

    public ItemStackSet subtract(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemStackSet copy = new ItemStackSet(this);
            copy.stacks.computeIfPresent(ItemStack.hashCode(stack), (e, entry) -> entry.add(-stack.getCount()));
            return copy;
        }
        return this;
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    static class Entry {
        final ItemStack stack;
        int count;

        public Entry(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
        }

        public Entry(Entry entry) {
            this.stack = entry.stack.copyWithCount(1);
            count = entry.count;
        }

        @Nullable
        public Entry add(int count) {
            this.count += count;
            return this.count >= 0 ? this : null;
        }

        public Iterator<ItemStack> iterator() {
            return new Iterator<>() {
                private int countRemaining = count;

                @Override
                public boolean hasNext() {
                    return countRemaining > 0;
                }

                @Override
                public ItemStack next() {
                    int stackSize = Math.min(stack.getMaxCount(), countRemaining);
                    countRemaining -= stackSize;
                    return stack.copyWithCount(stackSize);
                }
            };
        }
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<>() {
            private final ObjectIterator<Entry> iterator = stacks.values().iterator();
            @Nullable
            private Iterator<ItemStack> stackIterator;

            @Override
            public boolean hasNext() {
                return iterator.hasNext() || (stackIterator != null && stackIterator.hasNext());
            }

            @Override
            public ItemStack next() {
                if (stackIterator == null || !stackIterator.hasNext()) {
                    stackIterator = iterator.next().iterator();
                }
                return stackIterator.next();
            }
        };
    }
}
