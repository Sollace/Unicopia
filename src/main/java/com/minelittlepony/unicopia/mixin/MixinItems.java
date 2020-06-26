package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.world.item.VanillaOverrides;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Mixin(Items.class)
abstract class MixinItems {
    @ModifyVariable(method = "register(Lnet/minecraft/util/Identifier;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;",
            at = @At("HEAD"),
            index = 1,
            argsOnly = true)
    private static Item modifyItem(Item item, Identifier id, Item itemAlso) {
        if (VanillaOverrides.REGISTRY.containsId(id)) {
            return VanillaOverrides.REGISTRY.get(id);
        }
        return item;
    }
}
