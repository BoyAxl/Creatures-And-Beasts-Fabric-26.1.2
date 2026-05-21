package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import com.cgessinger.creaturesandbeasts.client.entity.model.CactemSpearModel;
import com.cgessinger.creaturesandbeasts.entities.ThrownCactemSpearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

@Environment(EnvType.CLIENT)
public class ThrownCactemSpearRenderer extends EntityRenderer<ThrownCactemSpearEntity, ThrownTridentRenderState> {
    private static final Identifier TEXTURE = CreaturesAndBeasts.id("textures/entity/cactem_spear.png");
    private final CactemSpearModel model;

    public ThrownCactemSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CactemSpearModel(context.bakeLayer(CactemSpearModel.LAYER_LOCATION));
    }

    @Override
    public void submit(ThrownTridentRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.yRot + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderState.xRot));
        submitNodeCollector.order(0).submitModel(this.model, Unit.INSTANCE, poseStack, TEXTURE, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor, null);
        if (renderState.isFoil) {
            submitNodeCollector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, ItemFeatureRenderer.getFoilRenderType(this.model.renderType(TEXTURE), false), renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor, null);
        }
        poseStack.popPose();
        super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public void extractRenderState(ThrownCactemSpearEntity entity, ThrownTridentRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.yRot = entity.getYRot(partialTick);
        renderState.xRot = entity.getXRot(partialTick);
        renderState.isFoil = entity.isFoil();
    }

    @Override
    public ThrownTridentRenderState createRenderState() {
        return new ThrownTridentRenderState();
    }
}
