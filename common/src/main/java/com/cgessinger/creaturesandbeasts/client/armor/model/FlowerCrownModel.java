package com.cgessinger.creaturesandbeasts.client.armor.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class FlowerCrownModel extends GeoModel<FlowerCrownItem> {
    private static final Identifier FLOWER_CROWN_MODEL = CreaturesAndBeasts.id("geo/armor/flower_crown");
    private static final Identifier FLOWER_CROWN_TEXTURE = CreaturesAndBeasts.id("textures/armor/flower_crown.png");
    private static final Identifier FLOWER_CROWN_ANIMATION = CreaturesAndBeasts.id("flower_crown");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return FLOWER_CROWN_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return FLOWER_CROWN_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(FlowerCrownItem animatable) {
        return FLOWER_CROWN_ANIMATION;
    }
}
