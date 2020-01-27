package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UPot extends FlowerPotBlock {

    public UPot() {
        setHardness(0);
        setSoundType(SoundType.STONE);
    }


    enum Plant {
        EMPTY("minecraft:air", -1),
        QUILL("minecraft:feather", -1),
        MEADOWBROOK("unicopia:staff_meadow_brook", -1);

        public final Identifier item;

        Plant(String item, int damage) {
            this.item = new Identifier(item);
        }

        public boolean isEmpty() {
            return this == EMPTY;
        }

    }
}
