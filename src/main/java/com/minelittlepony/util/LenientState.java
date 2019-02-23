package com.minelittlepony.util;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class LenientState extends BlockStateContainer {

    public LenientState(Block blockIn, IProperty<?>... properties) {
        super(blockIn, properties);
    }

    @Override
    protected StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        return new Impl(block, properties);
    }

    class Impl extends StateImplementation {
        protected Impl(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn) {
            super(blockIn, propertiesIn);
        }

        @Override
        public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
            if (!getProperties().containsKey(property)) {
                return this;
            }

            return super.withProperty(property, value);
        }
    }
}
