package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.EndWhaleEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class EndWhaleModel extends GeoModel<EndWhaleEntity> {
    private static final Identifier END_WHALE_MODEL = CreaturesAndBeasts.id("geo/entity/end_whale/end_whale");
    private static final Identifier END_WHALE_TEXTURE = CreaturesAndBeasts.id("textures/entity/end_whale/end_whale.png");
    private static final Identifier END_WHALE_SADDLE_TEXTURE = CreaturesAndBeasts.id("textures/entity/end_whale/end_whale_saddle.png");
    private static final Identifier END_WHALE_ANIMATIONS = CreaturesAndBeasts.id("end_whale");
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("end_whale_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return END_WHALE_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, END_WHALE_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(EndWhaleEntity entity) {
        return END_WHALE_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(EndWhaleEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(TEXTURE, entity.isSaddled() ? END_WHALE_SADDLE_TEXTURE : END_WHALE_TEXTURE);
    }
}
