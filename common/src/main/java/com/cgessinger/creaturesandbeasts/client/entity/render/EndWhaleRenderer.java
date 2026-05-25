package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.EndWhaleModel;
import com.cgessinger.creaturesandbeasts.entities.EndWhaleEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EndWhaleRenderer extends GeoEntityRenderer<EndWhaleEntity, LivingEntityRenderState> {
    private static final DataTicket<Float> RIDING_Z_ROT = DataTickets.create("cnb_end_whale_riding_z_rot", Float.class);
    private static final DataTicket<Float> RIDING_X_ROT = DataTickets.create("cnb_end_whale_riding_x_rot", Float.class);

    public EndWhaleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EndWhaleModel());
        this.shadowRadius = 1.5F;
        this.withRenderLayer(new EndWhaleGlowLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public void addRenderData(EndWhaleEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        float whaleRotY = animatable.getViewYRot(partialTick);
        float whaleRotX = animatable.getViewXRot(partialTick);
        float wantedRotY;
        float wantedRotX;
        Entity rider = animatable.getFirstPassenger();

        if (rider != null) {
            wantedRotY = rider.getViewYRot(partialTick);
            wantedRotX = rider.getViewXRot(partialTick);
        } else {
            wantedRotY = animatable.yBodyRot;
            wantedRotX = animatable.getXRot();
        }

        renderState.addGeckolibData(RIDING_Z_ROT, Mth.wrapDegrees(whaleRotY - wantedRotY) / 2F);
        renderState.addGeckolibData(RIDING_X_ROT, Mth.wrapDegrees(whaleRotX - wantedRotX));
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderInfo, poseStack, nativeScale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderInfo.getOrDefaultGeckolibData(RIDING_Z_ROT, 0F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderInfo.getOrDefaultGeckolibData(RIDING_X_ROT, 0F)));
    }
}
