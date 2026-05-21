package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.LizardEntity;
import com.cgessinger.creaturesandbeasts.init.CNBLizardTypes;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class LizardModel extends GeoModel<LizardEntity> {
    private static final Identifier LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/lizard");
    private static final Identifier MUSHROOM_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/mushroom_lizard");
    private static final Identifier SAD_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/sad_lizard");
    private static final Identifier SAD_MUSHROOM_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/sad_mushroom_lizard");
    private static final Identifier LIZARD_ANIMATIONS = CreaturesAndBeasts.id("lizard");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("lizard_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("lizard_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, LIZARD_MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, CNBLizardTypes.DESERT.getTextureLocation());
    }

    @Override
    public Identifier getAnimationResource(LizardEntity entity) {
        return LIZARD_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(LizardEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, modelFor(entity));
        renderState.addGeckolibData(TEXTURE, entity.getSad() ? entity.getLizardType().getSadTextureLocation() : entity.getLizardType().getTextureLocation());
    }

    private static Identifier modelFor(LizardEntity entity) {
        if (entity.getLizardType().equals(CNBLizardTypes.MUSHROOM)) {
            return entity.getSad() ? SAD_MUSHROOM_LIZARD_MODEL : MUSHROOM_LIZARD_MODEL;
        }

        return entity.getSad() ? SAD_LIZARD_MODEL : LIZARD_MODEL;
    }
}
