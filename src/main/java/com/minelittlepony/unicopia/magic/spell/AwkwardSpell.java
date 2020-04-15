package com.minelittlepony.unicopia.magic.spell;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.CasterUtils;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.ITossedEffect;
import com.minelittlepony.unicopia.magic.IUseable;
import com.minelittlepony.unicopia.util.shape.Sphere;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class AwkwardSpell extends AbstractSpell implements ITossedEffect, IUseable {

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
    public boolean update(ICaster<?> source) {
        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(new Sphere(false, (1 + source.getCurrentLevel()) * 8), 10, pos -> {

            List<Identifier> names = new ArrayList<>(Registry.PARTICLE_TYPE.getIds());

            int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, names.size());

            @SuppressWarnings("unchecked")
            ParticleType<ParticleEffect> type = (ParticleType<ParticleEffect>)Registry.PARTICLE_TYPE.get(names.get(index));

            if (shouldSpawnParticle(type)) {
                try {
                    source.getWorld().addParticle(type.getParametersFactory().read(type, new StringReader("0 0 0")), pos.x, pos.y, pos.z, 0, 0, 0);
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
    public void onImpact(ICaster<?> caster, BlockPos pos, BlockState state) {
        // noop
    }

    @Override
    public CastResult onUse(ItemUsageContext context, Affinity affinity) {
        return CastResult.PLACE;
    }

    @Override
    public CastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity) {

        CasterUtils.toCaster(player).ifPresent(this::toss);

        return CastResult.NONE;
    }
}
