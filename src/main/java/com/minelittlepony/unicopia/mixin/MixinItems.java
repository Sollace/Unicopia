package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.item.VanillaOverrides;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(Items.class)
abstract class MixinItems {
    @ModifyVariable(method = "register(Lnet/minecraft/util/Identifier;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;",
            at = @At("HEAD"),
            index = 1,
            argsOnly = true)
    private static Item modifyItem(Item item, Identifier id, Item itemAlso) {
        // Registry#containsId is client-only :thonkjang:
        // TODO: Move onGroundTick() event to MixinItem and make it a registerable event so we don't have to do this
        Item replacement = VanillaOverrides.REGISTRY.get(id);
        if (replacement != null) {
            Registry.register(Registry.ITEM, new Identifier("unicopia_overriden", id.getPath()), item);
            return replacement;
        }
        return item;
    }
}
