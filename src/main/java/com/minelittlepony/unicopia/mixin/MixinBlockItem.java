package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.mob.MimicEntity;
import com.minelittlepony.unicopia.item.component.MimicComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.server.world.WaterLoggingManager;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockItem.class)
abstract class MixinBlockItem extends Item {
    MixinBlockItem() {super(null); }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState onGetPlacementState(BlockState state, ItemPlacementContext context) {
        return WaterLoggingManager.getInstance().getPlacementState(state, context);
    }

    @ModifyReturnValue(method = "writeNbtToBlockEntity", at = @At("RETURN"))
    private static boolean onWriteNbtToBlockEntity(boolean returnValue, World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
        MimicComponent mimic = stack.get(UDataComponentTypes.MIMIC);
        if (mimic != null) {
            if (world.getBlockEntity(pos) instanceof MimicEntity.MimicGeneratable generatable) {
                generatable.setMimic(mimic.mimic());
            }
        }
        return returnValue;
    }
}
