package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.block.UMaterials;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;

public enum GasState {
    NORMAL,
    DENSE,
    ENCHANTED;

    public FabricBlockSettings configure() {
        return FabricBlockSettings.of(UMaterials.CLOUD)
                .strength(0.5F, 1)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque();
    }

    public boolean isTranslucent() {
        return this == NORMAL;
    }

    public boolean isDense() {
        return this != NORMAL;
    }

    public boolean isTouchable(boolean isPlayer, boolean isPegasus) {
        return isPegasus || this == ENCHANTED || (this == DENSE && isPlayer);
    }

    public boolean canTouch(Entity e) {
        return isTouchable(EquinePredicates.IS_PLAYER.test(e), EquinePredicates.ENTITY_INTERACT_WITH_CLOUD_BLOCKS.test(e));
    }

    public boolean canTouch(CloudInteractionContext context) {
        return context.canTouch(this);
    }

    public boolean canPlace(CloudInteractionContext context) {
        return context.isEmpty() || canTouch(context) && heldCanTouch(context);
    }

    private boolean heldCanTouch(CloudInteractionContext context) {
        ItemStack main = context.getHeldStack();

        if (main.getItem() instanceof BlockItem) {
            Block block = ((BlockItem)main.getItem()).getBlock();

            if (block instanceof Gas && ((Gas)block).getGasState(block.getDefaultState()).canTouch(context)) {
                return true;
            }

            return this == GasState.NORMAL && (
                       block instanceof TorchBlock
                    || block instanceof BedBlock
                    || block instanceof ChestBlock);
        }

        return true;
    }

}