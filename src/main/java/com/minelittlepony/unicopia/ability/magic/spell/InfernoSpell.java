package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.block.state.BlockStateConverter;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InfernoSpell extends FireSpell {

    protected InfernoSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onThrownTick(Caster<?> source) {
        if (source.isClient()) {
            generateParticles(source);
        }

        World w = source.getWorld();

        if (!w.isClient) {
            int radius = 4 + (source.getLevel().get() * 4);
            Shape shape = new Sphere(false, radius);

            Vec3d origin = source.getOriginVector();

            BlockStateConverter converter = w.getDimension().isUltrawarm() ? StateMaps.HELLFIRE_AFFECTED.getInverse() : StateMaps.HELLFIRE_AFFECTED;

            for (int i = 0; i < radius; i++) {
                BlockPos pos = new BlockPos(shape.computePoint(w.random).add(origin));

                BlockState state = w.getBlockState(pos);
                BlockState newState = converter.getConverted(w, state);

                if (!state.equals(newState)) {

                    if (newState.getBlock() instanceof DoorBlock) {
                        boolean lower = newState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                        BlockPos other = lower ? pos.up() : pos.down();

                        w.setBlockState(other, newState.with(DoorBlock.HALF, lower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER), 16 | 2);
                    }

                    w.setBlockState(pos, newState, 16 | 2);

                    playEffect(w, pos);
                }
            }

            shape = new Sphere(false, radius - 1);
            for (int i = 0; i < radius * 2; i++) {
                if (w.random.nextInt(12) == 0) {
                    Vec3d vec = shape.computePoint(w.random).add(origin);

                    if (!applyBlocks(w, new BlockPos(vec))) {
                        applyEntities(source.getMaster(), w, vec);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected DamageSource getDamageCause(Entity target, LivingEntity attacker) {
        if (attacker != null && attacker.getUuid().equals(target.getUuid())) {
            return MagicalDamageSource.create("fire.own", null);
        }
        return MagicalDamageSource.create("fire", attacker);
    }
}
