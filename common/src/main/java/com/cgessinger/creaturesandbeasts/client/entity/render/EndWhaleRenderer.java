package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.EndWhaleModel;
import com.cgessinger.creaturesandbeasts.entities.EndWhaleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class EndWhaleRenderer extends GeoEntityRenderer<EndWhaleEntity> {

    public EndWhaleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EndWhaleModel());
        this.shadowRadius = 1.5F;
    }

    @Override
    public RenderType getRenderType(EndWhaleEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    protected void applyRotations(EndWhaleEntity endWhale, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(endWhale, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
        float whaleRotY = endWhale.getViewYRot(partialTicks);
        float wantedRotY;
        float whaleRotX = endWhale.getViewXRot(partialTicks);
        float wantedRotX;
        Entity rider = endWhale.getFirstPassenger();

        if (rider != null) {
            wantedRotY = rider.getViewYRot(partialTicks);
            wantedRotX = rider.getViewXRot(partialTicks);
        } else {
            wantedRotY = endWhale.yBodyRot;
            wantedRotX = endWhale.getXRot();
        }

        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.wrapDegrees(whaleRotY - wantedRotY) / 2));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(Mth.wrapDegrees(whaleRotX - wantedRotX)));
    }
}
