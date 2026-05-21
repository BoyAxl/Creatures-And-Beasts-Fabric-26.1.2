package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.SporelingEntity;
import com.cgessinger.creaturesandbeasts.init.CNBSporelingTypes;
import com.cgessinger.creaturesandbeasts.util.SporelingType;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SporelingModel extends GeoModel<SporelingEntity> {
    private static final Identifier SPORELING_ANIMATIONS = CreaturesAndBeasts.id("sporeling");
    private static final Identifier GOOMY_MODEL = CreaturesAndBeasts.id("geo/entity/sporeling/sporeling_goomy");
    private static final Identifier SHRIMPSNAIL_MODEL = CreaturesAndBeasts.id("geo/entity/sporeling/sporeling_shrimpsnail");
    private static final Identifier BIT0_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_bit0.png");
    private static final Identifier LISTACALISTA_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_listacalista.png");
    private static final Identifier YUNGWILDER_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_yungwilder.png");
    private static final Identifier GOOMY_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_goomy.png");
    private static final Identifier SHRIMPSNAIL_TEXTURE = CreaturesAndBeasts.id("textures/entity/sporeling/sporeling_shrimpsnail.png");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("sporeling_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("sporeling_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, CNBSporelingTypes.RED_OVERWORLD.getModelLocation());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, CNBSporelingTypes.RED_OVERWORLD.getTextureLocation());
    }

    @Override
    public Identifier getAnimationResource(SporelingEntity entity) {
        return SPORELING_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(SporelingEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, modelFor(entity));
        renderState.addGeckolibData(TEXTURE, textureFor(entity));
    }

    private static Identifier modelFor(SporelingEntity entity) {
        if (entity.hasCustomName() && entity.getSporelingType().getHostility().equals(SporelingType.SporelingHostility.FRIENDLY)) {
            String customName = entity.getCustomName().getString();
            if (customName.equals("Bit0") || customName.equals("ListaCalista") || customName.equals("yungwilder")) {
                return CNBSporelingTypes.RED_OVERWORLD.getModelLocation();
            } else if (customName.equals("Goomy")) {
                return GOOMY_MODEL;
            } else if (customName.equals("ShrimpSnail")) {
                return SHRIMPSNAIL_MODEL;
            }
        }

        return entity.getSporelingType().getModelLocation();
    }

    private static Identifier textureFor(SporelingEntity entity) {
        if (entity.hasCustomName() && entity.getSporelingType().getHostility().equals(SporelingType.SporelingHostility.FRIENDLY)) {
            return switch (entity.getCustomName().getString()) {
                case "Bit0" -> BIT0_TEXTURE;
                case "ListaCalista" -> LISTACALISTA_TEXTURE;
                case "yungwilder" -> YUNGWILDER_TEXTURE;
                case "Goomy" -> GOOMY_TEXTURE;
                case "ShrimpSnail" -> SHRIMPSNAIL_TEXTURE;
                default -> entity.getSporelingType().getTextureLocation();
            };
        }

        return entity.getSporelingType().getTextureLocation();
    }
}
