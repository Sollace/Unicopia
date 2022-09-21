package com.minelittlepony.unicopia.mixin.trinkets;

import org.spongepowered.asm.mixin.*;

import com.minelittlepony.unicopia.trinkets.TrinketsDelegateImpl;
import dev.emi.trinkets.api.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(TrinketItem.class)
abstract class MixinTrinketItem {
    // overwrite with a max-count aware version (fixes inserting)
    @Overwrite
    public static boolean equipItem(PlayerEntity user, ItemStack stack) {
        return TrinketsDelegateImpl.INSTANCE.getInventories(user)
                .filter(inv -> TrinketsDelegateImpl.tryInsert(inv, stack, user))
                .findFirst()
                .isPresent();
    }
}
