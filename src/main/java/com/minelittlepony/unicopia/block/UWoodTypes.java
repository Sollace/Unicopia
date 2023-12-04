package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;

import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.WoodType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public interface UWoodTypes {
    WoodType PALM = register("palm");
    RegistryKey<TerraformBoatType> PALM_BOAT_TYPE = TerraformBoatTypeRegistry.createKey(Unicopia.id("palm"));

    BlockSetType CLOUD = new BlockSetTypeBuilder()
            .soundGroup(BlockSoundGroup.WOOL)
            .doorCloseSound(USounds.Vanilla.BLOCK_WOOL_HIT)
            .doorOpenSound(USounds.Vanilla.BLOCK_WOOL_BREAK)
            .register(Unicopia.id("cloud"));
    BlockSetType CRYSTAL = BlockSetTypeBuilder.copyOf(BlockSetType.IRON)
            .soundGroup(BlockSoundGroup.AMETHYST_BLOCK)
            .openableByHand(true)
            .register(Unicopia.id("crystal"));

    static WoodType register(String name) {
        Identifier id = Unicopia.id(name);
        return new WoodTypeBuilder().register(id, new BlockSetTypeBuilder().register(id));
    }
}
