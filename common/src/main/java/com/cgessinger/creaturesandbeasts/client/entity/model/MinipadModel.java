package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class MinipadModel extends GeoModel<MinipadEntity> {
    private static final Identifier MINIPAD_MODEL = CreaturesAndBeasts.id("geo/entity/minipad/minipad");
    private static final Identifier MINIPAD_SHEARED_TEXTURE = CreaturesAndBeasts.id("textures/entity/minipad/minipad_sheared.png");
    private static final Identifier MINIPAD_ANIMATIONS = CreaturesAndBeasts.id("minipad");
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("minipad_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MINIPAD_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, MINIPAD_SHEARED_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(MinipadEntity entity) {
        return MINIPAD_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(MinipadEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(TEXTURE, entity.getSheared() ? MINIPAD_SHEARED_TEXTURE : entity.getMinipadType().getTextureLocation());
    }
}
