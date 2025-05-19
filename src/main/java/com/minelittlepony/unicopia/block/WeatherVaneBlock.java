package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WeatherVaneBlock extends BlockWithEntity {
    public static final MapCodec<WeatherVaneBlock> CODEC = createCodec(WeatherVaneBlock::new);
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7.5F, 0, 7.5F, 8.5F, 14, 8.5F),
            Block.createCuboidShape(7, 0, 7, 9, 1, 9)
    );

    protected WeatherVaneBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends WeatherVaneBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
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
        private float lastAngle;

        private Vec3d airflow = Vec3d.ZERO;

        public WeatherVane(BlockPos pos, BlockState state) {
            super(UBlockEntities.WEATHER_VANE, pos, state);
        }

        public float getAngle(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevAngle, clientAngle);
        }

        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            angle = nbt.getFloat("angle");
            airflow = new Vec3d(nbt.getDouble("windX"), 0, nbt.getDouble("windZ"));
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.putFloat("angle", angle);
            nbt.putDouble("windX", airflow.x);
            nbt.putDouble("windZ", airflow.z);
        }

        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt(WrapperLookup lookup) {
            return createNbt(lookup);
        }

        public static void serverTick(World world, BlockPos pos, BlockState state, WeatherVane entity) {
            Vec3d airflow = WeatherConditions.get(world).getWindDirection();
            float angle = (WeatherConditions.get(world).getWindYaw() % MathHelper.PI);

            entity.lastAngle = entity.prevAngle;
            entity.prevAngle = entity.angle;
            if (angle != entity.angle) {
                entity.angle = angle;

                entity.airflow = airflow;
                entity.markDirty();
                if (world instanceof ServerWorld sw) {
                    sw.getChunkManager().markForUpdate(pos);
                }

                if (entity.lastAngle == entity.prevAngle) {
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), USounds.BLOCK_WEATHER_VANE_ROTATE, SoundCategory.BLOCKS, 1, 0.5F + (float)world.random.nextGaussian());
                }
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

            if (world.random.nextInt(3) == 0) {
                float radius = 10;
                for (int i = 0; i < 5; i++) {
                    world.addImportantParticle(new TargetBoundParticleEffect(UParticles.WIND, null),
                            world.getRandom().nextTriangular(pos.getX(), radius),
                            world.getRandom().nextTriangular(pos.getY(), radius),
                            world.getRandom().nextTriangular(pos.getZ(), radius),
                            entity.airflow.x / 10F, 0, entity.airflow.z / 10F
                    );
                }
            }
        }
    }
}
