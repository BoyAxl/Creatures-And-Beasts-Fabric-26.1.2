package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.MinipadEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@Environment(EnvType.CLIENT)
public class MinipadGlowLayer extends GeoRenderLayer<MinipadEntity> {
    private static final ResourceLocation MINIPAD_MODEL = CreaturesAndBeasts.id("geo/entity/minipad/minipad.geo.json");

    public MinipadGlowLayer(GeoEntityRenderer<MinipadEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, MinipadEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        long time = animatable.level().getDayTime();

        if (animatable.isGlowing()) {
            RenderType eyesTexture = RenderType.entityTranslucent(CreaturesAndBeasts.id("textures/entity/minipad/minipad_eyes_glow.png"));

            RenderType flowerGlow = RenderType.eyes(animatable.getMinipadType().getGlowTextureLocation());
            RenderType flowerTranslucent = RenderType.entityTranslucent(animatable.getMinipadType().getGlowTextureLocation());

            poseStack.pushPose();

            if (!animatable.getSheared()) {
                this.getRenderer().reRender(this.getGeoModel().getBakedModel(MINIPAD_MODEL), poseStack, bufferSource, animatable, flowerGlow, bufferSource.getBuffer(flowerGlow), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 0f);
                this.getRenderer().reRender(this.getGeoModel().getBakedModel(MINIPAD_MODEL), poseStack, bufferSource, animatable, flowerTranslucent, bufferSource.getBuffer(flowerTranslucent), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, (float) Math.pow((time - 18000) / 5000f, 2));
            }
            this.getRenderer().reRender(this.getGeoModel().getBakedModel(MINIPAD_MODEL), poseStack, bufferSource, animatable, eyesTexture, bufferSource.getBuffer(eyesTexture), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, (float) -Math.pow((time-18000)/5000f, 2) + 1);

            poseStack.popPose();
        }
    }
}
