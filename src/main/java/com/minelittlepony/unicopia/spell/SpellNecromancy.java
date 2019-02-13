package com.minelittlepony.unicopia.spell;

import java.util.List;

import com.google.common.collect.Lists;
import com.minelittlepony.util.WorldEvent;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class SpellNecromancy extends AbstractSpell.RangedAreaSpell {

    private final List<IMonsterSpawn<?>> spawns = Lists.newArrayList(
            EntityZombie::new,
            EntityHusk::new,
            EntityPigZombie::new
    );

    @Override
    public String getName() {
        return "necromancy";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    @Override
    public int getTint() {
        return 0x3A3A3A;
    }

    @Override
    public boolean update(ICaster<?> source) {

        if (source.getWorld().isRemote || source.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL) {
            return true;
        }


        float additional = source.getWorld().getDifficultyForLocation(source.getOrigin()).getAdditionalDifficulty();

        int radius = source.getCurrentLevel() + 1;

        IShape affectRegion = new Sphere(false, radius * 4);

        if (source.getWorld().rand.nextInt(100) != 0) {
            return true;
        }

        Vec3d origin = source.getOriginVector();

        if (VecHelper.findAllEntitiesInRange(source.getEntity(), source.getWorld(), source.getOrigin(), radius * 4)
                .filter(e -> e instanceof EntityZombie)
                .count() >= 10 * (1 + additional)) {
            return true;
        }

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getWorld().rand).add(origin);

            BlockPos loc = new BlockPos(pos);

            if (source.getWorld().isAirBlock(loc.up()) && !source.getWorld().isAirBlock(loc)) {
                spawnMonster(source, pos);

                return true;
            }
        }


        return true;
    }

    protected void spawnMonster(ICaster<?> source, Vec3d pos) {
        int index = (int)MathHelper.nextDouble(source.getWorld().rand, 0, spawns.size());
        EntityLivingBase zombie = spawns.get(index).create(source.getWorld());
        zombie.setPosition(pos.x, pos.y, pos.z);


        if (zombie instanceof EntityCreature) {
            ((EntityCreature)zombie).setHomePosAndDistance(source.getOrigin(), 10);
        }
        zombie.motionY += 0.3F;

        WorldEvent.DOOR_BROKEN.play(source.getWorld(), zombie.getPosition());

        source.getWorld().spawnEntity(zombie);
    }

    @Override
    public void render(ICaster<?> source) {
        IShape affectRegion = new Sphere(false, (1 + source.getCurrentLevel()) * 4);

        source.spawnParticles(affectRegion, 5, pos -> {
            if (!source.getWorld().isAirBlock(new BlockPos(pos).down())) {
                source.getWorld().spawnParticle(EnumParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        });
    }

    interface IMonsterSpawn<T extends EntityLivingBase> {
        T create(World w);
    }
}
