package com.minelittlepony.util.fixers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.chunk.NibbleArray;

public abstract class BlockFixer implements IFixableData {

    protected NBTTagCompound fixChunk(NBTTagCompound chunk) {
        byte[] blocks = chunk.getByteArray("Blocks");

        NibbleArray data = new NibbleArray(chunk.getByteArray("Data"));
        NibbleArray add = chunk.hasKey("Add", 7) ? new NibbleArray(chunk.getByteArray("Add")) : null;

        boolean altered = false;

        for (int i = 0; i < 4096; i++) {
            int xPos = i & 15;
            int yPos = i >> 8 & 15;
            int zPos = i >> 4 & 15;

            int extendedBlockId = (add == null ? 0 : add.get(xPos, yPos, zPos));
            int baseBlockId = blocks[i] & 255;


            int blockid = (extendedBlockId << 8) | baseBlockId;
            int metadata = data.get(xPos, yPos, zPos);

            int blockStateId = (blockid << 4) | metadata;

            Block block = Block.getBlockById(blockid);


            if (block != null) {
                IBlockState state = fixBlockState(blockid, metadata);

                if (state != null) {
                    @SuppressWarnings("deprecation")
                    int newBlockStateId = Block.BLOCK_STATE_IDS.get(state);

                    if (newBlockStateId != blockStateId) {
                        metadata = newBlockStateId & 15;
                        blockid = newBlockStateId >> 4;
                        extendedBlockId = blockid >> 8;
                        baseBlockId = blockid & 255;

                        if (extendedBlockId != 0) {
                            if (add == null) {
                                add = new NibbleArray(new byte[data.getData().length]);
                            }

                            add.set(xPos, yPos, zPos, extendedBlockId);
                        }

                        data.set(xPos, yPos, zPos, metadata);
                        blocks[i] = (byte)blockid;

                        altered = true;
                    }
                }
            }
        }

        if (altered) {
            if (add != null) {
                chunk.setByteArray("Add", add.getData());
            }
            chunk.setByteArray("Blocks", blocks);
            chunk.setByteArray("Data", data.getData());
        }

        return chunk;
    }

    protected abstract IBlockState fixBlockState(int id, int metadata);

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {

        if (compound.hasKey("Level", 10)) {
            NBTTagCompound level = compound.getCompoundTag("Level");

            if (level.hasKey("Sections", 9)) {
                NBTTagList sections = level.getTagList("Sections", 10);

                for (int i = 0; i < sections.tagCount(); i++) {
                    NBTTagCompound chunk = sections.getCompoundTagAt(i);

                    if (chunk.hasKey("Blocks", 7) && chunk.hasKey("Data", 7)) {
                        sections.set(i, fixChunk(chunk));
                    }
                }
            }
        }

        return compound;
    }
}