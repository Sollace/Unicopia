package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.item.ItemDuck;

import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck {
    private final List<GroundTickCallback> tickCallbacks = new ArrayList<>();

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }
}
