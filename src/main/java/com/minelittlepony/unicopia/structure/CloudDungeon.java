package com.minelittlepony.unicopia.structure;

import java.util.Random;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class CloudDungeon extends TemplateBasedFeature {

    private static final ResourceLocation TEMPLE = new ResourceLocation(Unicopia.MODID, "cloud/temple_small");
    private static final ResourceLocation HOUSE = new ResourceLocation(Unicopia.MODID, "cloud/house_small");

    public CloudDungeon() {
    }

    public CloudDungeon(Random rand, int x, int z) {
        super(rand, x, 0, z, 7, 5, 8);
    }

    @Override
    public boolean addComponentParts(World world, BlockPos startPos, TemplateManager templates, PlacementSettings placement) {

        if (world.rand.nextBoolean()) {
            applyTemplate(world, startPos, templates, placement, TEMPLE);
        } else {
            applyTemplate(world, startPos, templates, placement, HOUSE);
        }

        return true;
    }

    @Override
    protected boolean tryFitTerrain(World world, StructureBoundingBox bounds, int yOffset) {
        return true;
    }
}
