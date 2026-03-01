package com.cgessinger.creaturesandbeasts.client.armor.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.items.FlowerCrownItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class FlowerCrownModel extends GeoModel<FlowerCrownItem> {
    private final ResourceLocation FLOWER_CROWN_MODEL = CreaturesAndBeasts.id("geo/armor/flower_crown.geo.json");
    private final ResourceLocation FLOWER_CROWN_TEXTURE = CreaturesAndBeasts.id("textures/armor/flower_crown.png");
    private final ResourceLocation FLOWER_CROWN_ANIMATION = CreaturesAndBeasts.id("animations/flower_crown.json");

    @Override
    public ResourceLocation getModelResource(FlowerCrownItem object) {
        return FLOWER_CROWN_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlowerCrownItem object) {
        return FLOWER_CROWN_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlowerCrownItem animatable) {
        return FLOWER_CROWN_ANIMATION;
    }
}
