package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.YetiEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class YetiModel extends GeoModel<YetiEntity> {
    private static final Identifier YETI_MODEL = CreaturesAndBeasts.id("geo/entity/yeti/yeti");
    private static final Identifier BABY_YETI_MODEL = CreaturesAndBeasts.id("geo/entity/yeti/baby_yeti");
    private static final Identifier YETI_TEXTURE = CreaturesAndBeasts.id("textures/entity/yeti/yeti.png");
    private static final Identifier BABY_YETI_TEXTURE = CreaturesAndBeasts.id("textures/entity/yeti/baby_yeti.png");
    private static final Identifier YETI_ANIMATIONS = CreaturesAndBeasts.id("yeti");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("yeti_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("yeti_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, YETI_MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, YETI_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(YetiEntity entity) {
        return YETI_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(YetiEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, entity.isBaby() ? BABY_YETI_MODEL : YETI_MODEL);
        renderState.addGeckolibData(TEXTURE, entity.isBaby() ? BABY_YETI_TEXTURE : YETI_TEXTURE);
    }
}
