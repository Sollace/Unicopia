package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.init.UBlocks;
import com.minelittlepony.util.fixers.BlockFixer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;

public class Fixes {

    static void init(DataFixer fixer) {

        CompoundDataFixer forgeDataFixer = (CompoundDataFixer)fixer;

        try {
            ModFixs modfix = forgeDataFixer.init(Unicopia.MODID, 1342);

            modfix.registerFix(FixTypes.CHUNK, new FixCloudBlocks());
            modfix.registerFix(FixTypes.ITEM_INSTANCE, new FixCloudItems());
        } catch (Throwable ignored) {
            // no way to check if our fixer is already registered.
            // so just do it anyway and ignore the error.
            // @FUF(reason = "FUF")
        }
    }

    static class FixCloudItems implements IFixableData {
        @Override
        public int getFixVersion() {
            return 1342;
        }

        @Override
        public NBTTagCompound fixTagCompound(NBTTagCompound compound) {

            if (compound.hasKey("id", 8) && compound.hasKey("Damage", 3)) {
                String id = compound.getString("id");
                int damage = compound.getInteger("Damage");

                if (id == "unicopia:cloud_block") {
                    if (damage == 1) {
                        damage = 0;
                        id = "unicopia:packed_cloud_block";
                    } else if (damage == 2) {
                        damage = 0;
                        id = "unicopia:enchanted_cloud_block";
                    }
                }

                if (id == "unicopia:cloud_slab") {
                    if (damage == 1) {
                        damage = 0;
                        id = "unicopia:packed_cloud_slab";
                    } else if (damage == 2) {
                        damage = 0;
                        id = "unicopia:enchanted_cloud_slab";
                    }
                }

                compound.setString("id", id);
                compound.setInteger("Damage", 0);
            }

            return compound;
        }
    }

    static class FixCloudBlocks extends BlockFixer {

        @Override
        public int getFixVersion() {
            return 1342;
        }

        @Override
        protected IBlockState fixBlockState(int id, int metadata) {
            if (id == Block.getIdFromBlock(UBlocks.normal_cloud) && metadata != 0) {
                if (metadata == 1) {
                    return UBlocks.packed_cloud.getDefaultState();
                }
                if (metadata == 2) {
                    return UBlocks.enchanted_cloud.getDefaultState();
                }
            }

            int shifted = metadata % 8;

            if (id == Block.getIdFromBlock(UBlocks.cloud_slab) && shifted != 0) {
                if (shifted == 1) {
                    return UBlocks.packed_cloud_slab.getStateFromMeta(metadata - shifted);
                }
                if (shifted == 2) {
                    return UBlocks.enchanted_cloud_slab.getStateFromMeta(metadata - shifted);
                }
            }

            return null;
        }
    }
}
