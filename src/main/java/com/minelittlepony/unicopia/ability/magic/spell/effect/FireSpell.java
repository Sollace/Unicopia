package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

/**
 * Simple fire spell that triggers an effect when used on a block.
 */
public class FireSpell extends AbstractAreaEffectSpell implements ProjectileSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FIRE, 15)
            .build();

    protected FireSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        if (!projectile.isClient()) {
            projectile.getReferenceWorld().createExplosion(projectile.getEntity(), pos.getX(), pos.getY(), pos.getZ(), 2, DestructionType.DESTROY);
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        return PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, Math.max(0, 4 + getTraits().get(Trait.POWER)))).reduce(false,
                (r, i) -> source.canModifyAt(i) && applyBlocks(source.getReferenceWorld(), i),
                (a, b) -> a || b)
                || applyEntities(null, source.getReferenceWorld(), source.getOriginVector());
    }

    protected void generateParticles(Caster<?> source) {
        source.spawnParticles(new Sphere(false, Math.max(0, 4 + getTraits().get(Trait.POWER))), (1 + source.getLevel().get()) * 6, pos -> {
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

    protected boolean applyEntities(@Nullable Entity owner, World world, Vec3d pos) {
        return !VecHelper.findInRange(owner, world, pos, Math.max(0, 3 + getTraits().get(Trait.POWER)), i -> applyEntitySingle(owner, world, i)).isEmpty();
    }

    protected boolean applyEntitySingle(@Nullable Entity owner, World world, Entity e) {
        if ((!e.equals(owner) ||
                (owner instanceof PlayerEntity && !EquinePredicates.PLAYER_UNICORN.test(owner))) && !(e instanceof ItemEntity)
        && !(e instanceof Caster<?>)) {
            e.setOnFireFor(60);
            e.damage(getDamageCause(e, (LivingEntity)owner), 0.1f);
            playEffect(world, e.getBlockPos());
            return true;
        }

        return false;
    }

    protected DamageSource getDamageCause(Entity target, @Nullable LivingEntity attacker) {
        return MagicalDamageSource.create("fire", attacker);
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
