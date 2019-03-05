package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellChangelingTrap extends AbstractSpell implements ITossedEffect {

    @Override
    public String getName() {
        return "changeling_trap";
    }

    @Override
    public int getTint() {
        return 0x88FF88;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public boolean update(ICaster<?> source) {
        Entity entity = source.getEntity();

        if (!(entity instanceof EntityProjectile)) {
            entity.motionX /= 3;
            entity.motionY /= 3;
            entity.motionZ /= 3;
        }

        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(EnumParticleTypes.DRIP_LAVA.getParticleID(), 3);
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    public void enforce() {

        setDirty(true);
    }

    protected void entrap(IPlayer e) {

        SpellChangelingTrap existing = e.getEffect(SpellChangelingTrap.class, true);

        if (existing == null) {
            e.setEffect(copy());
        } else {
            existing.enforce();
        }
    }

    @Override
    public void onImpact(World world, BlockPos pos, IBlockState state) {
        VecHelper.findAllEntitiesInRange(null, world, pos, 5)
            .filter(e -> e instanceof EntityPlayer)
            .map(e -> PlayerSpeciesList.instance().getPlayer((EntityPlayer)e))
            .forEach(this::entrap);
    }
}
