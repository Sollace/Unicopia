package com.minelittlepony.unicopia.entity.behaviour;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FallingBlockBehaviour extends EntityBehaviour<FallingBlockEntity> {

    private static final Vec3d UP = Vec3d.of(Direction.UP.getVector());

    @Override
    public FallingBlockEntity onCreate(FallingBlockEntity entity, Disguise context) {
        super.onCreate(entity, context);

        BlockState state = entity.getBlockState();
        Block block = state.getBlock();
        if (block == Blocks.SAND) {
            block = Blocks.BLACKSTONE;
        }

        if (state.isIn(BlockTags.DOORS) && block instanceof DoorBlock) {
            BlockState lowerState = state.with(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            BlockState upperState = state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER);

            context.attachExtraEntity(new FallingBlockEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), upperState));

            return new FallingBlockEntity(entity.world, entity.getX(), entity.getY() + 1, entity.getZ(), lowerState);
        }

        if (block instanceof BlockEntityProvider) {
            BlockEntity b = ((BlockEntityProvider)block).createBlockEntity(entity.world);
            ((MixinBlockEntity)b).setCachedState(state);
            context.addBlockEntity(b);
        }

        return entity;
    }

    @Override
    public void update(Caster<?> source, FallingBlockEntity entity, Spell spell) {

        BlockState state = entity.getBlockState();
        if (state.contains(Properties.WATERLOGGED)) {
            boolean logged = entity.world.isWater(entity.getBlockPos());

            if (state.get(Properties.WATERLOGGED) != logged) {
                entity = new FallingBlockEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), state.with(Properties.WATERLOGGED, logged));
                ((DisguiseSpell)spell).getDisguise().setAppearance(entity);
                return;
            }
        }

        Disguise disguise = ((DisguiseSpell)spell).getDisguise();
        List<Entity> attachments = disguise.getAttachments();
        if (attachments.size() > 0) {
            copyBaseAttributes(source.getOwner(), attachments.get(0), UP);
        }

        BlockEntity be = disguise.getBlockEntity();

        if (source instanceof Pony && be instanceof Tickable && (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity)) {
            Pony player = (Pony)source;
            Tickable ceb = (Tickable)disguise.getBlockEntity();

            if (player.sneakingChanged()) {
                be.onSyncedBlockEvent(1, isSneakingOnGround(source) ? 1 : 0);
            }

            be.setLocation(entity.world, entity.getBlockPos());
            ceb.tick();
            be.setLocation(null, entity.getBlockPos());
        }
    }
}
