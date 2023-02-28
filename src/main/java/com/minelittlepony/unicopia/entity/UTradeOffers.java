package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.effect.UPotions;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.TradeOffers.Factory;

public interface UTradeOffers {
    static void bootstrap() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.MASON, 1, factories -> {
            factories.add(buyForEmeralds(UItems.GEMSTONE, 1, 1, 30, 2, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 1, factories -> {
            factories.add(buyForEmeralds(UItems.GEMSTONE, 2, 1, 2, 1, 0.05F));
            factories.add(buyForEmeralds(UItems.PEGASUS_FEATHER, 7, 5, 50, 3, 0.15F));
            factories.add(buyForEmeralds(UItems.GRYPHON_FEATHER, 4, 3, 50, 2, 0.12F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 1, factories -> {
            factories.add(buyForEmeralds(UItems.GEMSTONE, 3, 1, 20, 1, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 2, factories -> {
            factories.add(buy(Items.EMERALD, 4, UTags.APPLE_SEEDS, 2, 20, 1, 0.05F));
        });

        TradeOfferHelper.registerWanderingTraderOffers(1, factories -> {
            factories.add(buyTiered(UItems.GEMSTONE, 30, UItems.GOLDEN_FEATHER, 1, UItems.GOLDEN_WING, 1, 30, 2, 0.05F));
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 3), EnchantableItem.enchant(UItems.GEMSTONE.getDefaultStack(), SpellType.REGISTRY.getRandom(rng).get().value()), 20, 1, 0.05F));
            factories.add(buy(UItems.GEMSTONE, 20, UItems.HAY_FRIES, 5, 50, 3, 0.06F));
            factories.add(buy(Items.WHEAT, 17, UItems.HAY_BURGER, 1, 10, 6, 0.08F));
            factories.add(buy(ItemTags.SMALL_FLOWERS, 2, UItems.DAFFODIL_DAISY_SANDWICH, 1, 10, 6, 0.08F));
            factories.add(buy(UItems.ZAP_APPLE, 45, UItems.ZAP_APPLE_JAM_JAR, 5, 50, 3, 0.07F));
            factories.add(buy(UItems.CIDER, 1, UItems.FRIENDSHIP_BRACELET, 1, 6, 1, 0.05F));
            factories.add(buy(UItems.GEMSTONE, 5, UTags.FRESH_APPLES, 2, 12, 3, 0.05F));
            factories.add(new JarredItemTradeOfferFactory());
        });
        TradeOfferHelper.registerWanderingTraderOffers(2, factories -> {
            factories.add(buy(UItems.COOKED_ZAP_APPLE, 45, UItems.ZAP_APPLE_JAM_JAR, 15, 50, 7, 0.17F));
            factories.add(buy(UItems.CRYSTAL_HEART, 1, UItems.MUSIC_DISC_CRUSADE, 1, 10, 6, 0.08F));
            factories.add(buy(UItems.PEGASUS_AMULET, 1, UItems.ALICORN_AMULET, 1, 2, 6, 0.05F));
            factories.add(buyForEmeralds(UItems.FRIENDSHIP_BRACELET, 2, 1, 10, 7, 0.17F));
            factories.add(new SellPotionHoldingItemFactory(
                    new Item[] { Items.ARROW, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE },
                    new Item[] { Items.TIPPED_ARROW, Items.POTION, Items.SPLASH_POTION },
                    5, 5, 2, 12, 30));
        });
    }

    private static TradeOffers.Factory buyForEmeralds(Item item, int count, int returnCount, int maxUses, int experience, float priceChange) {
        return buy(item, count, Items.EMERALD, returnCount, maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory buy(Item item, int count, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(item, count), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory buyTiered(Item item, int count, Item intermediate, int intermediatCount, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(item, count), new ItemStack(intermediate, intermediatCount), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory buy(TagKey<Item> item, int count, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(random(e, item, rng), count), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory buy(Item item, int count, TagKey<Item> returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(item, count), new ItemStack(random(e, returnItem, rng), returnCount), maxUses, experience, priceChange);
    }

    private static Item random(Entity e, TagKey<Item> item, Random rng) {
        return RegistryUtils.entriesForTag(e.world, item).getRandom(rng).get().value();
    }

    static class JarredItemTradeOfferFactory implements TradeOffers.Factory {
        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random rng) {
            TradeOffers.Factory factory = Util.getRandom(TradeOffers.WANDERING_TRADER_TRADES.get(1), rng);

            if (factory == this) {
                return null;
            }

            TradeOffer offer = factory.create(entity, rng);

            return new TradeOffer(offer.getOriginalFirstBuyItem(), offer.getSecondBuyItem(), UItems.FILLED_JAR.withContents(offer.getSellItem()), offer.getUses(), offer.getMaxUses(), offer.getMerchantExperience(), offer.getPriceMultiplier(), offer.getDemandBonus());
        }
    }

    record SellPotionHoldingItemFactory (Item[] secondBuy, Item[] tippedArrow, int secondCount, int sellCount, int price, int maxUses, int experience) implements Factory {
        @Override
        public TradeOffer create(Entity entity, Random random) {
            int index = random.nextInt(tippedArrow.length);
            return new TradeOffer(
                new ItemStack(Items.EMERALD, price),
                new ItemStack(secondBuy[index], secondCount),
                PotionUtil.setPotion(new ItemStack(tippedArrow[index], sellCount), UPotions.REGISTRY.get(random.nextInt(UPotions.REGISTRY.size()))),
                maxUses, experience, 0.05f
            );
        }
    }
}
