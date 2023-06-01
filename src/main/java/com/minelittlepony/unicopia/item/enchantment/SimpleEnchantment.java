package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class SimpleEnchantment extends Enchantment {

    private final boolean allItems;

    private final EquipmentSlot[] slots;

    private final Options options;

    protected SimpleEnchantment(Options options, EnchantmentTarget target, EquipmentSlot... slots) {
        super(options.rarity, target, slots);
        this.options = options;
        this.allItems = false;
        this.slots = slots;
    }

    protected SimpleEnchantment(Options options, EquipmentSlot... slots) {
        super(options.rarity, EnchantmentTarget.VANISHABLE, slots); // vanishable includes breakable. It's the one that accepts the widest variety of items
        this.options = options;
        this.allItems = true;
        this.slots = slots;
    }

    public void onUserTick(Living<?> user, int level) {

    }

    public void onEquipped(Living<?> user) {

    }

    public void onUnequipped(Living<?> user) {

    }

    @Override
    public boolean isAcceptableItem(ItemStack itemStack) {
       return allItems || super.isAcceptableItem(itemStack);
    }

    public EquipmentSlot[] getSlots() {
        return slots;
    }

    @Override
    public final int getMaxLevel() {
        return options.maxLevel;
    }

    @Override
    public final boolean isCursed() {
        return options.cursed;
    }

    @Override
    public final boolean isTreasure() {
        return options.treasured;
    }

    @Override
    public final boolean isAvailableForEnchantedBookOffer() {
        return options.traded;
    }

    @Override
    public final boolean isAvailableForRandomSelection() {
        return options.looted;
    }

    public static class Data {
        public float level;

        public boolean update(int level) {
            if (level == this.level) {
                return false;
            }
            this.level = level;
            return true;
        }
    }

    public static class Options {
        private boolean cursed;
        private boolean treasured;
        private boolean traded = true;
        private boolean looted = true;
        private Rarity rarity;
        private int maxLevel = 1;

        /**
         * Sets how rare this enchantment is when using the enchantment table.
         * Enchantments with a higher rarity appear less often and costs the user more experience when working with it the anvil.
         */
        public Options rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * Whether this enchantment is considered a negative effect by the game.
         *
         * Cursed enchantments are removed when repairing an item
         * and do not give the user experience points when removed using a grindstone.
         */
        public Options curse() {
            cursed = true;
            return this;
        }

        /**
         * Treasure enchantments only generate in loot tables with high-value items or by trading with villagers.
         * They do not appear in the enchanting table.
         */
        public Options treasure() {
            treasured = true;
            return this;
        }

        /**
         * Loot-Only enchantments do not appear in villager trades.
         * They may still appear in loot table generation and can be found in the enchanting table.
         */
        public Options lootedOnly() {
            traded = false;
            looted = true;
            return this;
        }

        /**
         * Trade-Only enchantments are excluded from loot table generation and do not appear in the enchanting table.
         * They can only be found by trading with villagers.
         */
        public Options tradedOnly() {
            looted = false;
            traded = true;
            return this;
        }

        public Options maxLevel(int level) {
            maxLevel = level;
            return this;
        }
    }
}
