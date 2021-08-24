package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.item.UItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public interface UTradeOffers {
    static void bootstrap() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.MASON, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(UItems.GEMSTONE.getDefaultStack(), Items.EMERALD.getDefaultStack(), 30, 2, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 2), Items.EMERALD.getDefaultStack(), 20, 1, 0.05F));
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 30), UItems.GOLDEN_FEATHER.getDefaultStack(), UItems.GOLDEN_WING.getDefaultStack(), 30, 2, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 3), Items.EMERALD.getDefaultStack(), 20, 1, 0.05F));
        });
    }
}
