package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.shape.*;

import net.minecraft.block.*;
import net.minecraft.fluid.*;
import net.minecraft.nbt.*;
import net.minecraft.state.property.Properties;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class HydrophobicSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .build();

    private final TagKey<Fluid> affectedFluid;

    private final Set<Entry> storedFluidPositions = new HashSet<>();

    protected HydrophobicSpell(CustomisedSpellType<?> type, TagKey<Fluid> affectedFluid) {
        super(type);
        this.affectedFluid = affectedFluid;
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        if ((method == CastingMethod.DIRECT || method == CastingMethod.STAFF) && getTraits().get(Trait.GENEROSITY) > 0) {
            return toPlaceable();
        }
        return this;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient()) {
            World world = source.asWorld();

            Shape area = new Sphere(false, getRange(source)).translate(source.getOriginVector());

            storedFluidPositions.removeIf(entry -> {
               if (!area.isPointInside(Vec3d.ofCenter(entry.pos()))) {
                   if (source.canModifyAt(entry.pos())) {
                       entry.restore(world);
                   }
                   return true;
               }

               return false;
            });

            area.getBlockPositions().forEach(pos -> {
                pos = new BlockPos(pos);
                BlockState state = world.getBlockState(pos);

                if (source.canModifyAt(pos) && state.getFluidState().isIn(affectedFluid)) {
                    Block block = state.getBlock();

                    if (block instanceof FluidBlock) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                        storedFluidPositions.add(new Entry(pos, state));
                    } else if (state.contains(Properties.WATERLOGGED)) {
                        world.setBlockState(pos, state.cycle(Properties.WATERLOGGED), Block.NOTIFY_LISTENERS);
                        storedFluidPositions.add(new Entry(pos, state));
                    }
                }
            });

            if (!source.subtractEnergyCost(storedFluidPositions.isEmpty() ? 0.001F : 0.02F)) {
                setDead();
            }

            double range = getRange(source);
            Ether.get(source.asWorld()).getOrCreate(this, source).setRadius((float)range);

            source.spawnParticles(new Sphere(true, range), 10, pos -> {
                BlockPos bp = BlockPos.ofFloored(pos);
                if (source.canModifyAt(bp) && source.asWorld().getFluidState(bp.up()).isIn(affectedFluid)) {
                    source.addParticle(UParticles.RAIN_DROPS, pos, Vec3d.ZERO);
                }
            });

            if (source.asEntity().age % 200 == 0) {
                source.playSound(USounds.SPELL_AMBIENT, 0.5F);
            }

            if (storedFluidPositions.size() >= 100) {
                UCriteria.SPLIT_SEA.trigger(source.asEntity());
            }
        }

        return !isDead();
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        storedFluidPositions.removeIf(entry -> {
            if (caster.canModifyAt(entry.pos())) {
                entry.restore(caster.asWorld());
            }
            return true;
         });
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("storedFluidPositions", Entry.SERIALIZER.writeAll(storedFluidPositions));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        storedFluidPositions.clear();
        storedFluidPositions.addAll(Entry.SERIALIZER.readAll(compound.getList("storedFluidPositions", NbtElement.COMPOUND_TYPE)).toList());
    }
    /**
     * Calculates the maximum radius of the shield. aka The area of effect.
     */
    public double getRange(Caster<?> source) {
        float multiplier = 1;
        float min = (source instanceof Pony ? 4 : 6) + getTraits().get(Trait.POWER);
        boolean isLimitedRange = source instanceof Pony || source instanceof MagicProjectileEntity;
        double range = (min + (source.getLevel().getScaled(isLimitedRange ? 4 : 40) * (isLimitedRange ? 2 : 10))) / multiplier;
        return range;
    }

    record Entry (BlockPos pos, BlockState blockState) {
        public static final Serializer<Entry> SERIALIZER = Serializer.of(compound -> new Entry(
            NbtSerialisable.BLOCK_POS.read(compound.getCompound("pos")),
            NbtSerialisable.decode(BlockState.CODEC, compound.get("blockState")).orElse(Blocks.AIR.getDefaultState())
        ), entry -> {
            NbtCompound compound = new NbtCompound();
            compound.put("pos", NbtSerialisable.BLOCK_POS.write(entry.pos));
            compound.put("blockState", NbtSerialisable.encode(BlockState.CODEC, entry.blockState));
            return compound;
        });

        void restore(World world) {
            BlockState state = world.getBlockState(pos);

            if (state.isAir()) {
                if (blockState.contains(Properties.WATERLOGGED)) {
                    world.setBlockState(pos, blockState.getFluidState().getBlockState(), Block.NOTIFY_LISTENERS);
                } else {
                    world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
                }
            } else if (state.contains(Properties.WATERLOGGED)) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), Block.NOTIFY_LISTENERS);
            }
        }
    }

    public boolean blocksFlow(Ether.Entry<?> entry, Vec3d center, BlockPos pos, FluidState fluid) {
        return fluid.isIn(affectedFluid) && pos.isWithinDistance(center, (double)entry.getRadius() + 1);
    }

    public static boolean blocksFluidFlow(BlockView world, BlockPos pos, FluidState state) {
        if (!(world instanceof ServerWorld sw)) {
            return false;
        }

        return Ether.get(sw).anyMatch(SpellType.HYDROPHOBIC, entry -> {
            var target = entry.entity.getTarget().orElse(null);
            if (target == null || !pos.isWithinDistance(target.pos(), entry.getRadius() + 1)) {
                return false;
            }
            var spell = entry.getSpell();
            return spell != null && target != null && spell.blocksFlow(entry, target.pos(), pos, state);
        });
    }
}
