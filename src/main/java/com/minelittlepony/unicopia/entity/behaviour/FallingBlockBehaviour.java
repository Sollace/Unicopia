package com.minelittlepony.unicopia.entity.behaviour;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.tag.BlockTags;
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
            context.addBlockEntity(((BlockEntityProvider)block).createBlockEntity(entity.world));
        }

        return entity;
    }

    @Override
    public void update(Caster<?> source, FallingBlockEntity entity, Spell spell) {
        List<Entity> attachments = ((DisguiseSpell)spell).getDisguise().getAttachments();
        if (attachments.size() > 0) {
            copyBaseAttributes(source.getOwner(), attachments.get(0), UP);
        }
    }
}
