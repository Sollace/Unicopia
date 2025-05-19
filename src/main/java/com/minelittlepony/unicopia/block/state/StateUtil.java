package com.minelittlepony.unicopia.block.state;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

public interface StateUtil {
    Splitter COMMA_SPLITTER = Splitter.on(',');
    Splitter KEY_VALUE_SPLITTER = Splitter.on('=').limit(2);
    Splitter STATE_SPLITTER = Splitter.on(CharMatcher.anyOf("[]")).limit(2);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static BlockState copyState(BlockState from, @Nullable BlockState to) {
        if (to == null) {
            return to;
        }
        for (var property : from.getProperties()) {
            to = to.withIfExists((Property)property, from.get(property));
        }
        return to;
    }

    static BlockState stateFromString(String string) {
        Iterator<String> pair = Splitter.on(CharMatcher.anyOf("[]")).limit(3).split(string).iterator();
        if (!pair.hasNext()) {
            return Blocks.AIR.getDefaultState();
        }
        Block block = Identifier.validate(pair.next()).result().map(Registries.BLOCK::get).orElse(null);
        if (block == null) {
            return Blocks.AIR.getDefaultState();
        }
        if (!pair.hasNext()) {
            return block.getDefaultState();
        }
        return stateFromKeyMap(block, pair.next());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static BlockState stateFromKeyMap(Block block, String keyMap) {
        var stateFactory = block.getStateManager();
        var state = block.getDefaultState();
        for (String pair : COMMA_SPLITTER.split(keyMap)) {
            Iterator<String> iterator = KEY_VALUE_SPLITTER.split(pair).iterator();
            if (!iterator.hasNext()) continue;
            String propertyName = iterator.next();
            var property = stateFactory.getProperty(propertyName);
            if (property != null && iterator.hasNext()) {
                String value = iterator.next();
                var comparable = property.parse(value).orElse(null);
                if (comparable != null) {
                    state = state.with((Property)property, (Comparable)comparable);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + value + "' for blockstate property: '" + propertyName + "' " + property.getValues());
            }
            if (!propertyName.isEmpty()) {
                throw new RuntimeException("Unknown blockstate property: '" + propertyName + "'");
            }
        }
        return state;
    }
}
