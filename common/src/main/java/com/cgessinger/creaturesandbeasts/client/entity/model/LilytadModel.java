package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.LilytadEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class LilytadModel extends GeoModel<LilytadEntity> {
    private static final Identifier LILYTAD_MODEL = CreaturesAndBeasts.id("geo/entity/lilytad/lilytad");
    private static final Identifier LILYTAD_SHEARED_TEXTURE = CreaturesAndBeasts.id("textures/entity/lilytad/lilytad_sheared.png");
    private static final Identifier LILYTAD_ANIMATIONS = CreaturesAndBeasts.id("lilytad");
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("lilytad_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return LILYTAD_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, LILYTAD_SHEARED_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(LilytadEntity entity) {
        return LILYTAD_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(LilytadEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(TEXTURE, entity.getSheared() ? LILYTAD_SHEARED_TEXTURE : entity.getLilytadType().getTextureLocation());
    }
}
