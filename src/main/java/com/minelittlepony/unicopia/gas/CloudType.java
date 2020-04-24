package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.block.UMaterials;
import com.minelittlepony.unicopia.entity.CloudEntity;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;

public enum CloudType {
    NORMAL,
    DENSE,
    ENCHANTED;

    public FabricBlockSettings configure() {
        FabricBlockSettings settings = FabricBlockSettings.of(UMaterials.CLOUD)
                .strength(0.5F, 1)
                .sounds(BlockSoundGroup.WOOL);
        if (this != NORMAL ) {
            settings.nonOpaque();
        }
        return settings;
    }

    public boolean canInteract(Entity e) {
        if (e == null) {
            return false;
        }

        if (this == ENCHANTED) {
            return true;
        }

        if (e instanceof PlayerEntity) {

            if (this == DENSE) {
                return true;
            }

            return EquinePredicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)e)
                || (EquinePredicates.MAGI.test(e) && CloudEntity.getFeatherEnchantStrength((PlayerEntity)e) > 0);
        }

        if (e instanceof ItemEntity) {
            return EquinePredicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)e);
        }

        if (e instanceof CloudEntity && e.hasVehicle()) {
            return canInteract(e.getVehicle());
        }

        return false;
    }
}