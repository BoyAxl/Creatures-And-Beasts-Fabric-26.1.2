package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.LilytadEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class LilytadModel extends GeoModel<LilytadEntity> {
    private static final ResourceLocation LILYTAD_MODEL = CreaturesAndBeasts.id("geo/entity/lilytad/lilytad.geo.json");
    private static final ResourceLocation LILYTAD_SHEARED_TEXTURE = CreaturesAndBeasts.id("textures/entity/lilytad/lilytad_sheared.png");
    private static final ResourceLocation LILYTAD_ANIMATIONS = CreaturesAndBeasts.id("animations/lilytad.json");

    @Override
    public ResourceLocation getModelResource(LilytadEntity entity) {
        return LILYTAD_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LilytadEntity entity) {
        return entity.getSheared() ? LILYTAD_SHEARED_TEXTURE : entity.getLilytadType().getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(LilytadEntity entity) {
        return LILYTAD_ANIMATIONS;
    }
}
