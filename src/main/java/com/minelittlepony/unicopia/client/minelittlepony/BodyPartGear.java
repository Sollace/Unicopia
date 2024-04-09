package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.api.pony.IPonyData;
import com.minelittlepony.client.model.ClientPonyModel;
import com.minelittlepony.client.model.ModelType;
import com.minelittlepony.client.model.PlayerModelKey;
import com.minelittlepony.client.model.entity.race.ChangelingModel;
import com.minelittlepony.client.model.entity.race.PegasusModel;
import com.minelittlepony.client.model.entity.race.UnicornModel;
import com.minelittlepony.client.model.part.UnicornHorn;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.FlightType;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.AmuletSelectors;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.api.pony.meta.Race;

class BodyPartGear<M extends ClientPonyModel<LivingEntity> & MsonModel & IModel> implements IGear {

    private static final Predicate<LivingEntity> MINE_LP_HAS_NO_WINGS = e -> !MineLPDelegate.getInstance().getRace(e).canFly();
    private static final Predicate<LivingEntity> MINE_LP_HAS_NO_HORN = e -> !MineLPDelegate.getInstance().getRace(e).canCast();

    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus_pony.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted_pony.png");

    public static final Predicate<LivingEntity> BUG_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(AmuletSelectors.PEGASUS_AMULET.negate()).and(EquinePredicates.PLAYER_CHANGELING);
    public static final Identifier BUG_WINGS = Unicopia.id("textures/models/wings/bug_pony.png");

    public static final Predicate<LivingEntity> BAT_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(AmuletSelectors.PEGASUS_AMULET.negate()).and(EquinePredicates.PLAYER_BAT);
    public static final Identifier BAT_WINGS = Unicopia.id("textures/models/wings/bat_pony.png");

    public static final Predicate<LivingEntity> UNICORN_HORN_PREDICATE = MINE_LP_HAS_NO_HORN.and(AmuletSelectors.ALICORN_AMULET.or(EquinePredicates.raceMatches(com.minelittlepony.unicopia.Race::canCast)));
    public static final Identifier UNICORN_HORN = Unicopia.id("textures/models/horn/unicorn.png");

    public static final Predicate<LivingEntity> PEGA_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(AmuletSelectors.PEGASUS_AMULET.or(EquinePredicates.raceMatches(race -> race != com.minelittlepony.unicopia.Race.BAT && race.flightType() == FlightType.AVIAN)));
    public static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus_pony.png");

    public static BodyPartGear<WingsGearModel> pegasusWings() {
        return new BodyPartGear<>(Race.PEGASUS, BodyPart.BODY, ModelType.PEGASUS, PEGA_WINGS_PREDICATE, WingsGearModel::new, WingsGearModel::getWings, e -> {
            if (AmuletSelectors.PEGASUS_AMULET.test((LivingEntity)e)) {
                return e.getWorld().getDimension().ultrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
            }
            return PEGASUS_WINGS;
        });
    }

    public static BodyPartGear<WingsGearModel> batWings() {
        return new BodyPartGear<>(Race.BATPONY, BodyPart.BODY, ModelType.BAT_PONY, BAT_WINGS_PREDICATE, WingsGearModel::new, IPegasus::getWings, e -> BAT_WINGS);
    }

    public static BodyPartGear<BugWingsGearModel> bugWings() {
        return new BodyPartGear<>(Race.CHANGELING, BodyPart.BODY, ModelType.CHANGELING, BUG_WINGS_PREDICATE, BugWingsGearModel::new, IPegasus::getWings, e -> BUG_WINGS);
    }

    public static BodyPartGear<HornGearModel> unicornHorn() {
        return new BodyPartGear<>(Race.UNICORN, BodyPart.HEAD, ModelType.UNICORN, UNICORN_HORN_PREDICATE, HornGearModel::new, HornGearModel::getHorn, e -> UNICORN_HORN);
    }

    private final M model;
    private final Predicate<LivingEntity> renderTargetPredicate;
    private final IPart part;
    private final Function<Entity, Identifier> textureSupplier;
    private final BodyPart gearLocation;
    private final UnicopiaPonyData ponyData;

    public BodyPartGear(
            Race race,
            BodyPart gearLocation,
            PlayerModelKey<LivingEntity, ? super M> modelKey,
            Predicate<LivingEntity> renderTargetPredicate,
            MsonModel.Factory<M> modelFactory,
            Function<? super M, IPart> partExtractor,
            Function<Entity, Identifier> textureSupplier) {
        this.ponyData = new UnicopiaPonyData(race);
        this.gearLocation = gearLocation;
        this.model = modelKey.steveKey().createModel(modelFactory);
        this.part = partExtractor.apply(this.model);
        this.renderTargetPredicate = renderTargetPredicate;
        this.textureSupplier = textureSupplier;
    }

    @Override
    public BodyPart getGearLocation() {
        return gearLocation;
    }

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity l
            && MineLPDelegate.getInstance().getRace(entity).isEquine()
            && renderTargetPredicate.test(l);
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return textureSupplier.apply(entity);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void pose(IModel model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float tickDelta) {
        final IPonyData data = this.model.getAttributes().metadata;
        try {
            ponyData.original = data;
            ((ClientPonyModel)model).copyAttributes(this.model);
            this.model.getAttributes().metadata = ponyData;
            this.model.animateModel((LivingEntity)entity, move, swing, tickDelta);
            this.model.setAngles((LivingEntity)entity, move, swing, entity.age + tickDelta, 0, 0);
        } finally {
            this.model.getAttributes().metadata = data;
        }
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        part.renderPart(stack, consumer, light, overlay, red, green, blue, alpha, model.getAttributes());
    }

    static final class WingsGearModel extends PegasusModel<LivingEntity> {
        public WingsGearModel(ModelPart tree) {
            super(tree, false);
        }

        @Override
        public boolean canFly() {
            return true;
        }
    }

    static final class BugWingsGearModel extends ChangelingModel<LivingEntity> {
        public BugWingsGearModel(ModelPart tree) {
            super(tree, false);
        }

        @Override
        public boolean canFly() {
            return true;
        }
    }

    static final class HornGearModel extends UnicornModel<LivingEntity> {
        public HornGearModel(ModelPart tree) {
            super(tree, false);
        }

        @Override
        public boolean canFly() {
            return true;
        }

        public UnicornHorn getHorn() {
            return horn;
        }
    }
}
