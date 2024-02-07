package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.pony.IPonyData;
import com.minelittlepony.api.pony.TriggerPixelType;
import com.minelittlepony.api.pony.meta.Gender;
import com.minelittlepony.api.pony.meta.Race;
import com.minelittlepony.api.pony.meta.Size;
import com.minelittlepony.api.pony.meta.TailLength;
import com.minelittlepony.api.pony.meta.TailShape;
import com.minelittlepony.api.pony.meta.Wearable;
import com.minelittlepony.common.util.animation.Interpolator;

public class UnicopiaPonyData implements IPonyData {

    Race race;
    IPonyData original;

    public UnicopiaPonyData(Race race) {
        this.race = race;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public TailLength getTailLength() {
        return original.getTailLength();
    }

    @Override
    public TailShape getTailShape() {
        return original.getTailShape();
    }

    @Override
    public Gender getGender() {
        return original.getGender();
    }

    @Override
    public Size getSize() {
        return original.getSize();
    }

    @Override
    public int getGlowColor() {
        return original.getGlowColor();
    }

    @Override
    public Wearable[] getGear() {
        return original.getGear();
    }

    @Override
    public boolean isWearing(Wearable wearable) {
        return original.isWearing(wearable);
    }

    @Override
    public Interpolator getInterpolator(UUID interpolatorId) {
        return original.getInterpolator(interpolatorId);
    }

    @Override
    public Map<String, TriggerPixelType<?>> getTriggerPixels() {
        return original.getTriggerPixels();
    }

}
