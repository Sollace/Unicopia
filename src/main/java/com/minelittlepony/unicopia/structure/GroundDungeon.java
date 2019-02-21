package com.minelittlepony.unicopia.structure;

import java.util.Random;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class GroundDungeon extends TemplateBasedFeature {

    private static final ResourceLocation[] VARIANTS = new ResourceLocation[] {
            new ResourceLocation(Unicopia.MODID, "ground/tower"),
            new ResourceLocation(Unicopia.MODID, "ground/temple_with_book"),
            new ResourceLocation(Unicopia.MODID, "ground/temple_without_book"),
            new ResourceLocation(Unicopia.MODID, "ground/wizard_tower_red"),
            new ResourceLocation(Unicopia.MODID, "ground/wizard_tower_blue")
    };

    public GroundDungeon() {
    }

    public GroundDungeon(Random rand, int x, int z) {
        super(rand, x, 64, z, 7, 5, 8);
    }

    @Override
    public boolean addComponentParts(World world, BlockPos startPos, TemplateManager templates, PlacementSettings placement) {

        int index = world.rand.nextInt(VARIANTS.length);

        applyTemplate(world, startPos, templates, placement, VARIANTS[index]);

        return true;
    }
}
