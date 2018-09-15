package com.minelittlepony.unicopia.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityParticleEmitter {

    public void emitDestructionParticles(Entity e, IBlockState blockState) {
        float f = 0.1f;
        int total = 64 * (int)(e.width * e.height * e.width);
        for (int i = 0; i < total; i++) {
            double x = MathHelper.nextDouble(e.getEntityWorld().rand, e.posX - e.width/2 - f, e.posX + e.width/2 + f);
            double y = MathHelper.nextDouble(e.getEntityWorld().rand, e.posY - f, e.posY + e.height + f);
            double z = MathHelper.nextDouble(e.getEntityWorld().rand, e.posZ - e.width/2 - f, e.posZ + e.width/2 + f);
            spawnDigginFX(e.getEntityWorld(), x, y, z, x - (int)x - 0.5, y - (int)y - 0.5, z - (int)z - 0.5, blockState, 1, 1);
        }
    }

    public void emitDiggingParticles(Entity e, IBlockState blockState) {
        for (int side = 0; side < 6; side++) {
            addBlockHitEffectsToEntity(e, blockState, side);
        }
    }

    private void addBlockHitEffectsToEntity(Entity e, IBlockState blockState, int side) {
        side = side % 6;
        float f = 0.25f;
        double x = MathHelper.nextDouble(e.getEntityWorld().rand, e.posX - e.width/2 - f, e.posX + e.width/2 + f);
        double y = MathHelper.nextDouble(e.getEntityWorld().rand, e.posY - f, e.posY + e.height + f);
        double z = MathHelper.nextDouble(e.getEntityWorld().rand, e.posZ - e.width/2 - f, e.posZ + e.width/2 + f);

        double vX = 0;
        double vY = 0;
        double vZ = 0;

        if (side == 0) y = e.posY - f;
        if (side == 1) {
            y = e.posY + e.height + f;
            vY += 0.5;
        }
        if (side == 2) {
            z = e.posZ - e.width/2 - f;
            vZ -= 0.5;
        }
        if (side == 3) {
            z = e.posZ + e.width/2 + f;
            vZ += 0.5;
        }
        if (side == 4) {
            x = e.posX - e.width/2 - f;
            vX -= 0.5;
        }
        if (side == 5) {
            x = e.posX + e.width/2 + f;
            vX += 0.5;
        }

        spawnDigginFX(e.getEntityWorld(), x, y, z, vX, vY, vZ, blockState, 0.2F, 0.6F);
    }

    protected void spawnDigginFX(World w, double x, double y, double z, double vX, double vY, double vZ, IBlockState blockState, float multScale, float multVel) {
        if (w instanceof WorldServer) {
            ((WorldServer)w).spawnParticle(EnumParticleTypes.BLOCK_CRACK, false, x, y, z, 1, 0, 0, 0, Math.sqrt(vX * vX + vY * vY + vZ * vZ) * multVel, Block.getStateId(blockState));
        }
    }
}
