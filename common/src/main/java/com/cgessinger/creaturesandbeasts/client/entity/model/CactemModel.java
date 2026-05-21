package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.CactemEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class CactemModel extends GeoModel<CactemEntity> {
    private static final Identifier ELDER_CACTEM_MODEL = CreaturesAndBeasts.id("geo/entity/cactem/elder_cactem");
    private static final Identifier WARRIOR_CACTEM_MODEL = CreaturesAndBeasts.id("geo/entity/cactem/warrior_cactem");
    private static final Identifier BABY_CACTEM_MODEL = CreaturesAndBeasts.id("geo/entity/cactem/baby_cactem");
    private static final Identifier ELDER_CACTEM_TEXTURE = CreaturesAndBeasts.id("textures/entity/cactem/elder_cactem.png");
    private static final Identifier WARRIOR_CACTEM_TEXTURE = CreaturesAndBeasts.id("textures/entity/cactem/warrior_cactem.png");
    private static final Identifier BABY_CACTEM_TEXTURE = CreaturesAndBeasts.id("textures/entity/cactem/baby_cactem.png");
    private static final Identifier CACTEM_ANIMATIONS = CreaturesAndBeasts.id("cactem");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("cactem_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("cactem_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, WARRIOR_CACTEM_MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, WARRIOR_CACTEM_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(CactemEntity entity) {
        return CACTEM_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(CactemEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, modelFor(entity));
        renderState.addGeckolibData(TEXTURE, textureFor(entity));
    }

    private static Identifier modelFor(CactemEntity entity) {
        if (entity.isBaby()) {
            return BABY_CACTEM_MODEL;
        }
        return entity.isElder() ? ELDER_CACTEM_MODEL : WARRIOR_CACTEM_MODEL;
    }

    private static Identifier textureFor(CactemEntity entity) {
        if (entity.isBaby()) {
            return BABY_CACTEM_TEXTURE;
        }
        return entity.isElder() ? ELDER_CACTEM_TEXTURE : WARRIOR_CACTEM_TEXTURE;
    }
}
