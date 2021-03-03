package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractPlacedSpell extends AbstractSpell implements Attached {

    @Nullable
    protected Vec3d origin;
    @Nullable
    protected BlockPos placement;
    @Nullable
    private Identifier dimension;

    private final ParticleHandle particlEffect = new ParticleHandle();

    protected AbstractPlacedSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {

        if (origin == null) {
            origin = source.getOriginVector();
            placement = source.getOrigin();
            dimension = source.getWorld().getRegistryKey().getValue();
        }

        if (!source.getWorld().getRegistryKey().getValue().equals(dimension)) {
            return false;
        }

        if (source.isClient()) {
            particlEffect.ifAbsent(source, spawner -> {
                spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.MAGIC_RUNES, 90, 0), origin, Vec3d.ZERO);
            }).ifPresent(p -> {
                p.attach(source);
                p.setAttribute(1, getType().getColor());
            });
        }

        return onGroundTick(source);
    }

    protected abstract boolean onGroundTick(Caster<?> source);

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        if (placement != null) {
            compound.put("placement", NbtHelper.fromBlockPos(placement));
        }
        if (origin != null) {
            compound.put("origin", NbtSerialisable.writeVector(origin));
        }
        if (dimension != null) {
            compound.putString("dimension", dimension.toString());
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        if (compound.contains("placement")) {
            placement = NbtHelper.toBlockPos(compound.getCompound("placement"));
        }
        if (compound.contains("origin")) {
            origin = NbtSerialisable.readVector(compound.getList("origin", 6));
        }
        if (compound.contains("dimension")) {
            dimension = new Identifier(compound.getString("dimension"));
        }
    }
}
