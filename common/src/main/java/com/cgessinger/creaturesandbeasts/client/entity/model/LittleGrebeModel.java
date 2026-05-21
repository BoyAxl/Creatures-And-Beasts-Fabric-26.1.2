package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.LittleGrebeEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class LittleGrebeModel extends GeoModel<LittleGrebeEntity> {
    private static final Identifier LITTLE_GREBE_MODEL = CreaturesAndBeasts.id("geo/entity/little_grebe/little_grebe");
    private static final Identifier LITTLE_GREBE_CHICK_MODEL = CreaturesAndBeasts.id("geo/entity/little_grebe/little_grebe_chick");
    private static final Identifier LITTLE_GREBE_TEXTURE = CreaturesAndBeasts.id("textures/entity/little_grebe/little_grebe.png");
    private static final Identifier LITTLE_GREBE_CHICK_TEXTURE = CreaturesAndBeasts.id("textures/entity/little_grebe/little_grebe_chick.png");
    private static final Identifier LITTLE_GREBE_ANIMATIONS = CreaturesAndBeasts.id("little_grebe");
    private static final DataTicket<Identifier> MODEL = DataTickets.create("little_grebe_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTickets.create("little_grebe_texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, LITTLE_GREBE_MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, LITTLE_GREBE_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(LittleGrebeEntity entity) {
        return LITTLE_GREBE_ANIMATIONS;
    }

    @Override
    public void addAdditionalStateData(LittleGrebeEntity entity, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, entity.isBaby() ? LITTLE_GREBE_CHICK_MODEL : LITTLE_GREBE_MODEL);
        renderState.addGeckolibData(TEXTURE, entity.isBaby() ? LITTLE_GREBE_CHICK_TEXTURE : LITTLE_GREBE_TEXTURE);
    }
}
