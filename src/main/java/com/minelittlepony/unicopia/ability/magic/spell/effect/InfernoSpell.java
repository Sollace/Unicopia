package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.block.state.BlockStateConverter;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Converts surrounding blocks and entities into their nether equivalent
 */
public class InfernoSpell extends FireSpell {

    protected InfernoSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            generateParticles(source);
        }

        World w = source.asWorld();

        if (!w.isClient) {
            float radius = 4 + (source.getLevel().getScaled(4) * 4);
            Shape shape = new Sphere(false, radius);

            Vec3d origin = source.getOriginVector();

            BlockStateConverter converter = w.getDimension().ultrawarm() ? StateMaps.HELLFIRE_AFFECTED.getInverse() : StateMaps.HELLFIRE_AFFECTED;

            for (int i = 0; i < radius; i++) {
                BlockPos pos = new BlockPos(shape.computePoint(w.random).add(origin));

                if (source.canModifyAt(pos) && converter.convert(w, pos)) {
                    playEffect(w, pos);
                }
            }

            shape = new Sphere(false, radius - 1);
            for (int i = 0; i < radius * 2; i++) {
                if (w.random.nextInt(12) == 0) {
                    Vec3d vec = shape.computePoint(w.random).add(origin);

                    if (!applyBlocks(w, new BlockPos(vec))) {
                        applyEntities(source, vec);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected DamageSource getDamageCause(Entity target, LivingEntity attacker) {
        if (attacker != null && attacker.getUuid().equals(target.getUuid())) {
            return MagicalDamageSource.create("fire.own");
        }
        return MagicalDamageSource.create("fire", attacker);
    }
}
