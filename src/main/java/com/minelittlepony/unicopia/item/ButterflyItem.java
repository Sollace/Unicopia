package com.minelittlepony.unicopia.item;

import java.util.Arrays;
import java.util.List;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ButterflyItem extends Item implements MultiItem {

    public ButterflyItem(Settings settings) {
        super(settings.component(UDataComponentTypes.BUTTERFLY_VARIANT, new BufferflyVariantComponent(ButterflyEntity.Variant.BUTTERFLY, true)));
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Arrays.stream(ButterflyEntity.Variant.VALUES).map(variant -> BufferflyVariantComponent.set(getDefaultStack(), new BufferflyVariantComponent(variant, true))).toList();
    }
}
