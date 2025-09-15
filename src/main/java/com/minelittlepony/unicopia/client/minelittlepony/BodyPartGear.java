package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.api.pony.PonyData;
import com.minelittlepony.client.model.ClientPonyModel;
import com.minelittlepony.client.model.ModelType;
import com.minelittlepony.client.model.PlayerModelKey;
import com.minelittlepony.client.model.entity.race.ChangelingModel;
import com.minelittlepony.client.model.entity.race.PegasusModel;
import com.minelittlepony.client.model.entity.race.UnicornModel;
import com.minelittlepony.client.model.part.UnicornHorn;
import com.minelittlepony.client.render.entity.state.PlayerPonyRenderState;
import com.minelittlepony.client.render.entity.state.PonyRenderState;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.unicopia.FlightType;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import com.minelittlepony.api.pony.meta.Race;

class BodyPartGear<M extends ClientPonyModel<PonyRenderState>> implements Gear {
    private static final Predicate<PlayerPonyRenderState> MINE_LP_HAS_NO_WINGS = s -> !s.getRace().hasWings() && !s.getRace().hasBugWings();
    private static final Predicate<PlayerPonyRenderState> MINE_LP_HAS_NO_HORN = s -> !s.getRace().hasHorn();

    private static final Predicate<PlayerPonyRenderState> EXCLUDE_PEGASUS_AMULET = s -> !CasterState.of(s).amulet.stack().isOf(UItems.PEGASUS_AMULET);
    private static final Predicate<PlayerPonyRenderState> INCLUDE_ALICORN_AMULET = s -> CasterState.of(s).amulet.stack().isOf(UItems.ALICORN_AMULET);

    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus_pony.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted_pony.png");

    public static final Predicate<PlayerPonyRenderState> BUG_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(EXCLUDE_PEGASUS_AMULET).and(s -> CasterState.of(s).species.any(com.minelittlepony.unicopia.Race.CHANGELING::equals));
    public static final Identifier BUG_WINGS = Unicopia.id("textures/models/wings/bug_pony.png");

    public static final Predicate<PlayerPonyRenderState> BAT_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(EXCLUDE_PEGASUS_AMULET).and(s -> CasterState.of(s).species.any(com.minelittlepony.unicopia.Race.BAT::equals));
    public static final Identifier BAT_WINGS = Unicopia.id("textures/models/wings/bat_pony.png");

    public static final Predicate<PlayerPonyRenderState> UNICORN_HORN_PREDICATE = MINE_LP_HAS_NO_HORN.and(INCLUDE_ALICORN_AMULET.or(s -> CasterState.of(s).species.any(r -> r.canCast())));
    public static final Identifier UNICORN_HORN = Unicopia.id("textures/models/horn/unicorn.png");

    public static final Predicate<PlayerPonyRenderState> PEGA_WINGS_PREDICATE = MINE_LP_HAS_NO_WINGS.and(INCLUDE_ALICORN_AMULET.or(s -> CasterState.of(s).species.any(race -> race != com.minelittlepony.unicopia.Race.BAT && race.flightType() == FlightType.AVIAN)));
    public static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus_pony.png");

    public static BodyPartGear<WingsGearModel> pegasusWings() {
        return new BodyPartGear<>(Race.PEGASUS, BodyPart.BODY, ModelType.PEGASUS, PEGA_WINGS_PREDICATE, WingsGearModel::new, WingsGearModel::getWings, e -> {
            if (CasterState.of(e).pegasusAmulet) {
                return CasterState.of(e).inHell ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
            }
            return PEGASUS_WINGS;
        });
    }

    public static BodyPartGear<WingsGearModel> batWings() {
        return new BodyPartGear<>(Race.BATPONY, BodyPart.BODY, ModelType.BAT_PONY, BAT_WINGS_PREDICATE, WingsGearModel::new, WingedPonyModel::getWings, e -> BAT_WINGS);
    }

    public static BodyPartGear<BugWingsGearModel> bugWings() {
        return new BodyPartGear<>(Race.CHANGELING, BodyPart.BODY, ModelType.CHANGELING, BUG_WINGS_PREDICATE, BugWingsGearModel::new, WingedPonyModel::getWings, e -> BUG_WINGS);
    }

    public static BodyPartGear<HornGearModel> unicornHorn() {
        return new BodyPartGear<>(Race.UNICORN, BodyPart.HEAD, ModelType.UNICORN, UNICORN_HORN_PREDICATE, HornGearModel::new, HornGearModel::getHorn, e -> UNICORN_HORN);
    }

    private final Race race;
    private final M model;
    private final Predicate<PlayerPonyRenderState> renderTargetPredicate;
    private final SubModel<?> part;
    private final Function<PlayerPonyRenderState, Identifier> textureSupplier;
    private final BodyPart gearLocation;
    private final LoadingCache<PonyData, PonyData> dataCache = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build(CacheLoader.from(this::convertMetadata));

    private @Nullable PlayerPonyRenderState state;

    public BodyPartGear(
            Race race,
            BodyPart gearLocation,
            PlayerModelKey<? super M> modelKey,
            Predicate<PlayerPonyRenderState> renderTargetPredicate,
            MsonModel.Factory<M> modelFactory,
            Function<? super M, SubModel<?>> partExtractor,
            Function<PlayerPonyRenderState, Identifier> textureSupplier) {
        this.race = race;
        this.gearLocation = gearLocation;
        this.model = modelKey.steveKey().createModel(modelFactory);
        this.part = partExtractor.apply(this.model);
        this.renderTargetPredicate = renderTargetPredicate;
        this.textureSupplier = textureSupplier;
    }

    private PonyData convertMetadata(PonyData metadata) {
        return new PonyData(BodyPartGear.this.race,
                metadata.tailLength(), metadata.tailShape(),
                metadata.gender(), metadata.size(),
                metadata.glowColor(), metadata.noSkin(),
                metadata.priority(), metadata.gear());
    }

    @Override
    public BodyPart getGearLocation() {
        return gearLocation;
    }

    @Override
    public boolean canRender(PonyModel<?> model, EntityRenderState state) {
        return state instanceof PlayerPonyRenderState pony
            && !pony.getRace().isHuman()
            && pony.getRace() != race
            && renderTargetPredicate.test(pony);
    }

    @Override
    public <S extends EntityRenderState> Identifier getTexture(S entity, Context<S, ?> context) {
        return textureSupplier.apply((PlayerPonyRenderState)entity);
    }

    @Override
    public <S extends BipedEntityRenderState & PonyModel.AttributedHolder> void pose(PonyModel<S> model, S state, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        this.state = (PlayerPonyRenderState)state;
    }

    public static PonyData copyOf(PonyData metadata, Race newRace) {
        return new PonyData(newRace, metadata.tailLength(), metadata.tailShape(), metadata.gender(),
                metadata.size(), metadata.glowColor(), metadata.noSkin(), metadata.priority(), metadata.gear());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        var originalRace = state.getRace();
        var originalData = state.getAttributes().metadata;
        try {
            state.getAttributes().metadata = this.dataCache.get(originalData);
            state.race = state.getAttributes().metadata.race();
            model.setAngles(state);
            part.renderPart(stack, consumer, light, overlay, color);
        } catch (ExecutionException ignored) {
        } finally {
            state.getAttributes().metadata = originalData;
            state.race = originalRace;
        }
    }

    static final class WingsGearModel extends PegasusModel<PonyRenderState> {
        public WingsGearModel(ModelPart tree) {
            super(tree, false);
        }
    }

    static final class BugWingsGearModel extends ChangelingModel<PonyRenderState> {
        public BugWingsGearModel(ModelPart tree) {
            super(tree, false);
        }
    }

    static final class HornGearModel extends UnicornModel<PonyRenderState> {
        public HornGearModel(ModelPart tree) {
            super(tree, false);
        }

        public UnicornHorn<PonyRenderState> getHorn() {
            return horn;
        }
    }
}
