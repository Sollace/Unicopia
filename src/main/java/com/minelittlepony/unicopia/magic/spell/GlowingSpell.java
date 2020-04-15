package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.GenericSpell;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IHeldEffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;

public class GlowingSpell extends GenericSpell implements IHeldEffect {

    private BlockPos lastPos;
    private ICaster<?> source;

    public GlowingSpell() {
        super("light", 0xF7FACB, Affinity.GOOD);
    }

    @Override
    public void onPlaced(ICaster<?> caster) {
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

            provider.queueLightCheck(lastPos);
        }
    }

    @Override
    public void updateInHand(IPlayer caster, Affinity affinity) {
        if (caster.getSpecies().canCast()) {
            update(caster);
        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        this.source = source;

        BlockPos pos = source.getOrigin().down();

        World world = source.getWorld();

        ChunkLightingView view = world.getChunkManager().getLightingProvider().get(LightType.BLOCK);

        if (!(view instanceof ChunkBlockLightProvider)) {
            return true;
        }


        ChunkBlockLightProvider provider = (ChunkBlockLightProvider)view;

        if (lastPos != null && !lastPos.equals(pos)) {
            provider.queueLightCheck(lastPos);
        }

        lastPos = pos;

        int light = provider.getLightLevel(pos);

        if (light < 8) {
            provider.method_15514(pos, 8);
            provider.queueLightCheck(pos);
        }

        return true;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (compound.containsKey("lastX")) {
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
