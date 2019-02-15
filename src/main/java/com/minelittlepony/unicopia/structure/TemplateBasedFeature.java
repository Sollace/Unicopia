package com.minelittlepony.unicopia.structure;

import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;

public abstract class TemplateBasedFeature extends AbstractFeature {

    public TemplateBasedFeature() {}

    protected TemplateBasedFeature(Random rand, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        super(rand, x, y, z, sizeX, sizeY, sizeX);
    }


    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounds) {

        if (!offsetToAverageGroundLevel(world, bounds, -1)) {
            return false;
        }

        bounds = getBoundingBox();

        BlockPos startPos = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);

        Rotation[] orientations = Rotation.values();

        TemplateManager templates = world.getSaveHandler().getStructureTemplateManager();

        PlacementSettings placement = new PlacementSettings()
                .setRotation(orientations[rand.nextInt(orientations.length)])
                .setReplacedBlock(Blocks.STRUCTURE_VOID)
                .setBoundingBox(bounds);

        return addComponentParts(world, startPos, templates, placement);
    }

    protected void applyTemplate(World world, BlockPos startPos, TemplateManager templates, PlacementSettings placement, ResourceLocation templateId) {
        Template template = templates.get(world.getMinecraftServer(), templateId);

        template.addBlocksToWorldChunk(world, startPos, placement);

        Map<BlockPos, String> map = template.getDataBlocks(startPos, placement);

        for (Entry<BlockPos, String> entry : map.entrySet()) {
            applyLootTables(world, entry.getKey(), entry.getValue());
        }
    }

    protected void applyLootTables(World world, BlockPos pos, String blockId) {
        ResourceLocation lootTable = getLootTable(blockId);

        if (lootTable != null) {

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            TileEntity tileentity = world.getTileEntity(pos.down());

            if (tileentity instanceof TileEntityLockableLoot) {
                ((TileEntityLockableLoot)tileentity).setLootTable(lootTable, world.rand.nextLong());
            }
        }
    }

    @Nullable
    protected ResourceLocation getLootTable(String blockId) {
        if ("chest".equals(blockId)) {
            return LootTableList.CHESTS_STRONGHOLD_LIBRARY;
        }

        return null;
    }

    public abstract boolean addComponentParts(World world, BlockPos startPos, TemplateManager templates, PlacementSettings placement);
}
