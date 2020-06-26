package com.minelittlepony.unicopia.world.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;

public interface UMaterials {
    Material CLOUD = new FabricMaterialBuilder(MaterialColor.WHITE).allowsMovement().lightPassesThrough().notSolid().build();
    Material HIVE = new Material.Builder(MaterialColor.NETHER).build();
    Material CHITIN = new Material.Builder(MaterialColor.BLACK).build();
    Material STICK = new FabricMaterialBuilder(MaterialColor.WOOD).allowsMovement().burnable().destroyedByPiston().lightPassesThrough().notSolid().build();
}
