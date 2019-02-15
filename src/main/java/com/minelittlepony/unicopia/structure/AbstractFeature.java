package com.minelittlepony.unicopia.structure;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.TemplateManager;

public abstract class AbstractFeature extends StructureComponent {

    protected int width;

    protected int height;

    protected int depth;

    protected int horizontalPos = -1;

    public AbstractFeature() {
    }

    protected AbstractFeature(Random rand, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        super(0);

        width = sizeX;
        height = sizeY;
        depth = sizeZ;

        setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(rand));

        if (getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            boundingBox = new StructureBoundingBox(x, y, z, x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1);
        } else {
            boundingBox = new StructureBoundingBox(x, y, z, x + sizeZ - 1, y + sizeY - 1, z + sizeX - 1);
        }
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("Width", width);
        tagCompound.setInteger("Height", height);
        tagCompound.setInteger("Depth", depth);
        tagCompound.setInteger("HPos", horizontalPos);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templater) {
        width = tagCompound.getInteger("Width");
        height = tagCompound.getInteger("Height");
        depth = tagCompound.getInteger("Depth");
        horizontalPos = tagCompound.getInteger("HPos");
    }

    protected int getAverageStructureAltitude() {
        return 64;
    }

    protected boolean offsetToAverageGroundLevel(World worldIn, StructureBoundingBox structurebb, int yOffset) {
        if (horizontalPos >= 0) {
            return true;
        }

        int xOff = 0;
        int offsetIncrements = 0;

        int targetY = getAverageStructureAltitude();

        MutableBlockPos pos = new MutableBlockPos();

        for (int z = boundingBox.minZ; z <= boundingBox.maxZ; ++z) {
            for (int x = boundingBox.minX; x <= boundingBox.maxX; ++x) {
                pos.setPos(x, targetY, z);

                if (structurebb.isVecInside(pos)) {
                    xOff += Math.max(worldIn.getTopSolidOrLiquidBlock(pos).getY(), worldIn.provider.getAverageGroundLevel());
                    offsetIncrements++;
                }
            }
        }

        if (offsetIncrements == 0) {
            return false;
        }

        horizontalPos = xOff / offsetIncrements;
        boundingBox.offset(0, horizontalPos - boundingBox.minY + yOffset, 0);
        return true;
    }

    public abstract static class Start extends StructureStart {
        public Start() {

        }

        public Start(World world, Random rand, int x, int z) {
            this(world, rand, x, z, world.getBiome(new BlockPos(x * 16 + 8, 0, z * 16 + 8)));
        }

        public Start(World world, Random rand, int x, int z, Biome biome) {
            super(x, z);
            addComponents(world, rand, x, z, biome);
            updateBoundingBox();
        }

        protected abstract void addComponents(World world, Random ran, int x, int z, Biome biome);
    }
}