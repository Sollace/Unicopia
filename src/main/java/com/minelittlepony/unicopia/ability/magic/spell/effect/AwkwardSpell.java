package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.Registries;

public class AwkwardSpell extends AbstractSpell implements TimedSpell {

    private final Timer timer;

    protected AwkwardSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer(20);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.PROJECTILE) {
            timer.tick();

            if (timer.getTicksRemaining() <= 0) {
                return false;
            }

            setDirty();
        }

        if (source.isClient()) {
            source.spawnParticles(new Sphere(false, (1 + source.getLevel().getScaled(8)) * 8), 10, pos -> {

                List<Identifier> names = new ArrayList<>(Registries.PARTICLE_TYPE.getIds());

                int index = (int)MathHelper.nextDouble(source.asWorld().random, 0, names.size());

                Identifier id = names.get(index);
                ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);

                if (type instanceof ParticleEffect && shouldSpawnParticle(type)) {
                    source.addParticle((ParticleEffect)type, pos, Vec3d.ZERO);
                }
            });
        }

        return true;
    }

    protected boolean shouldSpawnParticle(ParticleType<?> type) {
        return type != ParticleTypes.BLOCK_MARKER
            && type != ParticleTypes.SMOKE
            && type != ParticleTypes.EXPLOSION
            && type != ParticleTypes.EXPLOSION_EMITTER
            && type != ParticleTypes.AMBIENT_ENTITY_EFFECT;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        timer.fromNBT(compound);
    }
}
