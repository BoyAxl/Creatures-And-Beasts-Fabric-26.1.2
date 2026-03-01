package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.entities.CindershellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@Environment(EnvType.CLIENT)
public class CindershellGlowLayer extends GeoRenderLayer<CindershellEntity> {
    private static final ResourceLocation GLOW_LAYER = CreaturesAndBeasts.id("textures/entity/cindershell/cindershell_glow.png");
    private static final ResourceLocation CINDERSHELL_MODEL = CreaturesAndBeasts.id("geo/entity/cindershell/cindershell.geo.json");
    private static final ResourceLocation CINDERSHELL_FURNACE_MODEL = CreaturesAndBeasts.id("geo/entity/cindershell/cindershell_furnace.geo.json");

    public CindershellGlowLayer(GeoEntityRenderer<CindershellEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, CindershellEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.isBaby()) {
            renderType = RenderType.eyes(GLOW_LAYER);
            poseStack.pushPose();
            this.getRenderer().reRender(this.getGeoModel().getBakedModel(animatable.hasFurnace() ? CINDERSHELL_FURNACE_MODEL : CINDERSHELL_MODEL), poseStack, bufferSource, animatable, renderType, bufferSource.getBuffer(renderType), partialTick, packedLight, LivingEntityRenderer.getOverlayCoords(animatable, 0.0F), 1f, 1f, 1f, 1f);
            poseStack.popPose();
        }
    }
}
