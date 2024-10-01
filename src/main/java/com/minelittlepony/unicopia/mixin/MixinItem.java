package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.ItemImpl.GroundTickCallback;
import com.minelittlepony.unicopia.item.ItemDuck;

import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class MixinItem implements ItemDuck {
    private final List<ItemImpl.GroundTickCallback> tickCallbacks = new ArrayList<>();

    @Override
    public List<GroundTickCallback> getCallbacks() {
        return tickCallbacks;
    }
}
