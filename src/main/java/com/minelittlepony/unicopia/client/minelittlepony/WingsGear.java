package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.api.pony.*;
import com.minelittlepony.client.model.ClientPonyModel;
import com.minelittlepony.client.model.ModelType;
import com.minelittlepony.client.model.entity.race.PegasusModel;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

class WingsGear implements IGear {
    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus_pony.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted_pony.png");
    private static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus_pony.png");
    private static final Identifier BAT_WINGS = Unicopia.id("textures/models/wings/bat_pony.png");

    private final Model model = ModelType.PEGASUS.steveKey().createModel(Model::new);

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity l
            && !MineLPDelegate.getInstance().getRace(entity).canFly()
            && (AmuletSelectors.PEGASUS_AMULET.test(l) || Equine.of(entity).filter(this::canRender).isPresent());
    }

    protected boolean canRender(Equine<?> equine) {
        if (equine instanceof Pony pony) {
            return pony.getObservedSpecies().canInteractWithClouds();
        }
        return equine.getSpecies().canFly();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        Living<?> living = Living.living(entity);
        if (living == null) {
            return DefaultPonySkinHelper.STEVE;
        }

        if (AmuletSelectors.PEGASUS_AMULET.test(living.asEntity())) {
            return entity.world.getDimension().ultrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
        }

        Race race = living instanceof Pony pony ? pony.getObservedSpecies() : living.getSpecies();
        if (race.canInteractWithClouds()) {
            return PEGASUS_WINGS;
        }

        return BAT_WINGS;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void pose(IModel model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        ((ClientPonyModel)model).copyAttributes(this.model);
        this.model.getWings().setRotationAndAngles(rainboom, interpolatorId, move, swing, bodySwing, ticks);
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        model.getWings().renderPart(stack, consumer, light, overlay, red, green, blue, alpha, model.getAttributes());
    }

    static class Model extends PegasusModel<LivingEntity> {
        public Model(ModelPart tree) {
            super(tree, false);
        }

        @Override
        public boolean canFly() {
            return true;
        }
    }
}
