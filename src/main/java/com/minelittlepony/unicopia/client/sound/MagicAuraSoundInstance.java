package com.minelittlepony.unicopia.client.sound;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class MagicAuraSoundInstance extends FadeOutSoundInstance {

    private final Living<?> living;

    public MagicAuraSoundInstance(SoundCategory category, Living<?> living, Random random) {
        super(USounds.ITEM_MAGIC_AURA, category, 1, random);
        this.relative = false;
        this.living = living;

        Vec3d pos = living.getOriginVector();
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        var data = living.getEnchants().getOrEmpty(UEnchantments.GEM_FINDER);

        Vec3d pos = living.getOriginVector();
        x = pos.x;
        y = pos.y;
        z = pos.z;

        if (!living.asEntity().isRemoved() && data.isPresent()) {
            float level = data.get().level;
            if (level != targetVolume) {
                setTargetVolume(level);
            }
            return true;
        }

        return false;
    }
}
