package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.ThrowableSpell;
import com.minelittlepony.unicopia.util.shape.Sphere;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class AwkwardSpell extends AbstractSpell implements ThrowableSpell {

    @Override
    public String getName() {
        return "awkward";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public int getTint() {
        return 0xE1239C;
    }

    @Override
    public boolean update(Caster<?> source) {
        return true;
    }

    @Override
    public void render(Caster<?> source) {
        source.spawnParticles(new Sphere(false, (1 + source.getCurrentLevel()) * 8), 10, pos -> {

            List<Identifier> names = new ArrayList<>(Registry.PARTICLE_TYPE.getIds());

            int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, names.size());

            @SuppressWarnings("unchecked")
            ParticleType<ParticleEffect> type = (ParticleType<ParticleEffect>)Registry.PARTICLE_TYPE.get(names.get(index));

            if (shouldSpawnParticle(type)) {
                try {
                    source.addParticle(type.getParametersFactory().read(type, new StringReader("0 0 0")), pos, Vec3d.ZERO);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected boolean shouldSpawnParticle(ParticleType<?> type) {
        return type != ParticleTypes.BARRIER
            && type != ParticleTypes.SMOKE
            && type != ParticleTypes.EXPLOSION
            && type != ParticleTypes.EXPLOSION_EMITTER
            && type != ParticleTypes.AMBIENT_ENTITY_EFFECT;
    }

    @Override
    public void onImpact(Caster<?> caster, BlockPos pos, BlockState state) {
        // noop
    }

}
