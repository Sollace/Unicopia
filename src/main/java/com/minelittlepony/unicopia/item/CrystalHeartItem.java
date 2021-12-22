package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.entity.FloatingArtefactEntity.State;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrystalHeartItem extends Item implements FloatingArtefactEntity.Artifact {

    public CrystalHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        World world = context.getWorld();
        BlockPos blockPos = new ItemPlacementContext(context).getBlockPos();

        Box placementArea = UEntities.FLOATING_ARTEFACT.getDimensions().getBoxAt(Vec3d.ofBottomCenter(blockPos));

        if (!world.isSpaceEmpty(null, placementArea)) {
            return ActionResult.FAIL;
        }

        if (world instanceof ServerWorld) {

            FloatingArtefactEntity entity = UEntities.FLOATING_ARTEFACT.create((ServerWorld)world, context.getStack().getNbt(), null, context.getPlayer(), blockPos, SpawnReason.SPAWN_EGG, false, true);

            if (entity == null) {
                return ActionResult.FAIL;
            }

            entity.setStack(context.getStack().split(1));
            ((ServerWorld)world).spawnEntityAndPassengers(entity);

            entity.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.75F, 0.8F);
        } else {
            context.getStack().decrement(1);
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public void onArtifactTick(FloatingArtefactEntity entity) {

        if (entity.getState() == State.INITIALISING) {
            if (findStructure(entity)) {
                entity.setState(State.RUNNING);
            }
        } else {
            if (!findStructure(entity)) {
                entity.setState(State.INITIALISING);
            }

            entity.addSpin(2, 10);

            BlockPos pos = entity.getBlockPos();
            entity.world.addParticle(ParticleTypes.COMPOSTER,
                    pos.getX() + entity.world.getRandom().nextFloat(),
                    pos.getY() + entity.world.getRandom().nextFloat(),
                    pos.getZ() + entity.world.getRandom().nextFloat(),
                    0, 0, 0);

            if (entity.world.getTime() % 80 == 0 && !entity.world.isClient) {
                List<LivingEntity> inputs = new ArrayList<>();
                List<LivingEntity> outputs = new ArrayList<>();

                VecHelper.findInRange(entity, entity.world, entity.getPos(), 20, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> !e.isRemoved() && (e instanceof PlayerEntity || e instanceof MobEntity))).forEach(e -> {
                    LivingEntity living = (LivingEntity)e;

                    if (e instanceof PlayerEntity
                            || (living instanceof TameableEntity && ((TameableEntity)living).isTamed())
                            || (living instanceof Saddleable && ((Saddleable)living).isSaddled())) {
                        if (living.getHealth() < living.getMaxHealth()) {
                            outputs.add(living);
                        }
                    } else if (e.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
                        inputs.add(living);
                    }
                });

                int demand = outputs.size();
                int supply = inputs.size();

                if (demand == 0 || supply == 0) {
                    return;
                }

                float gives;
                float takes;

                if (supply > demand) {
                    gives = supply / demand;
                    takes = 1;
                } else if (demand > supply) {
                    takes = demand / supply;
                    gives = 1;
                } else {
                    gives = 1;
                    takes = 1;
                }

                inputs.forEach(input -> {
                    input.damage(MagicalDamageSource.create("feed"), takes);
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity, 0.2F), input, 1);
                });
                outputs.forEach(output -> {
                    ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, output, 0.2F), entity, 1);
                    output.heal(gives);
                });

                entity.addSpin(gives > 0 ? 20 : 10, 30);
            }
        }

    }

    @Override
    public ActionResult onArtifactDestroyed(FloatingArtefactEntity entity) {
        entity.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.75F, 1);
        entity.dropStack(new ItemStack(UItems.CRYSTAL_SHARD, 1 + entity.world.random.nextInt(5)), 0);
        return ActionResult.SUCCESS;
    }

    private boolean findStructure(FloatingArtefactEntity entity) {
        return findPyramid(entity, Direction.UP) && findPyramid(entity, Direction.DOWN);
    }

    private boolean findPyramid(FloatingArtefactEntity entity, Direction direction) {

        BlockPos tip = entity.getBlockPos().offset(direction);
        BlockState tipState = entity.world.getBlockState(tip);
        if (!tipState.isIn(UTags.CRYSTAL_HEART_ORNAMENT) || (!tipState.contains(EndRodBlock.FACING)|| tipState.get(EndRodBlock.FACING) != direction.getOpposite())) {
            return false;
        }

        tip = tip.offset(direction);
        if (!isDiamond(entity.world.getBlockState(tip))) {
            return false;
        }
        tip = tip.offset(direction);

        final BlockPos center = tip;

        return BlockPos.streamOutwards(center, 1, 0, 1)
                .filter(p -> p.getX() == center.getX() || p.getZ() == center.getZ())
                .map(entity.world::getBlockState)
                .allMatch(this::isDiamond);
    }

    private boolean isDiamond(BlockState state) {
        return state.isIn(UTags.CRYSTAL_HEART_BASE);
    }
}
