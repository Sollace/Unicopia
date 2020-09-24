package com.minelittlepony.unicopia.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.DyeColor;

@Mixin(SheepEntity.class)
public interface MixinSheepEntity {
    @Accessor("DROPS")
    static Map<DyeColor, ItemConvertible> getDrops() {
        return null;
    }
}
