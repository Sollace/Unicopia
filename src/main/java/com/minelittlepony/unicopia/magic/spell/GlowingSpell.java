package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.HeldSpell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;

public class GlowingSpell extends GenericSpell implements HeldSpell {

    private BlockPos lastPos;
    private Caster<?> source;

    public GlowingSpell() {
        super("light", 0xF7FACB, Affinity.GOOD);
    }

    @Override
    public void onPlaced(Caster<?> caster) {
        this.source = caster;
    }

    @Override
    public void setDead() {
        super.setDead();

        if (lastPos != null) {
            ChunkLightingView view = source.getWorld().getChunkManager().getLightingProvider().get(LightType.BLOCK);

            if (!(view instanceof ChunkBlockLightProvider)) {
                return;
            }

            ChunkBlockLightProvider provider = (ChunkBlockLightProvider)view;

            provider.checkBlock(lastPos);
        }
    }

    @Override
    public void updateInHand(Pony caster, Affinity affinity) {
        if (caster.getSpecies().canCast()) {
            update(caster);
        }
    }

    @Override
    public boolean update(Caster<?> source) {
        this.source = source;

        BlockPos pos = source.getOrigin().down();

        World world = source.getWorld();

        ChunkLightingView view = world.getChunkManager().getLightingProvider().get(LightType.BLOCK);

        if (!(view instanceof ChunkBlockLightProvider)) {
            return true;
        }


        ChunkBlockLightProvider provider = (ChunkBlockLightProvider)view;

        if (lastPos != null && !lastPos.equals(pos)) {
            provider.checkBlock(lastPos);
        }

        lastPos = pos;

        int light = provider.getLightLevel(pos);

        if (light < 8) {
            provider.addLightSource(pos, 8);
            provider.checkBlock(pos);
        }

        return true;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (compound.contains("lastX")) {
            lastPos = new BlockPos(compound.getInt("lastX"), compound.getInt("lastY"), compound.getInt("lastZ"));
        } else {
            lastPos = null;
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (lastPos != null) {
            compound.putInt("lastX", lastPos.getX());
            compound.putInt("lastY", lastPos.getY());
            compound.putInt("lastZ", lastPos.getZ());
        }
    }
}
