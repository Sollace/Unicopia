package com.minelittlepony.unicopia.world.recipe;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.minelittlepony.unicopia.world.recipe.ingredient.PredicatedIngredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;

public class Pattern {

    public static Pattern read(PacketByteBuf buf) {
        return new Pattern(
                Utils.read(buf, PredicatedIngredient.EMPTY, PredicatedIngredient::read),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static Pattern read(JsonObject json) {
        String[] patterns = removePadding(readPattern(JsonHelper.getArray(json, "pattern")));
        Map<String, PredicatedIngredient> symbols = readSymbols(JsonHelper.getObject(json, "key"));

        return new Pattern(patterns, symbols);
    }

    public final DefaultedList<PredicatedIngredient> matrix;

    public final int width;
    public final int height;

    public Pattern(DefaultedList<PredicatedIngredient> matrix, int width, int height) {
        this.matrix = matrix;
        this.width = width;
        this.height = height;
    }

    public Pattern(String[] pattern, Map<String, PredicatedIngredient> ingredients) {
        this(pattern, ingredients, pattern[0].length(), pattern.length);
    }

    private Pattern(String[] pattern, Map<String, PredicatedIngredient> ingredients, int width, int height) {
        this(buildIngredientMatrix(pattern, ingredients, width, height), width, height);
    }

    public boolean matches(CraftingInventory inv) {
        for(int x = 0; x <= inv.getWidth() - width; x++) {
            for(int y = 0; y <= inv.getHeight() - height; y++) {
                if (matchesPattern(inv, x, y, true) || matchesPattern(inv, x, y, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void write(PacketByteBuf buf) {
        Utils.write(buf, matrix, PredicatedIngredient::write);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
    }

    public int size() {
        return matrix.size();
    }

    private boolean matchesPattern(CraftingInventory inv, int offsetX, int offsetY, boolean reflected) {
        for(int x = 0; x < inv.getWidth(); ++x) {
            for(int y = 0; y < inv.getHeight(); ++y) {
                int left = x - offsetX;
                int top = y - offsetY;

                PredicatedIngredient ingredient = PredicatedIngredient.EMPTY;

                if (left >= 0 && top >= 0 && left < width && top < height) {
                    if (reflected) {
                        ingredient = matrix.get(width - left - 1 + top * width);
                    } else {
                        ingredient = matrix.get(left + top * width);
                    }
                }

                if (!ingredient.matches(inv.getStack(x + y * inv.getWidth()), 1)) {
                    return false;
                }
            }
        }

        return true;
    }

    static DefaultedList<PredicatedIngredient> buildIngredientMatrix(String[] pattern, Map<String, PredicatedIngredient> symbols, int width, int height) {
        DefaultedList<PredicatedIngredient> result = DefaultedList.ofSize(width * height, PredicatedIngredient.EMPTY);

        Set<String> unresolved = Sets.newHashSet(symbols.keySet());
        unresolved.remove(" ");

        for(int i = 0; i < pattern.length; ++i) {
            for(int j = 0; j < pattern[i].length(); ++j) {
                String key = pattern[i].substring(j, j + 1);

                PredicatedIngredient ingredient = symbols.get(key);

                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + key + "' but it's not defined in the key");
                }

                unresolved.remove(key);
                result.set(j + width * i, ingredient);
            }
        }

        if (!unresolved.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + unresolved);
        }

        return result;
    }

    /**
     * Removes empty space from around the recipe pattern.
     *
     * Turns patterns such as:
     * "   o"
     * "   a"
     * "    "
     * Into:
     * "o"
     * "a"
     *
     * @param pattern
     * @return A new recipe pattern with all leading and trailing empty rows/columns removed.
     */
    static String[] removePadding(String... pattern) {
        int left = Integer.MAX_VALUE;
        int right = 0;

        int top = 0;
        int bottom = 0;

        for(int yPosition = 0; yPosition < pattern.length; ++yPosition) {
            String row = pattern[yPosition];

            left = Math.min(left, findFirstSymbol(row));

            int rowEnd = findLastSymbol(row);

            right = Math.max(right, rowEnd);

            if (rowEnd < 0) {
                if (top == yPosition) {
                    top++;
                }

                bottom++;
            } else {
                bottom = 0;
            }
        }

        if (pattern.length == bottom) {
            return new String[0];
        }

        String[] strings = new String[pattern.length - bottom - top];

        for(int i = 0; i < strings.length; i++) {
            strings[i] = pattern[i + top].substring(left, right + 1);
        }

        return strings;
    }

    private static int findFirstSymbol(String pattern) {
       int i;
       for(i = 0; i < pattern.length() && pattern.charAt(i) == ' '; ++i) {}

       return i;
    }

    private static int findLastSymbol(String pattern) {
       int i;
       for(i = pattern.length() - 1; i >= 0 && pattern.charAt(i) == ' '; --i) {}

       return i;
    }

    static String[] readPattern(JsonArray json) {
        String[] rows = new String[json.size()];

        if (rows.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        if (rows.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }

        for(int i = 0; i < rows.length; ++i) {
            String column = JsonHelper.asString(json.get(i), "pattern[" + i + "]");

            if (column.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }

            if (i > 0 && rows[0].length() != column.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }

            rows[i] = column;
        }

        return rows;
    }

    static Map<String, PredicatedIngredient> readSymbols(JsonObject json) {
        return Util.make(json.entrySet().stream().collect(Collectors.toMap(e -> {
               if (e.getKey().length() != 1) {
                   throw new JsonSyntaxException("Invalid key entry: '" + e.getKey() + "' is an invalid symbol (must be 1 character only).");
               }

               if (" ".equals(e.getKey())) {
                   throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
               }

               return e.getKey();
           },
           e -> PredicatedIngredient.one(e.getValue())
       )), m -> m.put(" ", PredicatedIngredient.EMPTY));
    }
}
