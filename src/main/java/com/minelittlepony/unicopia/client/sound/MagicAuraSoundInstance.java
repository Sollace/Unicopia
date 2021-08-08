package com.minelittlepony.unicopia.client.sound;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.enchantment.SimpleEnchantment;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class MagicAuraSoundInstance extends FadeOutSoundInstance {

    private final Living<?> living;

    public MagicAuraSoundInstance(SoundCategory category, Living<?> living) {
        super(USounds.ITEM_MAGIC_AURA, category, 1);
        this.living = living;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        Optional<SimpleEnchantment.Data> data = living.getEnchants().getOrEmpty(UEnchantments.GEM_FINDER);

        Vec3d pos = living.getOriginVector();
        x = pos.x;
        y = pos.y;
        z = pos.z;

        if (!living.getEntity().isRemoved() && data.isPresent()) {
            float level = data.get().level;
            if (level != targetVolume) {
                setTargetVolume(level);
            }
            return true;
        }

        return false;
    }
}
