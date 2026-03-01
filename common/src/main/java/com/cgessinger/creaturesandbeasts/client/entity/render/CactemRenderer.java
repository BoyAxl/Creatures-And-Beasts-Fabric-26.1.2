package com.cgessinger.creaturesandbeasts.client.entity.render;

import com.cgessinger.creaturesandbeasts.client.entity.model.CactemModel;
import com.cgessinger.creaturesandbeasts.entities.CactemEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class CactemRenderer extends GeoEntityRenderer<CactemEntity> {
    public CactemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CactemModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public RenderType getRenderType(CactemEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, CactemEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("ItemHolder") && animatable.isTrading()) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-0.4F, 0.3F, -0.1F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.TOTEM_OF_UNDYING), ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, poseStack, bufferSource, animatable.level(), 0);
            poseStack.popPose();
            buffer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(animatable)));
        }
        if (!bone.getName().equals("spear") || animatable.isSpearShown()) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
