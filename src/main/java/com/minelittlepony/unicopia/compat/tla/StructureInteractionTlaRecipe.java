package com.minelittlepony.unicopia.compat.tla;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.TransformCropsRecipe;
import com.minelittlepony.unicopia.recipe.URecipes;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

public class StructureInteractionTlaRecipe implements TlaRecipe {
    private final RecipeCategory category;
    private final Identifier id;
    private final Schematic schematic;

    private final List<TlaIngredient> allInputs;
    private final List<TlaIngredient> inputs;
    private final List<TlaIngredient> catalysts;
    private final List<TlaStack> output;

    private final TextureConfig processIcon;

    private int age;

    static void generateFarmingRecipes(RecipeCategory category, PluginContext context) {
        context.addRecipeGenerator(URecipes.GROWING, recipe -> {
            return new StructureInteractionTlaRecipe(
                    category,
                    recipe.id(),
                    new Schematic.Builder()
                        .fill(0, 0, 0, 6, 0, 6, recipe.value().getCatalystState())
                        .set(3, 0, 3, Blocks.FARMLAND.getDefaultState())
                        .set(3, 1, 3, recipe.value().getTargetState())
                        .build(),
                    List.of(
                            TlaStack.of(recipe.value().getTarget()).asIngredient(),
                            TlaStack.of(ItemVariant.of(recipe.value().getCatalyst()), TransformCropsRecipe.AREA).asIngredient()
                    ),
                    TlaStack.of(recipe.value().getOutput()),
                    Unicopia.id("textures/gui/ability/grow.png")
            );
        });
    }

    static void generateAltarRecipes(RecipeCategory category, PluginContext context) {
        context.addGenerator(client -> List.of(new StructureInteractionTlaRecipe(
                category,
                Unicopia.id("altar/spectral_clock"),
                Schematic.ALTAR,
                List.of(
                    TlaStack.of(Items.CLOCK).asIngredient(),
                    TlaStack.of(UItems.SPELLBOOK).asIngredient(),
                    TlaStack.of(Blocks.SOUL_SAND).asIngredient(),
                    TlaStack.of(Blocks.LODESTONE).asIngredient(),
                    TlaStack.of(Blocks.OBSIDIAN, 8 * 4 + 8).asIngredient()
                ),
                TlaStack.of(UItems.SPECTRAL_CLOCK),
                Unicopia.id("textures/gui/race/alicorn.png")
        )));
    }

    public StructureInteractionTlaRecipe(RecipeCategory category, Identifier id, Schematic schematic, List<TlaIngredient> inputs, TlaStack output, Identifier processIcon) {
        this.category = category;
        this.id = id;
        this.schematic = schematic;
        this.allInputs = inputs;
        this.inputs = inputs.stream().limit(1).toList();
        this.catalysts = inputs.stream().skip(1).toList();
        this.output = List.of(output);
        this.processIcon = TextureConfig.builder().texture(processIcon).size(13, 13).regionSize(16, 16).textureSize(16, 16).build();
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<TlaStack> getOutputs() {
        return output;
    }


    @Override
    public List<TlaIngredient> getCatalysts() {
        return catalysts;
    }

    /*@Override
    public int getDisplayHeight() {
        return schematic.dy() * 8 + 80 + 20 * (inputs.size() - 2);
    }*/

    @Override
    public void buildGui(GuiBuilder widgets) {
        int y = schematic.dy() * 8;
        int row = 0;
        age = 0;
        widgets.addCustomWidget(10, y / 2, 100, 100, this::renderSchematic);
        int x = 10;
        for (int i = 0; i < allInputs.size(); i++) {
            if (i > 1) {
                x -= 40;
                row += 20;
            }
            if (i > 0) {
                widgets.addTexture(Main.PLUS, x + 3, y + 53 + row);
                x += 20;
            }
            var slot = widgets.addSlot(allInputs.get(i), x, y + 50 + row);
            if (i > 0) {
                slot.markCatalyst();
            }
            x += 20;
        }
        widgets.addArrow(73, y + 52, false);
        widgets.addSlot(output.get(0), 100, y + 47).makeLarge().markOutput();
        widgets.addTexture(processIcon, 73, y + 45).addTooltip(List.of(Text.translatable(
                Util.createTranslationKey("recipe", category.getId()) + "." + "instruction"
        )));
    }

    private void renderSchematic(DrawContext context, int mouseX, int mouseY, float delta) {
        if (schematic.volume() == 0) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        Immediate immediate = context.getVertexConsumers();

        MinecraftClient client = MinecraftClient.getInstance();

        matrices.push();
        float minSize = (Math.max(schematic.dz(), Math.max(schematic.dx(), schematic.dy())) + 1) * 16;
        float scale = 60 / minSize;
        matrices.scale(scale, scale, 1);
        matrices.translate(95, 40, 100);
        matrices.scale(16, -16, 16);

        matrices.peek().getNormalMatrix().scale(1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20));
        matrices.peek().getPositionMatrix().rotate(RotationAxis.POSITIVE_Y.rotationDegrees(40));
        matrices.translate(
                (-schematic.dx() - 1) / 2F,
                (-schematic.dy() - 1) / 2F,
                (-schematic.dz() - 1) / 2F
        );
        DiffuseLighting.disableGuiDepthLighting();

        age++;

        for (var entry : schematic.states()) {
            int x = entry.x() - schematic.dx() / 2;
            int z = entry.z() - schematic.dz() / 2;
            int distance = x * x + z * z;
            if (age >= distance * 2) {
                matrices.push();
                matrices.translate(entry.x(), entry.y(), entry.z());
                client.getBlockRenderManager().renderBlockAsEntity(entry.state(), matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            }
        }

        matrices.pop();
    }
}
