package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.YetiEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class YetiModel extends GeoModel<YetiEntity> {
    private static final ResourceLocation YETI_MODEL = CreaturesAndBeasts.id("geo/entity/yeti/yeti.geo.json");
    private static final ResourceLocation BABY_YETI_MODEL = CreaturesAndBeasts.id("geo/entity/yeti/baby_yeti.geo.json");

    private static final ResourceLocation YETI_TEXTURE = CreaturesAndBeasts.id("textures/entity/yeti/yeti.png");
    private static final ResourceLocation BABY_YETI_TEXTURE = CreaturesAndBeasts.id("textures/entity/yeti/baby_yeti.png");

    private static final ResourceLocation YETI_ANIMATIONS = CreaturesAndBeasts.id("animations/yeti.json");

    @Override
    public ResourceLocation getModelResource(YetiEntity entity) {
        return entity.isBaby() ? BABY_YETI_MODEL : YETI_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(YetiEntity entity) {
        return entity.isBaby() ? BABY_YETI_TEXTURE : YETI_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(YetiEntity entity) {
        return YETI_ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(YetiEntity animatable, long instanceId, AnimationState<YetiEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        CoreGeoBone head_rotation = this.getAnimationProcessor().getBone("head_rotation");

        EntityModelData extraData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        head_rotation.setRotX(extraData.headPitch() * ((float) Math.PI / 180F));
        if (animatable.isBaby()) {
            head_rotation.setRotZ(extraData.netHeadYaw() * ((float) Math.PI / 180F));
        } else {
            head_rotation.setRotY(extraData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}
