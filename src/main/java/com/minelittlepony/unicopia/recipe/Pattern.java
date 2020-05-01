package com.minelittlepony.unicopia.recipe;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.minelittlepony.unicopia.recipe.ingredient.Ingredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.Util;

public class Pattern {

    public static Pattern read(PacketByteBuf buf) {
        return new Pattern(
                Utils.read(buf, Ingredient.EMPTY, Ingredient::read),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static Pattern read(JsonObject json) {
        String[] patterns = combinePatternMatrix(getPattern(JsonHelper.getArray(json, "pattern")));
        Map<String, Ingredient> ingredients = readIngredients(JsonHelper.getObject(json, "key"));

        return new Pattern(patterns, ingredients);
    }

    public final DefaultedList<Ingredient> matrix;

    public final int width;
    public final int height;

    public Pattern(DefaultedList<Ingredient> matrix, int width, int height) {
        this.matrix = matrix;
        this.width = width;
        this.height = height;
    }

    public Pattern(String[] pattern, Map<String, Ingredient> ingredients) {
        this(pattern, ingredients, pattern[0].length(), pattern.length);
    }

    private Pattern(String[] pattern, Map<String, Ingredient> ingredients, int width, int height) {
        this(buildIngredientMatrix(pattern, ingredients, width, height), width, height);
    }

    public boolean matches(CraftingInventory inv) {
        for(int x = 0; x <= inv.getWidth() - width; x++) {
            for(int y = 0; y <= inv.getHeight() - height; y++) {
                if (matchesSmall(inv, x, y, true) || matchesSmall(inv, x, y, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void write(PacketByteBuf buf) {
        Utils.write(buf, matrix, Ingredient::write);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
    }

    public int size() {
        return matrix.size();
    }

    private boolean matchesSmall(CraftingInventory inv, int offsetX, int offsetY, boolean reflected) {
        for(int x = 0; x < inv.getWidth(); ++x) {
            for(int y = 0; y < inv.getHeight(); ++y) {
                int k = x - offsetX;
                int l = y - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < width && l < this.height) {
                    if (reflected) {
                        ingredient = matrix.get(width - k - 1 + l * this.width);
                    } else {
                        ingredient = matrix.get(k + l * this.width);
                    }
                }

                if (!ingredient.matches(inv.getInvStack(x + y * inv.getWidth()), 1)) {
                    return false;
                }
            }
        }

        return true;
    }

    static DefaultedList<Ingredient> buildIngredientMatrix(String[] pattern, Map<String, Ingredient> ingredients, int width, int height) {
        DefaultedList<Ingredient> result = DefaultedList.ofSize(width * height, Ingredient.EMPTY);

        Set<String> unsolved = Sets.newHashSet(ingredients.keySet());
        unsolved.remove(" ");

        for(int i = 0; i < pattern.length; ++i) {
            for(int j = 0; j < pattern[i].length(); ++j) {
                String key = pattern[i].substring(j, j + 1);

                Ingredient ingredient = ingredients.get(key);

                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + key + "' but it's not defined in the key");
                }

                unsolved.remove(key);
                result.set(j + width * i, ingredient);
            }
        }

        if (!unsolved.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + unsolved);
        }

        return result;
    }

    static String[] combinePatternMatrix(String... lines) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for(int row = 0; row < lines.length; ++row) {
            String line = lines[row];

            i = Math.min(i, lookAhead(line));

            int n = lookBack(line);

            j = Math.max(j, n);
            if (n < 0) {
                if (k == row) {
                    ++k;
                }

                ++l;
            } else {
                l = 0;
            }
        }

        if (lines.length == l) {
            return new String[0];
        }

        String[] strings = new String[lines.length - l - k];

        for(int o = 0; o < strings.length; ++o) {
            strings[o] = lines[o + k].substring(i, j + 1);
        }

        return strings;
    }

    private static int lookAhead(String pattern) {
       int i;
       for(i = 0; i < pattern.length() && pattern.charAt(i) == ' '; ++i) {}

       return i;
    }

    private static int lookBack(String pattern) {
       int i;
       for(i = pattern.length() - 1; i >= 0 && pattern.charAt(i) == ' '; --i) {}

       return i;
    }

    static String[] getPattern(JsonArray json) {
       String[] strings = new String[json.size()];
       if (strings.length > 3) {
          throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
       } else if (strings.length == 0) {
          throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
       } else {
          for(int i = 0; i < strings.length; ++i) {
             String string = JsonHelper.asString(json.get(i), "pattern[" + i + "]");
             if (string.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
             }

             if (i > 0 && strings[0].length() != string.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
             }

             strings[i] = string;
          }

          return strings;
       }
    }

    static Map<String, Ingredient> readIngredients(JsonObject json) {
        return Util.make(json.entrySet().stream().collect(Collectors.toMap(e -> {
               if (e.getKey().length() != 1) {
                   throw new JsonSyntaxException("Invalid key entry: '" + e.getKey() + "' is an invalid symbol (must be 1 character only).");
               }

               if (" ".equals(e.getKey())) {
                   throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
               }

               return e.getKey();
           },
           e -> Ingredient.one(e.getValue())
       )), m -> m.put(" ", Ingredient.EMPTY));
    }

}
