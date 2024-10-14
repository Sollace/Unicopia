package com.minelittlepony.unicopia.util;

import net.minecraft.util.ActionResult;

public record TypedActionResult<T> (ActionResult result, T value) {
    public static <T> TypedActionResult<T> success(T value) {
        return new TypedActionResult<>(ActionResult.SUCCESS, value);
    }

    public static <T> TypedActionResult<T> pass(T value) {
        return new TypedActionResult<>(ActionResult.PASS, value);
    }

    public static <T> TypedActionResult<T> fail(T value) {
        return new TypedActionResult<>(ActionResult.FAIL, value);
    }
}
