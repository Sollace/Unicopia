package com.minelittlepony.unicopia.block;

import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;

public interface UMaterials {
    Material CLOUD = new Material.Builder(MaterialColor.WHITE).allowsMovement().build();
    Material HIVE = new Material.Builder(MaterialColor.NETHER).build();
}
