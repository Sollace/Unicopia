package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

/**
 * Simple fire spell that triggers an effect when used on a block.
 */
public class FireSpell extends AbstractAreaEffectSpell implements ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FIRE, 15)
            .build();

    protected FireSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!projectile.isClient()) {
            Vec3d pos = hit.getPos();
            projectile.asWorld().createExplosion(projectile.getOwner(), pos.getX(), pos.getY(), pos.getZ(), 2, ExplosionSourceType.MOB);
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!projectile.isClient()) {
            Entity entity = hit.getEntity();
            projectile.asWorld().createExplosion(projectile.getOwner(), entity.getX(), entity.getY(), entity.getZ(), 2, ExplosionSourceType.MOB);
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        return new Sphere(false, Math.max(0, 4 + getTraits().get(Trait.POWER))).translate(source.getOrigin()).getBlockPositions().reduce(false,
                (r, i) -> source.canModifyAt(i) && applyBlocks(source.asWorld(), i),
                (a, b) -> a || b)
                || applyEntities(source, source.getOriginVector());
    }

    protected void generateParticles(Caster<?> source) {
        source.spawnParticles(new Sphere(false, Math.max(0, 4 + getTraits().get(Trait.POWER))), (int)(1 + source.getLevel().getScaled(8)) * 6, pos -> {
            source.addParticle(ParticleTypes.LARGE_SMOKE, pos, Vec3d.ZERO);
        });
    }

    protected boolean applyBlocks(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.isAir()) {
            if (state.isOf(Blocks.NETHERRACK)) {
                if (world.isAir(pos.up())) {

                    if (world.random.nextInt(300) == 0) {
                        world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
                    }

                    return true;
                }
            } else if (state.isOf(Blocks.REDSTONE_WIRE)) {
                int power = world.random.nextInt(5) == 3 ? 15 : 3;

                sendPower(world, pos, power, 3, 0);

                return true;
            } else if (state.isIn(BlockTags.SAND) && world.random.nextInt(10) == 0) {
                if (isSurroundedBySand(world, pos)) {
                    world.setBlockState(pos, Blocks.GLASS.getDefaultState());

                    playEffect(world, pos);
                    return true;
                }
            } else if (state.isIn(BlockTags.LEAVES)) {
                if (world.isAir(pos.up())) {
                    world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());

                    playEffect(world, pos);
                    return true;
                }
            } else if (StateMaps.FIRE_AFFECTED.convert(world, pos)) {
                playEffect(world, pos);
                return true;
            }
        }

        return false;
    }

    protected boolean applyEntities(Caster<?> source, Vec3d pos) {
        return source.findAllEntitiesInRange(Math.max(0, 3 + getTraits().get(Trait.POWER)), e -> {
            LivingEntity master = source.getMaster();
            return (!(e.equals(source.asEntity()) || e.equals(master)) ||
                    (master instanceof PlayerEntity && !EquinePredicates.PLAYER_UNICORN.test(master))) && !(e instanceof ItemEntity)
                    && !(e instanceof Caster<?>);
        }).filter(e -> {
            e.setOnFireFor(60);
            e.damage(getDamageCause(source, e), 0.1f);
            playEffect(source.asWorld(), e.getBlockPos());
            return true;
        })
        .count() > 0;
    }

    protected DamageSource getDamageCause(Caster<?> source, Entity target) {
        return source.damageOf(DamageTypes.IN_FIRE, source);
    }

    /**
     * Transmits power to a piece of redstone
     */
    private void sendPower(World w, BlockPos pos, int power, int max, int i) {
        BlockState state = w.getBlockState(pos);
        Block id = state.getBlock();

        if (i < max && id == Blocks.REDSTONE_WIRE) {
            i++;

            w.setBlockState(pos, state.with(RedstoneWireBlock.POWER, power));

            sendPower(w, pos.up(), power, max, i);
            sendPower(w, pos.down(), power, max, i);
            sendPower(w, pos.north(), power, max, i);
            sendPower(w, pos.south(), power, max, i);
            sendPower(w, pos.east(), power, max, i);
            sendPower(w, pos.west(), power, max, i);
        }
    }

    protected void playEffect(World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        world.playSound(null, pos, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.AMBIENT, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            ParticleUtils.spawnParticle(world, ParticleTypes.LARGE_SMOKE, new Vec3d(
                    x + Math.random(),
                    y + Math.random(),
                    z + Math.random()
            ), Vec3d.ZERO);
        }
    }

    public static boolean isSurroundedBySand(World w, BlockPos pos) {
        return isSand(w, pos.up()) && isSand(w, pos.down()) &&
                isSand(w, pos.north()) && isSand(w, pos.south()) &&
                isSand(w, pos.east()) && isSand(w, pos.west());
    }

    public static boolean isSand(World world, BlockPos pos) {
        Block id = world.getBlockState(pos).getBlock();
        return id == Blocks.SAND || id == Blocks.GLASS;
    }

}
