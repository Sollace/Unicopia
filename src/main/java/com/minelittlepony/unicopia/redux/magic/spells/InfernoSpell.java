package com.minelittlepony.unicopia.redux.magic.spells;

import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.CastResult;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.spell.FireSpell;
import com.minelittlepony.unicopia.core.util.MagicalDamageSource;
import com.minelittlepony.unicopia.core.util.collection.IStateMapping;
import com.minelittlepony.unicopia.core.util.collection.StateMapList;
import com.minelittlepony.unicopia.core.util.shape.IShape;
import com.minelittlepony.unicopia.core.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.OreBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InfernoSpell extends FireSpell {

    public final StateMapList hellFireAffected = new StateMapList();

    public InfernoSpell() {
        hellFireAffected.add(IStateMapping.build(
                s -> s.getBlock() == Blocks.GRASS || s.getBlock() == Blocks.DIRT || s.getBlock() == Blocks.STONE,
                s -> Blocks.NETHERRACK.getDefaultState()));

        hellFireAffected.replaceBlock(Blocks.SAND, Blocks.SOUL_SAND);
        hellFireAffected.replaceBlock(Blocks.GRAVEL, Blocks.SOUL_SAND);

        hellFireAffected.add(IStateMapping.build(
                s -> s.getMaterial() == Material.WATER,
                s -> Blocks.OBSIDIAN.getDefaultState()));

        hellFireAffected.add(IStateMapping.build(
                s -> s.getBlock() instanceof PlantBlock,
                s -> Blocks.NETHER_WART.getDefaultState()));

        hellFireAffected.add(IStateMapping.build(
                s -> (s.getBlock() != Blocks.NETHER_QUARTZ_ORE) && (s.getBlock() instanceof OreBlock),
                s -> Blocks.NETHER_QUARTZ_ORE.getDefaultState()));
    }

    @Override
    public String getName() {
        return "inferno";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    public int getTint() {
        return 0xF00F00;
    }

    @Override
    public CastResult onUse(ItemUsageContext context, Affinity affinity) {
        return CastResult.PLACE;
    }

    @Override
    public boolean update(ICaster<?> source) {
        World w = source.getWorld();

        if (!w.isClient) {
            int radius = 4 + (source.getCurrentLevel() * 4);
            IShape shape = new Sphere(false, radius);

            Vec3d origin = source.getOriginVector();

            for (int i = 0; i < radius; i++) {
                BlockPos pos = new BlockPos(shape.computePoint(w.random).add(origin));

                BlockState state = w.getBlockState(pos);
                BlockState newState = hellFireAffected.getConverted(state);

                if (!state.equals(newState)) {
                    w.setBlockState(pos, newState, 3);

                    playEffect(w, pos);
                }
            }

            shape = new Sphere(false, radius - 1);
            for (int i = 0; i < radius * 2; i++) {
                if (w.random.nextInt(12) == 0) {
                    BlockPos pos = new BlockPos(shape.computePoint(w.random).add(origin));

                    if (!applyBlocks(w, pos)) {
                        applyEntities(source.getOwner(), w, pos);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected DamageSource getDamageCause(Entity target, LivingEntity attacker) {
        if (attacker != null && attacker.getUuid().equals(target.getUuid())) {
            return MagicalDamageSource.causeMobDamage("fire.own", null);
        }
        return MagicalDamageSource.causeMobDamage("fire", attacker);
    }
}
