package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.particle.Particles;

import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGlowingGem extends BlockTorch implements ICloudBlock {


    private static final double A = 5/16D;
    private static final double B = 6/16D;

    private static final double C = 10/16D;

    // tiltedOffWall
    private static final double F = 10/16D;

    // tiltedMinY
    private static final double E = 3/16D;

    protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(
            7/16D, 0, 7/16D,
            9/16D, 1, 9/16D
    );
    protected static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(
            B, E, F,
            C, 1, 1
    );
    protected static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(
            B, E, 0,
            C, 1, A
    );
    protected static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(
            F, E, B,
            1, 1, C
    );
    protected static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(
            0, E, B,
            A, 1, C
    );

    public BlockGlowingGem(String domain, String name) {
        super();
        setTranslationKey(name);
        setRegistryName(domain, name);

        setHardness(0);
        setLightLevel(1);
        setSoundType(SoundType.GLASS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

        switch (state.getValue(FACING)) {
            case EAST: return TORCH_EAST_AABB;
            case WEST: return TORCH_WEST_AABB;
            case SOUTH: return TORCH_SOUTH_AABB;
            case NORTH: return TORCH_NORTH_AABB;
            default: return STANDING_AABB;
        }
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        EnumFacing facing = state.getValue(FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;

        double drop = 0.22D;
        double variance = 0.27D;

        if (facing.getAxis().isHorizontal()) {
            facing = facing.getOpposite();

            x += variance * facing.getXOffset();
            y += drop;
            z += variance * facing.getZOffset();
        }

        for (int i = 0; i < 3; i++) {
            Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false,
                    x - 0.3, y, z - 0.3,
                    rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
        }
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.ENCHANTED;
    }
}
