package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.item.UItems;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DiscoveryToast implements Toast {
    private static final long MAX_AGE = 5000L;
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final Text TITLE = Text.translatable("unicopia.toast.discoveries.title");
    private static final Text DESCRIPTION = Text.translatable("unicopia.toast.discoveries.description");

    private final List<Identifier> discoveries = new ArrayList<>();
    private long startTime;
    private long age;

    private boolean justUpdated;


    @Override
    public Visibility getVisibility() {
        return discoveries.isEmpty() || age >= MAX_AGE ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (justUpdated) {
            this.startTime = time;
            justUpdated = false;
        }
        age = startTime - this.startTime;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, getWidth(), getHeight());
        context.drawText(textRenderer, TITLE, 30, 7, -11534256, false);
        context.drawText(textRenderer, DESCRIPTION, 30, 18, -16777216, false);

        Identifier icon = discoveries.get((int)(startTime / Math.max(1L, MAX_AGE / discoveries.size()) % discoveries.size()));

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(0.6F, 0.6F, 1);
        context.drawItem(UItems.SPELLBOOK.getDefaultStack(), 3, 3);
        matrices.pop();

        context.drawTexture(RenderLayer::getGuiTextured, icon, 8, 8, 1, 0, 0, 16, 16, 16, 16);
    }

    public DiscoveryToast addDiscoveries(Identifier icon) {
        if (!discoveries.contains(icon)) {
            discoveries.add(icon);
        }
        justUpdated = true;

        return this;
    }

    class Discovery {
        final Identifier icon;
        final Text description;

        Discovery(Identifier icon, Text description) {
            this.icon = icon;
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Discovery
                    && Objects.equal(icon, ((Discovery) o).icon)
                    && Objects.equal(description, ((Discovery) o).description);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(icon, description);
        }
    }

    public static void show(ToastManager manager, Identifier icon) {
        DiscoveryToast existing = manager.getToast(DiscoveryToast.class, TYPE);
        if (existing == null) {
            manager.add(new DiscoveryToast().addDiscoveries(icon));
        } else {
            existing.addDiscoveries(icon);
        }
    }
}

