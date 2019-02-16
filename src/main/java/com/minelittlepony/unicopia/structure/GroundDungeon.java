package com.minelittlepony.unicopia.structure;

import java.util.Random;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class GroundDungeon extends TemplateBasedFeature {

    private static final ResourceLocation TOWER = new ResourceLocation(Unicopia.MODID, "ground/tower");
    private static final ResourceLocation TEMPLE_1 = new ResourceLocation(Unicopia.MODID, "ground/temple_with_book");

    public GroundDungeon() {
    }

    public GroundDungeon(Random rand, int x, int z) {
        super(rand, x, 64, z, 7, 5, 8);
    }

    @Override
    public boolean addComponentParts(World world, BlockPos startPos, TemplateManager templates, PlacementSettings placement) {

        if (world.rand.nextBoolean()) {
            applyTemplate(world, startPos, templates, placement, TOWER);
        } else {
            applyTemplate(world, startPos, templates, placement, TEMPLE_1);
        }

        return true;
    }
}
