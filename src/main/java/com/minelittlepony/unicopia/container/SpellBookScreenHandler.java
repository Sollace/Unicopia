package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

public class SpellBookScreenHandler extends ScreenHandler {

    protected SpellBookScreenHandler(int syncId, PlayerInventory inv) {
        super(UScreenHandlers.SPELL_BOOK, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EquinePredicates.IS_CASTER.test(player);
    }
}
