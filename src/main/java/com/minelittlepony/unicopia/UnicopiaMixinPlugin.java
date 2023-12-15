package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class UnicopiaMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "com.minelittlepony.unicopia.mixin";

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MIXIN_PACKAGE)) {
            if (mixinClassName.indexOf("sodium") != -1) {
                return FabricLoader.getInstance().isModLoaded("sodium");
            }
            if (mixinClassName.indexOf("trinkets") != -1) {
                return FabricLoader.getInstance().isModLoaded("trinkets");
            }
            if (mixinClassName.indexOf("seasons") != -1) {
                return FabricLoader.getInstance().isModLoaded("seasons");
            }
            if (mixinClassName.indexOf("ad_astra") != -1) {
                return FabricLoader.getInstance().isModLoaded("ad_astra");
            }
            if (mixinClassName.indexOf("minelp") != -1) {
                return FabricLoader.getInstance().isModLoaded("minelp");
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
