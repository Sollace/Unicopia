package com.minelittlepony.unicopia.spell;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SpellAwkward extends AbstractSpell implements ITossedEffect, IUseAction {

    private final List<String> names = Lists.newArrayList(EnumParticleTypes.getParticleNames());

    private final int max = names.size();

    @Override
    public String getName() {
        return "awkward";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
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
            int index = (int)MathHelper.nextDouble(source.getWorld().rand, 0, max);

            EnumParticleTypes type = EnumParticleTypes.getByName(names.get(index));

            if (shouldSpawnParticle(type)) {
                int[] arguments = new int[type.getArgumentCount()];

                source.getWorld().spawnParticle(type, pos.x, pos.y, pos.z, 0, 0, 0, arguments);
            }
        });
    }

    protected boolean shouldSpawnParticle(EnumParticleTypes type) {
        switch (type) {
            case BARRIER:
            case SMOKE_LARGE:
            case MOB_APPEARANCE:
            case EXPLOSION_HUGE:
            case EXPLOSION_LARGE:
            case EXPLOSION_NORMAL: return false;
            default: return true;
        }
    }

    @Override
    public void onImpact(World world, BlockPos pos, IBlockState state) {
        // noop
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        return SpellCastResult.PLACE;
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, @Nullable Entity hitEntity) {

        CasterUtils.toCaster(player).ifPresent(this::toss);

        return SpellCastResult.NONE;
    }
}
