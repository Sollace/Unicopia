package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.server.world.WeatherConditions;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WeatherVaneBlock extends BlockWithEntity {
    /*private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7.5F, 0, 7.5F, 8.5F, 14, 8.5F),
            Block.createCuboidShape(7, 0, 7, 9, 1, 9)
    );*/

    protected WeatherVaneBlock(Settings settings) {
        super(settings);
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                Block.createCuboidShape(7.5F, 0, 7.5F, 8.5F, 14, 8.5F),
                Block.createCuboidShape(7, 0, 7, 9, 1, 9)
        );
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WeatherVane(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, UBlockEntities.WEATHER_VANE, world.isClient ? WeatherVane::clientTick : WeatherVane::serverTick);
    }

    public static class WeatherVane extends BlockEntity {
        private float angle;

        private float clientAngle;
        private float prevAngle;

        public WeatherVane(BlockPos pos, BlockState state) {
            super(UBlockEntities.WEATHER_VANE, pos, state);
        }

        public float getAngle(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevAngle, clientAngle);
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            angle = nbt.getFloat("angle");
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            nbt.putFloat("angle", angle);
        }

        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt() {
            return createNbt();
        }

        public static void serverTick(World world, BlockPos pos, BlockState state, WeatherVane entity) {
            Vec3d airflow = WeatherConditions.get(world).getWindDirection();
            float angle = (float)Math.atan2(airflow.x, airflow.z) + MathHelper.PI;
            if (Math.signum(entity.angle) != Math.signum(angle)) {
                angle = MathHelper.PI - angle;
            }
            angle %= MathHelper.PI;

            if (angle != entity.angle) {
                entity.angle = angle;
                entity.markDirty();
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.getChunkManager().markForUpdate(pos);
                }

                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), USounds.BLOCK_WEATHER_VANE_ROTATE, SoundCategory.BLOCKS, 1, 0.5F + (float)world.random.nextGaussian());
            }
        }

        public static void clientTick(World world, BlockPos pos, BlockState state, WeatherVane entity) {
            entity.prevAngle = entity.clientAngle;

            float angle = entity.angle + (float)Math.sin(world.getTime() / 70F) * (world.isThundering() ? 30 : 1);

            float step = (Math.abs(entity.clientAngle) - Math.abs(angle)) / 7F;

            if (entity.clientAngle < angle) {
                entity.clientAngle += step;
            } else if (entity.clientAngle > angle) {
                entity.clientAngle -= step;
            }
        }
    }
}
