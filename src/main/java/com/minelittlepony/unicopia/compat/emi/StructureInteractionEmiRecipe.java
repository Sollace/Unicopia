package com.minelittlepony.unicopia.compat.emi;

import java.util.List;

import com.minelittlepony.unicopia.block.state.Schematic;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

public class StructureInteractionEmiRecipe implements EmiRecipe {

    private final EmiRecipeCategory category;
    private final Identifier id;
    private final Schematic schematic;

    private final List<EmiIngredient> inputs;
    private final List<EmiStack> output;

    private final Identifier processIcon;

    private int age;

    public StructureInteractionEmiRecipe(EmiRecipeCategory category, Identifier id, Schematic schematic, List<EmiIngredient> inputs, EmiStack output, Identifier processIcon) {
        this.category = category;
        this.id = id;
        this.schematic = schematic;
        this.inputs = inputs;
        this.output = List.of(output);
        this.processIcon = processIcon;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return schematic.dy() * 8 + 80 + 20 * (inputs.size() - 2);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = schematic.dy() * 8;
        int row = 0;
        age = 0;
        widgets.addDrawable(10, y / 2, 100, 100, this::renderSchematic);
        int x = 10;
        for (int i = 0; i < inputs.size(); i++) {
            if (i > 1) {
                x -= 40;
                row += 20;
            }
            if (i > 0) {
                widgets.addTexture(EmiTexture.PLUS, x + 3, y + 53 + row);
                x += 20;
            }
            widgets.addSlot(inputs.get(i), x, y + 50 + row).catalyst(i > 0);
            x += 20;
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 73, y + 52);
        widgets.addSlot(output.get(0), 100, y + 47).large(true).recipeContext(this);
        widgets.addTexture(processIcon, 73, y + 45, 13, 13, 0, 0, 16, 16, 16, 16).tooltipText(List.of(Text.translatable(
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
