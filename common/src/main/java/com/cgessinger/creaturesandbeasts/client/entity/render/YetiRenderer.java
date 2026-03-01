package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.YetiModel;
import com.cgessinger.creaturesandbeasts.entities.YetiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class YetiRenderer extends GeoEntityRenderer<YetiEntity> {
    public YetiRenderer(EntityRendererProvider.Context context) {
        super(context, new YetiModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, YetiEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("ItemHolder")) {
            poseStack.pushPose();

            if (animatable != null && animatable.isBaby()) {
                poseStack.translate(bone.getPosX() + 0.05D, bone.getPosY() + 0.3D, bone.getPosZ() + 0.15D);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-43.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            } else if (animatable != null){
                poseStack.translate(bone.getPosX() + 0.3D, bone.getPosY() + 1.0D, bone.getPosZ());
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable.getHolding(), ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, poseStack, bufferSource, animatable.level(), 0);
            poseStack.popPose();
            buffer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(animatable)));
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(YetiEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
