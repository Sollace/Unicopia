package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public class ChitinBlock extends Block {

    public static final EnumProperty<Covering> COVERING = EnumProperty.of("covering", Covering.class);

    public ChitinBlock() {
        super(FabricBlockSettings.of(UMaterials.HIVE)
                .hardness(50)
                .strength(2000, 2000)
                .materialColor(MaterialColor.BLACK)
                .build()
        );
        setDefaultState(stateManager.getDefaultState().with(COVERING, Covering.UNCOVERED));

        // TODO: drops:
        //  UItems.chitin_shell x 3
        // setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float hardness = super.calcBlockBreakingDelta(state, player, world, pos);

        Pony iplayer = Pony.of(player);
        Race race = iplayer.getSpecies();

        if (race == Race.CHANGELING) {
            hardness *= 80;
        } else if (race.canInteractWithClouds()) {
            hardness /= 4;
        } else if (race.canUseEarth()) {
            hardness *= 10;
        }

        return hardness;
    }


    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {
        if (direction == Direction.UP) {
            Block block = other.getBlock();

            boolean snowy = block == Blocks.SNOW_BLOCK || block == Blocks.SNOW;
            boolean solid = (other.isOpaque() && other.isSimpleFullBlock(world, pos)) || Block.isFaceFullSquare(other.getCollisionShape(world, otherPos, EntityContext.absent()), Direction.DOWN);

            return state.with(COVERING, snowy ? Covering.SNOW_COVERED : solid ? Covering.COVERED : Covering.UNCOVERED);
        }

        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COVERING);
    }

    public enum Covering implements StringIdentifiable {
        COVERED,
        UNCOVERED,
        SNOW_COVERED;

        @Override
        public String toString() {
            return asString();
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }

    }
}
