package com.minelittlepony.unicopia.world.recipe.enchanting;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface IConditionFactory {
    IUnlockCondition<?> create(JsonObject json);
}
