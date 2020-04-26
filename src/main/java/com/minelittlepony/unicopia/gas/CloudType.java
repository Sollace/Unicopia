package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.UMaterials;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.entity.Entity;
import net.minecraft.sound.BlockSoundGroup;

public enum CloudType {
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

    public boolean isTouchable(boolean isPlayer) {
        return this == ENCHANTED || (this == DENSE && isPlayer);
    }

    public boolean canInteract(Entity e) {
        return CloudInteractionContext.of(e).canTouch(this);
    }
}