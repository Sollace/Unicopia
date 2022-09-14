package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.ICE, 15)
            .build();

    private final int rad = 3;
    private final Shape outerRange = new Sphere(false, rad);

    protected IceSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        boolean submerged = source.getEntity().isSubmergedInWater() || source.getEntity().isSubmergedIn(FluidTags.LAVA);

        long blocksAffected = PosHelper.getAllInRegionMutable(source.getOrigin(), outerRange).filter(i -> {
            if (source.canModifyAt(i) && applyBlockSingle(source.getEntity(), source.getReferenceWorld(), i, situation)) {

                if (submerged & source.getOrigin().isWithinDistance(i, rad - 1)) {
                    BlockState state = source.getReferenceWorld().getBlockState(i);
                    if (state.isIn(BlockTags.ICE) || state.isOf(Blocks.OBSIDIAN)) {
                        source.getReferenceWorld().setBlockState(i, Blocks.AIR.getDefaultState(), Block.NOTIFY_NEIGHBORS);
                    } else if (!state.getFluidState().isEmpty()) {
                        source.getReferenceWorld().setBlockState(i, state.with(Properties.WATERLOGGED, false), Block.NOTIFY_NEIGHBORS);
                    }
                }

                ParticleUtils.spawnParticle(source.getReferenceWorld(), ParticleTypes.SPLASH, new Vec3d(
                        i.getX() + source.getReferenceWorld().random.nextFloat(),
                        i.getY() + 1,
                        i.getZ() + source.getReferenceWorld().random.nextFloat()), Vec3d.ZERO);

                return true;
            }

            return false;
        }).count();

        source.subtractEnergyCost(Math.min(10, blocksAffected));

        return applyEntities(source.getMaster(), source.getReferenceWorld(), source.getOriginVector()) && situation == Situation.PROJECTILE;
    }

    protected boolean applyEntities(LivingEntity owner, World world, Vec3d pos) {
        return !VecHelper.findInRange(owner, world, pos, 3, i -> applyEntitySingle(owner, i)).isEmpty();
    }

    protected boolean applyEntitySingle(LivingEntity owner, Entity e) {
        if (e instanceof TntEntity) {
            e.remove(RemovalReason.DISCARDED);
            e.getEntityWorld().setBlockState(e.getBlockPos(), Blocks.TNT.getDefaultState());
        } else if (e.isOnFire()) {
            e.extinguish();
        } else {
            e.damage(MagicalDamageSource.create("cold", owner), 2);
        }

        return true;
    }

    private boolean applyBlockSingle(Entity owner, World world, BlockPos pos, Situation situation) {
        BlockState state = world.getBlockState(pos);

        if ((situation == Situation.PROJECTILE && StateMaps.SNOW_PILED.convert(world, pos))
            || StateMaps.ICE_AFFECTED.convert(world, pos)) {
            return true;
        }

        if (world.isTopSolid(pos, owner)
                || (state.isOf(Blocks.SNOW) && state.get(SnowBlock.LAYERS) == SnowBlock.MAX_LAYERS)
                || state.isIn(BlockTags.LEAVES)) {
            addSnowLayer(world, pos.up());
            return true;
        }

        if (state.getMaterial() == Material.ICE
                && world.random.nextInt(10) == 0
                && isSurroundedByIce(world, pos)) {
            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
            return true;
        }

        return false;
    }

    private static boolean isSurroundedByIce(World w, BlockPos pos) {
        return PosHelper.adjacentNeighbours(pos).allMatch(i ->
            w.getBlockState(i).getMaterial() == Material.ICE
        );
    }

    private static void addSnowLayer(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isAir() || StatePredicate.isPlant(state)) {
            world.setBlockState(pos, Blocks.SNOW.getDefaultState(), 3);
        }
    }
}
