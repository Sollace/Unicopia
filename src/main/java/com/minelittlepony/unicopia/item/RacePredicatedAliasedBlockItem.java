package com.minelittlepony.unicopia.item;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.Block;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class RacePredicatedAliasedBlockItem extends AliasedBlockItem {

    private Predicate<Race> predicate;

    public RacePredicatedAliasedBlockItem(Block block, Settings settings, Predicate<Race> predicate) {
        super(block, settings);
        this.predicate = predicate;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Pony pony = Pony.of(context.getPlayer());
        if (pony == null || !predicate.test(pony.getSpecies())) {
            return ActionResult.FAIL;
        }

        return super.useOnBlock(context);
     }
}
