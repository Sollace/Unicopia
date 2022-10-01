package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.mixin.MixinFallingBlockEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.Trace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Picks up and throws an entity or block.
 */
public class CatapultSpell extends AbstractSpell implements ProjectileSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.EARTH, 60)
            .with(Trait.STRENGTH, 50)
            .build();

    private static final float HORIZONTAL_VARIANCE = 0.25F;
    private static final float MAX_STRENGTH = 120;

    protected CatapultSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        if (!projectile.isClient() && projectile.canModifyAt(pos)) {
            createBlockEntity(projectile.world, pos, e -> apply(projectile, e));
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        if (!projectile.isClient()) {
            apply(projectile, entity);
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return true;
        }

        getTarget(caster, e -> apply(caster, e));
        return false;
    }

    protected void apply(Caster<?> caster, Entity e) {
        Vec3d vel = caster.getEntity().getVelocity();
        if (Math.abs(e.getVelocity().y) > 0.5) {
            e.setVelocity(caster.getEntity().getVelocity());
        } else {
            e.addVelocity(
                ((caster.getReferenceWorld().random.nextFloat() * HORIZONTAL_VARIANCE) - HORIZONTAL_VARIANCE + vel.x * 0.8F) * 0.1F,
                0.1F + (getTraits().get(Trait.STRENGTH, -MAX_STRENGTH, MAX_STRENGTH) - 40) / 16D,
                ((caster.getReferenceWorld().random.nextFloat() * HORIZONTAL_VARIANCE) - HORIZONTAL_VARIANCE + vel.z * 0.8F) * 0.1F
            );
        }
    }

    protected void getTarget(Caster<?> caster, Consumer<Entity> apply) {
        if (caster.isClient()) {
            return;
        }

        double maxDistance = 2 + (getTraits().get(Trait.FOCUS) - 50) * 8;

        Trace trace = Trace.create(caster.getEntity(), maxDistance, 1, EntityPredicates.EXCEPT_SPECTATOR);
        trace.getEntity().ifPresentOrElse(apply, () -> {
            trace.ifBlock(pos -> {
                if (caster.canModifyAt(pos)) {
                    createBlockEntity(caster.getReferenceWorld(), pos, apply);
                }
            });
        });
    }

    static void createBlockEntity(World world, BlockPos bpos, @Nullable Consumer<Entity> apply) {

        if (world.isAir(bpos)) {
            return;
        }

        Vec3d pos = Vec3d.ofBottomCenter(bpos);
        FallingBlockEntity e = MixinFallingBlockEntity.createInstance(world, pos.x, pos.y, pos.z, world.getBlockState(bpos));
        world.removeBlock(bpos, true);
        e.setOnGround(false);
        e.timeFalling = Integer.MIN_VALUE;
        e.setHurtEntities(1 + (world.random.nextFloat() * 10), 100);

        if (apply != null) {
            apply.accept(e);
        }
        world.spawnEntity(e);

        e.updateVelocity(HORIZONTAL_VARIANCE, pos);
    }
}
