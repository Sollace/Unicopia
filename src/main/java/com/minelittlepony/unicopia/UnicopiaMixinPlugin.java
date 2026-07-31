package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.api.FabricLoader;

public class UnicopiaMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "com.minelittlepony.unicopia.mixin";

    private final Supplier<Boolean> hasConnector = requireMod("connectormod");
    private final Set<Map.Entry<String, Supplier<Boolean>>> modRequirements = Map.of(
        "sodium", requireMod("sodium"),
        "trinkets", requireMod("strinkets"),
        "seasons", requireMod("seasons"),
        "ad_astra", requireMod("ad_astra"),
        "minelp", requireMod("minelp"),
        "forgified", hasConnector,
        "fabricified", () -> !hasConnector.get()
    ).entrySet();

    private static Supplier<Boolean> requireMod(String modid) {
        return Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded(modid));
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MIXIN_PACKAGE)) {
            for (var requirement : modRequirements) {
                if (mixinClassName.indexOf(requirement.getKey()) != -1) {
                    return requirement.getValue().get();
                }
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
