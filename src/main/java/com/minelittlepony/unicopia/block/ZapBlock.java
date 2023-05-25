package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapBlock extends Block {
    public static final BooleanProperty NATURAL = BooleanProperty.of("natural");

    private final Block artificialModelBlock;

    ZapBlock(Settings settings, Block artificialModelBlock) {
        super(settings.strength(500, 1200));
        setDefaultState(getDefaultState().with(NATURAL, true));
        this.artificialModelBlock = artificialModelBlock;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NATURAL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(NATURAL, false);
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos, player);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (!state.get(NATURAL)) {
            return artificialModelBlock.calcBlockBreakingDelta(artificialModelBlock.getDefaultState(), player, world, pos);
        }

        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }


    public static void triggerLightning(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Vec3d center = Vec3d.ofCenter(pos);
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            world.getOtherEntities(null, Box.from(center).expand(7)).forEach(other -> {
                float dist = (float)other.getPos().distanceTo(center);
                if (dist < 4) {
                    other.onStruckByLightning(serverWorld, lightning);
                } else {
                    float damage = 3 / dist;
                    if (damage > 1) {
                        other.damage(DamageSource.LIGHTNING_BOLT, damage);
                    }
                }
            });
        }
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(state));
        ParticleUtils.spawnParticle(world, UParticles.LIGHTNING_BOLT, Vec3d.ofCenter(pos), Vec3d.ZERO);
    }
}
