package com.cgessinger.creaturesandbeasts.client.entity.model;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.LizardEntity;
import com.cgessinger.creaturesandbeasts.init.CNBLizardTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class LizardModel extends GeoModel<LizardEntity> {
    private static final ResourceLocation LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/lizard.geo.json");
    private static final ResourceLocation MUSHROOM_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/mushroom_lizard.geo.json");
    private static final ResourceLocation SAD_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/sad_lizard.geo.json");
    private static final ResourceLocation SAD_MUSHROOM_LIZARD_MODEL = CreaturesAndBeasts.id("geo/entity/lizard/sad_mushroom_lizard.geo.json");

    private static final ResourceLocation LIZARD_ANIMATIONS = CreaturesAndBeasts.id("animations/lizard.json");

    @Override
    public ResourceLocation getModelResource(LizardEntity entity) {
        if (entity.getLizardType().equals(CNBLizardTypes.MUSHROOM)) {
            return entity.getSad() ? SAD_MUSHROOM_LIZARD_MODEL : MUSHROOM_LIZARD_MODEL;
        }
        
        return entity.getSad() ? SAD_LIZARD_MODEL : LIZARD_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LizardEntity entity) {
        return entity.getSad() ? entity.getLizardType().getSadTextureLocation() : entity.getLizardType().getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(LizardEntity entity) {
        return LIZARD_ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(LizardEntity animatable, long instanceId, AnimationState<LizardEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        CoreGeoBone head_rotation = this.getAnimationProcessor().getBone("head_rotation");

        EntityModelData extraData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        head_rotation.setRotX(extraData.headPitch() * ((float) Math.PI / 180F));
        head_rotation.setRotY(extraData.netHeadYaw() * ((float) Math.PI / 180F));
    }
}