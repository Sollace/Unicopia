package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class JarItem extends Item implements ProjectileDelegate, ItemImpl.GroundTickCallback {

    private final boolean rain;
    private final boolean thunder;
    private final boolean lightning;

    public JarItem(Settings settings, boolean rain, boolean thunder, boolean lightning) {
        super(settings);
        this.rain = rain;
        this.thunder = thunder;
        this.lightning = lightning;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                USounds.ENTITY_JAR_THROW, SoundCategory.NEUTRAL,
                0.5F,
                0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            MagicProjectileEntity projectile = new MagicProjectileEntity(world, player);
            projectile.setItem(stack);
            projectile.setThrowDamage(getProjectileDamage(stack));
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 1);

            world.spawnEntity(projectile);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    protected float getProjectileDamage(ItemStack stack) {
        return 0.5F;
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = item.get().getMaster();

        entity.setInvulnerable(true);

        if (!lightning
                && !entity.world.isClient
                && !entity.isRemoved()
                && entity.getItemAge() > 100
                && entity.world.isThundering()
                && entity.world.isSkyVisible(entity.getBlockPos())
                && entity.world.random.nextInt(130) == 0) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.world);
            lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());

            entity.remove(RemovalReason.DISCARDED);
            entity.world.spawnEntity(lightning);

            ItemEntity neu = EntityType.ITEM.create(entity.world);
            neu.copyPositionAndRotation(entity);
            neu.setStack(new ItemStack(this == UItems.RAIN_CLOUD_JAR ? UItems.STORM_CLOUD_JAR : UItems.LIGHTNING_JAR));
            neu.setInvulnerable(true);

            entity.world.spawnEntity(neu);

            ItemEntity copy = EntityType.ITEM.create(entity.world);
            copy.copyPositionAndRotation(entity);
            copy.setInvulnerable(true);
            copy.setStack(entity.getStack());
            copy.getStack().decrement(1);

            entity.world.spawnEntity(copy);
        }
        return ActionResult.PASS;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        onImpact(projectile);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        onImpact(projectile);
    }

    protected void onImpact(MagicProjectileEntity projectile) {
        if (!projectile.isClient()) {
            ServerWorld world = (ServerWorld)projectile.world;

            if (rain || thunder) {
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

                world.setWeather(0, 0, rain, thunder);

                if (thunder) {
                    for (int i = world.random.nextInt(7); i > 0; i--) {
                        AwaitTickQueue.scheduleTask(world, w -> {
                            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                            bolt.setCosmetic(true);
                            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, world.getRandomPosInChunk(
                                    ChunkSectionPos.getBlockCoord(ChunkSectionPos.getSectionCoord(projectile.getX())),
                                    0,
                                    ChunkSectionPos.getBlockCoord(ChunkSectionPos.getSectionCoord(projectile.getZ())),
                                    15
                            )).up(32)));
                            world.spawnEntity(bolt);
                        }, 15 + world.random.nextInt(12));
                    }
                }
            }

            if (lightning) {
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                lightning.refreshPositionAfterTeleport(projectile.getX(), projectile.getY(), projectile.getZ());

                world.spawnEntity(lightning);
            }
        }

        if (lightning) {
            ParticleUtils.spawnParticle(projectile.world, UParticles.LIGHTNING_BOLT, projectile.getPos(), Vec3d.ZERO);
        }

        if (rain || thunder) {
            projectile.world.syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, projectile.getBlockPos(), thunder ? 0x888888 : 0xF8F8F8);

            for (int i = projectile.world.random.nextInt(3) + 1; i >= 0; i--) {
                ParticleUtils.spawnParticle(projectile.world, UParticles.CLOUDS_ESCAPING,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        projectile.world.random.nextFloat() - 0.5,
                        0,
                        projectile.world.random.nextFloat() - 0.5
                );
            }
        }

        projectile.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, projectile.getBlockPos(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
    }
}
