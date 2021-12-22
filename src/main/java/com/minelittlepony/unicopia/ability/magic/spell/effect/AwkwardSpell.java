package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class AwkwardSpell extends AbstractSpell {

    protected AwkwardSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (source.isClient()) {
            source.spawnParticles(new Sphere(false, (1 + source.getLevel().get()) * 8), 10, pos -> {

                List<Identifier> names = new ArrayList<>(Registry.PARTICLE_TYPE.getIds());

                int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, names.size());

                Identifier id = names.get(index);
                ParticleType<?> type = Registry.PARTICLE_TYPE.get(id);

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
}
