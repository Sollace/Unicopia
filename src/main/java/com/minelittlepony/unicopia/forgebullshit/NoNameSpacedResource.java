package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.util.ResourceLocation;

public class NoNameSpacedResource extends ResourceLocation {

    public static ResourceLocation[] ofAll(String...strings) {
        ResourceLocation[] resources = new ResourceLocation[strings.length];

        for (int i = 0; i < strings.length; i++) {
            resources[i] = new NoNameSpacedResource(strings[i]);
        }

        return resources;
    }

    public static ResourceLocation[] ofAllDomained(String domain, String...strings) {
        ResourceLocation[] resources = new ResourceLocation[strings.length];

        for (int i = 0; i < strings.length; i++) {
            resources[i] = new ResourceLocation(domain, strings[i]);
        }

        return resources;
    }

    public NoNameSpacedResource(String path) {
        super(path);
    }

    @Override
    public String toString() {
        return getPath();
    }
}
