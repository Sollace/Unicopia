package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.Projectile;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class WeatherJarItem extends AliasedBlockItem implements Projectile, ProjectileDelegate.HitListener {
    private final Type type;

    public WeatherJarItem(Block block, Settings settings, Type type) {
        super(block, settings);
        this.type = type;
        Projectile.makeDispensable(this);
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public float getProjectileDamage(ItemStack stack) {
        return 0.5F;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return super.use(world, player, hand);
        }
        return triggerThrow(world, player, hand);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        releaseContents(projectile.getWorld(), projectile.getBlockPos());
    }

    public void releaseContents(World world, BlockPos pos) {
        if (!world.isClient()) {
            ServerWorld sw = (ServerWorld)world;

            if (type == Type.RAIN || type == Type.THUNDER) {
                // clear weather time = number of ticks for which the weather is clear
                // rain time = ticks until rain gets toggled (reset the tick after toggling)
                // thunder time = ticks until thundering gets toggled (reset the tick after toggling)

                // clear weather time
                //   Number of ticks weather must stay clear.
                //   Raining and thundering, and raining/thundering times are kept to false and 0
                // when clear weather time is <= 0
                //   - wait for thunder time to reach zero then toggle thundering
                //   - wait for rain time to reach zero then toggle raining
                // when thunder time is <= 0
                //   - randomly pick a new value for thunder time
                // when rain time is <= 0
                //   - randomly pick a new value for rain time

                sw.setWeather(0, 0, type == Type.RAIN, type == Type.THUNDER);

                if (type == Type.THUNDER) {
                    for (int i = world.random.nextInt(7); i > 0; i--) {
                        AwaitTickQueue.scheduleTask(world, w -> {
                            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                            bolt.setCosmetic(true);
                            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, world.getRandomPosInChunk(
                                    ChunkSectionPos.getBlockCoord(ChunkSectionPos.getSectionCoord(pos.getX())),
                                    0,
                                    ChunkSectionPos.getBlockCoord(ChunkSectionPos.getSectionCoord(pos.getZ())),
                                    15
                            )).up(32)));
                            world.spawnEntity(bolt);
                        }, 15 + world.random.nextInt(12));
                    }
                }
            }

            if (type == Type.LIGHTNING) {
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());

                world.spawnEntity(lightning);
            }
        }

        Vec3d centerPos = pos.toCenterPos();

        if (type == Type.LIGHTNING) {
            ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, centerPos, Vec3d.ZERO);
        }

        if (type == Type.RAIN || type == Type.THUNDER) {
            world.syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, pos, type == Type.THUNDER ? 0x888888 : 0xF8F8F8);

            for (int i = world.random.nextInt(3) + 1; i >= 0; i--) {
                ParticleUtils.spawnParticle(world, UParticles.CLOUDS_ESCAPING,
                        centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                        world.random.nextFloat() - 0.5,
                        0,
                        world.random.nextFloat() - 0.5
                );
            }
        }

        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
    }

    public enum Type {
        RAIN,
        THUNDER,
        LIGHTNING
    }
}