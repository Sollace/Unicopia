package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Unicopia;
import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;

import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.block.WoodType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public interface UWoodTypes {
    WoodType PALM = register("palm");
    RegistryKey<TerraformBoatType> PALM_BOAT_TYPE = TerraformBoatTypeRegistry.createKey(Unicopia.id("palm"));

    static WoodType register(String name) {
        Identifier id = Unicopia.id(name);
        return new WoodTypeBuilder().register(id, new BlockSetTypeBuilder().register(id));
    }




}
